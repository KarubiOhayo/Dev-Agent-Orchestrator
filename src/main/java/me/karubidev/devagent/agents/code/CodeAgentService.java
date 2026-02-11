package me.karubidev.devagent.agents.code;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    if (request == null) {
      throw new IllegalArgumentException("request is required");
    }

    String projectId = normalizeProjectId(request.getProjectId());
    Path targetRoot = resolveTargetRoot(request.getTargetProjectRoot());
    String requestPreview = requestPreview(request);
    if (requestPreview.isBlank()) {
      throw new IllegalArgumentException("userRequest or specInputPath is required");
    }

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
        requestPreview
    );

    try {
      SpecInput specInput = loadSpecInput(request.getSpecInputPath(), targetRoot);
      if (specInput != null) {
        runStateStore.appendEvent(runId, "SPEC_INPUT_LOADED", specInput.path().toString());
      }

      String resolvedUserRequest = buildUserRequest(request.getUserRequest(), specInput);
      if (resolvedUserRequest.isBlank()) {
        throw new IllegalArgumentException("userRequest or readable specInputPath is required");
      }

      ContextBundle context = contextManager.buildCodeContext(resolvedUserRequest, projectId, targetRoot);
      String prompt = promptRegistry.buildPrompt(
          "code",
          targetRoot,
          resolvedUserRequest,
          appendSpecContext(context.text(), specInput)
      );
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
        specInputPath: %s
        mode: %s
        model: %s:%s
        parsedFiles: %d
        writtenFiles: %d
        dryRun: %s
        """.formatted(
        requestPreview(request),
        request.getSpecInputPath(),
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

  private String requestPreview(CodeGenerateRequest request) {
    if (request.getUserRequest() != null && !request.getUserRequest().isBlank()) {
      return request.getUserRequest();
    }
    if (request.getSpecInputPath() != null && !request.getSpecInputPath().isBlank()) {
      return "specInputPath=" + request.getSpecInputPath();
    }
    return "";
  }

  private SpecInput loadSpecInput(String specInputPath, Path targetRoot) {
    if (specInputPath == null || specInputPath.isBlank()) {
      return null;
    }

    Path rawPath = Path.of(specInputPath);
    Path resolvedPath = rawPath.isAbsolute() ? rawPath : targetRoot.resolve(rawPath);
    Path normalized = resolvedPath.toAbsolutePath().normalize();

    try {
      String text = Files.readString(normalized, StandardCharsets.UTF_8);
      return new SpecInput(normalized, text);
    } catch (IOException e) {
      throw new IllegalArgumentException("failed to read specInputPath: " + normalized, e);
    }
  }

  private String buildUserRequest(String userRequest, SpecInput specInput) {
    String baseRequest = userRequest == null ? "" : userRequest.trim();
    if (specInput == null) {
      return baseRequest;
    }

    String specPrompt = """
        Use this specification JSON as the source of truth for implementation.
        [SPEC_INPUT_PATH]
        %s
        [SPEC_JSON]
        %s
        """.formatted(specInput.path(), trim(specInput.content(), 7000));

    if (baseRequest.isBlank()) {
      return specPrompt;
    }
    return baseRequest + "\n\n" + specPrompt;
  }

  private String appendSpecContext(String contextText, SpecInput specInput) {
    if (specInput == null) {
      return contextText;
    }
    return contextText + "\n\n## SPEC_INPUT\n### " + specInput.path() + "\n" + trim(specInput.content(), 7000);
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

  private record SpecInput(Path path, String content) {
  }
}
