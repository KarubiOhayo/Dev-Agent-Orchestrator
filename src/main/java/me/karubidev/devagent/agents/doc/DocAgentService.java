package me.karubidev.devagent.agents.doc;

import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
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
public class DocAgentService {

  private final ModelRouter modelRouter;
  private final LlmOrchestratorService llmOrchestrator;
  private final ProjectContextManager contextManager;
  private final PromptRegistry promptRegistry;
  private final RunStateStore runStateStore;
  private final DocOutputSchemaParser docOutputSchemaParser;

  public DocAgentService(
      ModelRouter modelRouter,
      LlmOrchestratorService llmOrchestrator,
      ProjectContextManager contextManager,
      PromptRegistry promptRegistry,
      RunStateStore runStateStore,
      DocOutputSchemaParser docOutputSchemaParser
  ) {
    this.modelRouter = modelRouter;
    this.llmOrchestrator = llmOrchestrator;
    this.contextManager = contextManager;
    this.promptRegistry = promptRegistry;
    this.runStateStore = runStateStore;
    this.docOutputSchemaParser = docOutputSchemaParser;
  }

  public DocGenerateResponse generate(DocGenerateRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request is required");
    }

    String projectId = normalizeProjectId(request.getProjectId());
    Path targetRoot = resolveTargetRoot(request.getTargetProjectRoot());
    String resolvedUserRequest = resolveUserRequest(request);

    RouteRequest routeRequest = new RouteRequest();
    routeRequest.setAgentType(AgentType.DOC);
    routeRequest.setMode(request.getMode());
    routeRequest.setRiskLevel(request.getRiskLevel());
    routeRequest.setLargeContext(request.isLargeContext());
    routeRequest.setStrictJsonRequired(request.isStrictJsonRequired());

    RouteDecision routeDecision = modelRouter.resolve(routeRequest);
    String runId = runStateStore.startRun(
        projectId,
        AgentType.DOC.name(),
        routeDecision.mode().name(),
        requestPreview(request, resolvedUserRequest)
    );

    try {
      ContextBundle context = contextManager.buildCodeContext(resolvedUserRequest, projectId, targetRoot);
      String prompt = promptRegistry.buildPrompt(
          "doc",
          targetRoot,
          buildDocRequest(resolvedUserRequest),
          appendCodeContext(context.text(), request)
      );
      runStateStore.appendEvent(runId, "DOC_PROMPT_READY", "contextFiles=" + context.referencedFiles().size());

      LlmExecutionResult execution = llmOrchestrator.generate(routeDecision, prompt);
      runStateStore.appendEvent(runId, "DOC_LLM_DONE",
          execution.result().provider() + ":" + execution.result().model());

      DocOutputSchemaParser.ParseResult parseResult = docOutputSchemaParser.parse(
          execution.result().text(),
          resolvedUserRequest,
          request.getCodeFiles()
      );
      if (parseResult.usedFallback()) {
        runStateStore.appendEvent(runId, "DOC_OUTPUT_FALLBACK_WARNING", "source=" + parseResult.source().name());
      }

      JsonNode document = parseResult.document();
      runStateStore.appendEvent(runId, "DOC_SCHEMA_READY", "keys=" + document.size());

      String summary = summarize(request, execution, document);
      runStateStore.updateProjectSummary(projectId, summary);
      runStateStore.completeSuccess(
          runId,
          projectId,
          execution.result().provider(),
          execution.result().model(),
          document.toString()
      );

      return new DocGenerateResponse(
          runId,
          projectId,
          targetRoot.toString(),
          routeDecision,
          execution.result().provider(),
          execution.result().model(),
          document,
          execution.attempts(),
          context.referencedFiles(),
          runStateStore.getProjectSummary(projectId),
          request.getCodeRunId()
      );
    } catch (Exception e) {
      runStateStore.appendEvent(runId, "DOC_FAILED", errorMessage(e));
      runStateStore.completeFailure(runId, projectId, e.getMessage());
      throw e;
    }
  }

  private String buildDocRequest(String userRequest) {
    return """
        Create a strict JSON documentation output.
        Return JSON object only with this shape:
        {
          "title": "string",
          "summary": "string",
          "sections": [
            {"heading": "string", "content": "string"}
          ],
          "relatedFiles": ["relative/path"],
          "notes": ["string"]
        }
        USER_REQUEST:
        %s
        """.formatted(userRequest);
  }

  private String appendCodeContext(String contextText, DocGenerateRequest request) {
    StringBuilder sb = new StringBuilder(contextText == null ? "" : contextText);
    if (request.getCodeRunId() != null && !request.getCodeRunId().isBlank()) {
      sb.append("\n\n## CODE_RUN\n").append(request.getCodeRunId().trim());
    }

    List<GeneratedFile> codeFiles = request.getCodeFiles() == null ? List.of() : request.getCodeFiles();
    if (!codeFiles.isEmpty()) {
      sb.append("\n\n## GENERATED_FILES\n");
      int limit = Math.min(codeFiles.size(), 40);
      for (int i = 0; i < limit; i++) {
        GeneratedFile file = codeFiles.get(i);
        if (file == null || file.path() == null || file.path().isBlank()) {
          continue;
        }
        sb.append("- ").append(file.path().trim()).append("\n");
      }
    }

    if (request.getCodeOutput() != null && !request.getCodeOutput().isBlank()) {
      sb.append("\n\n## CODE_OUTPUT_SNIPPET\n")
          .append(trim(request.getCodeOutput(), 6000));
    }
    return sb.toString();
  }

  private String summarize(DocGenerateRequest request, LlmExecutionResult execution, JsonNode document) {
    return """
        lastDocRequest: %s
        sourceCodeRunId: %s
        mode: %s
        model: %s:%s
        schemaKeys: %d
        relatedFiles: %d
        """.formatted(
        resolveUserRequest(request),
        request.getCodeRunId(),
        request.getMode(),
        execution.result().provider(),
        execution.result().model(),
        document.size(),
        document.path("relatedFiles").size()
    );
  }

  private String resolveUserRequest(DocGenerateRequest request) {
    if (request.getUserRequest() != null && !request.getUserRequest().isBlank()) {
      return request.getUserRequest().trim();
    }
    if (request.getCodeRunId() != null && !request.getCodeRunId().isBlank()) {
      return "Generate developer documentation based on code run " + request.getCodeRunId().trim() + ".";
    }
    return "Generate developer documentation based on the provided code output.";
  }

  private String requestPreview(DocGenerateRequest request, String resolvedUserRequest) {
    if (request.getUserRequest() != null && !request.getUserRequest().isBlank()) {
      return request.getUserRequest();
    }
    if (request.getCodeRunId() != null && !request.getCodeRunId().isBlank()) {
      return "codeRunId=" + request.getCodeRunId();
    }
    return resolvedUserRequest;
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

  private String trim(String value, int maxChars) {
    if (value == null) {
      return "";
    }
    if (value.length() <= maxChars) {
      return value;
    }
    return value.substring(0, maxChars) + "\n...(truncated)...";
  }

  private String errorMessage(Exception e) {
    if (e.getMessage() == null || e.getMessage().isBlank()) {
      return e.getClass().getSimpleName();
    }
    return e.getMessage();
  }
}
