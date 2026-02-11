package me.karubidev.devagent.agents.code;

import java.nio.file.Path;
import me.karubidev.devagent.agents.code.apply.CodeOutputParser;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.FileApplyService;
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

@Service
public class CodeAgentService {

  private final ModelRouter modelRouter;
  private final LlmOrchestratorService llmOrchestrator;
  private final ProjectContextManager contextManager;
  private final PromptRegistry promptRegistry;
  private final RunStateStore runStateStore;
  private final CodeOutputParser codeOutputParser;
  private final FileApplyService fileApplyService;

  public CodeAgentService(
      ModelRouter modelRouter,
      LlmOrchestratorService llmOrchestrator,
      ProjectContextManager contextManager,
      PromptRegistry promptRegistry,
      RunStateStore runStateStore,
      CodeOutputParser codeOutputParser,
      FileApplyService fileApplyService
  ) {
    this.modelRouter = modelRouter;
    this.llmOrchestrator = llmOrchestrator;
    this.contextManager = contextManager;
    this.promptRegistry = promptRegistry;
    this.runStateStore = runStateStore;
    this.codeOutputParser = codeOutputParser;
    this.fileApplyService = fileApplyService;
  }

  public CodeGenerateResponse generate(CodeGenerateRequest request) {
    if (request == null || request.getUserRequest() == null || request.getUserRequest().isBlank()) {
      throw new IllegalArgumentException("userRequest is required");
    }

    String projectId = normalizeProjectId(request.getProjectId());
    Path targetRoot = resolveTargetRoot(request.getTargetProjectRoot());

    RouteRequest routeRequest = new RouteRequest();
    routeRequest.setAgentType(AgentType.CODE);
    routeRequest.setMode(request.getMode());
    routeRequest.setRiskLevel(request.getRiskLevel());
    routeRequest.setLargeContext(request.isLargeContext());
    routeRequest.setStrictJsonRequired(request.isStrictJsonRequired());

    RouteDecision routeDecision = modelRouter.resolve(routeRequest);
    String runId = runStateStore.startRun(
        projectId,
        AgentType.CODE.name(),
        routeDecision.mode().name(),
        request.getUserRequest()
    );

    try {
      ContextBundle context = contextManager.buildCodeContext(request.getUserRequest(), projectId, targetRoot);
      String prompt = promptRegistry.buildPrompt("code", targetRoot, request.getUserRequest(), context.text());
      runStateStore.appendEvent(runId, "PROMPT_READY", "contextFiles=" + context.referencedFiles().size());

      LlmExecutionResult execution = llmOrchestrator.generate(routeDecision, prompt);
      runStateStore.appendEvent(runId, "LLM_DONE",
          execution.result().provider() + ":" + execution.result().model());

      var parsedFiles = codeOutputParser.parseFiles(execution.result().text());
      FileApplyResult applyResult = fileApplyService.apply(
          targetRoot,
          parsedFiles,
          !request.isApply(),
          request.isOverwriteExisting()
      );

      String summary = summarize(request, execution, applyResult);
      runStateStore.updateProjectSummary(projectId, summary);
      runStateStore.completeSuccess(
          runId,
          projectId,
          execution.result().provider(),
          execution.result().model(),
          execution.result().text()
      );

      return new CodeGenerateResponse(
          runId,
          projectId,
          targetRoot.toString(),
          routeDecision,
          execution.result().provider(),
          execution.result().model(),
          execution.result().text(),
          execution.attempts(),
          context.referencedFiles(),
          runStateStore.getProjectSummary(projectId),
          applyResult
      );
    } catch (Exception e) {
      runStateStore.completeFailure(runId, projectId, e.getMessage());
      throw e;
    }
  }

  private String summarize(CodeGenerateRequest request, LlmExecutionResult execution, FileApplyResult applyResult) {
    return """
        lastRequest: %s
        mode: %s
        model: %s:%s
        parsedFiles: %d
        writtenFiles: %d
        dryRun: %s
        """.formatted(
        request.getUserRequest(),
        request.getMode(),
        execution.result().provider(),
        execution.result().model(),
        applyResult.parsedFiles(),
        applyResult.writtenFiles(),
        applyResult.dryRun()
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
