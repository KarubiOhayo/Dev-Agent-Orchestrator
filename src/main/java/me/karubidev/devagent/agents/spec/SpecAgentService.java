package me.karubidev.devagent.agents.spec;

import java.nio.file.Path;
import me.karubidev.devagent.context.ContextBundle;
import me.karubidev.devagent.context.ProjectContextManager;
import me.karubidev.devagent.llm.LlmExecutionResult;
import me.karubidev.devagent.llm.LlmOrchestratorService;
import me.karubidev.devagent.orchestration.routing.AgentType;
import me.karubidev.devagent.orchestration.routing.ModelRouter;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import me.karubidev.devagent.orchestration.routing.RouteRequest;
import me.karubidev.devagent.prompt.PromptRegistry;
import me.karubidev.devagent.state.RunStateStore;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class SpecAgentService {

  private final ModelRouter modelRouter;
  private final LlmOrchestratorService llmOrchestrator;
  private final ProjectContextManager contextManager;
  private final PromptRegistry promptRegistry;
  private final RunStateStore runStateStore;
  private final SpecOutputSchemaParser specOutputSchemaParser;
  private final SpecCodeChainService specCodeChainService;

  public SpecAgentService(
      ModelRouter modelRouter,
      LlmOrchestratorService llmOrchestrator,
      ProjectContextManager contextManager,
      PromptRegistry promptRegistry,
      RunStateStore runStateStore,
      SpecOutputSchemaParser specOutputSchemaParser,
      SpecCodeChainService specCodeChainService
  ) {
    this.modelRouter = modelRouter;
    this.llmOrchestrator = llmOrchestrator;
    this.contextManager = contextManager;
    this.promptRegistry = promptRegistry;
    this.runStateStore = runStateStore;
    this.specOutputSchemaParser = specOutputSchemaParser;
    this.specCodeChainService = specCodeChainService;
  }

  public SpecGenerateResponse generate(SpecGenerateRequest request) {
    if (request == null || request.getUserRequest() == null || request.getUserRequest().isBlank()) {
      throw new IllegalArgumentException("userRequest is required");
    }

    String projectId = normalizeProjectId(request.getProjectId());
    Path targetRoot = resolveTargetRoot(request.getTargetProjectRoot());

    RouteRequest routeRequest = new RouteRequest();
    routeRequest.setAgentType(AgentType.SPEC);
    routeRequest.setMode(request.getMode());
    routeRequest.setRiskLevel(request.getRiskLevel());
    routeRequest.setLargeContext(request.isLargeContext());
    routeRequest.setStrictJsonRequired(request.isStrictJsonRequired());

    RouteDecision routeDecision = modelRouter.resolve(routeRequest);
    String runId = runStateStore.startRun(
        projectId,
        AgentType.SPEC.name(),
        routeDecision.mode().name(),
        request.getUserRequest()
    );

    try {
      ContextBundle context = contextManager.buildCodeContext(request.getUserRequest(), projectId, targetRoot);
      String prompt = promptRegistry.buildPrompt(
          "spec",
          targetRoot,
          buildSpecRequest(request.getUserRequest()),
          context.text()
      );
      runStateStore.appendEvent(runId, "PROMPT_READY", "contextFiles=" + context.referencedFiles().size());

      LlmExecutionResult execution = llmOrchestrator.generate(routeDecision, prompt);
      runStateStore.appendEvent(runId, "LLM_DONE",
          execution.result().provider() + ":" + execution.result().model());

      SpecOutputSchemaParser.ParseResult parseResult = specOutputSchemaParser.parse(
          execution.result().text(),
          request.getUserRequest()
      );
      if (parseResult.source() != SpecOutputSchemaParser.ParseSource.DIRECT_JSON) {
        runStateStore.appendEvent(runId, "SPEC_OUTPUT_FALLBACK_WARNING", "source=" + parseResult.source().name());
      }

      JsonNode specSchema = parseResult.spec();
      runStateStore.appendEvent(runId, "SPEC_SCHEMA_READY", "keys=" + specSchema.size());

      var chainedCodeResult = specCodeChainService.runChain(runId, request, specSchema, targetRoot);
      String summary = summarize(request, execution, specSchema, chainedCodeResult != null);
      runStateStore.updateProjectSummary(projectId, summary);
      runStateStore.completeSuccess(
          runId,
          projectId,
          execution.result().provider(),
          execution.result().model(),
          specSchema.toString()
      );

      return new SpecGenerateResponse(
          runId,
          projectId,
          targetRoot.toString(),
          routeDecision,
          execution.result().provider(),
          execution.result().model(),
          specSchema,
          execution.attempts(),
          context.referencedFiles(),
          runStateStore.getProjectSummary(projectId),
          chainedCodeResult
      );
    } catch (Exception e) {
      runStateStore.completeFailure(runId, projectId, e.getMessage());
      throw e;
    }
  }

  private String buildSpecRequest(String userRequest) {
    return """
        Create a strict JSON specification.
        Return JSON object only with this shape:
        {
          "title": "string",
          "overview": "string",
          "constraints": ["string"],
          "acceptanceCriteria": ["string"],
          "tasks": [
            {"id": "TASK-1", "description": "string", "files": ["relative/path"]}
          ],
          "notes": ["string"]
        }
        USER_REQUEST:
        %s
        """.formatted(userRequest);
  }

  private String summarize(SpecGenerateRequest request, LlmExecutionResult execution, JsonNode specSchema,
      boolean chainExecuted) {
    return """
        lastSpecRequest: %s
        mode: %s
        model: %s:%s
        schemaKeys: %d
        chainToCode: %s
        """.formatted(
        request.getUserRequest(),
        request.getMode(),
        execution.result().provider(),
        execution.result().model(),
        specSchema.size(),
        chainExecuted
    );
  }

  private String normalizeProjectId(String projectId) {
    if (projectId == null || projectId.isBlank()) {
      return "default";
    }
    return projectId.trim();
  }

  private Path resolveTargetRoot(String targetProjectRoot) {
    String raw = (targetProjectRoot == null || targetProjectRoot.isBlank()) ? "." : targetProjectRoot;
    return Path.of(raw).toAbsolutePath().normalize();
  }
}
