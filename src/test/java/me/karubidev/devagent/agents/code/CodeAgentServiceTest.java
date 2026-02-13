package me.karubidev.devagent.agents.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.apply.CodeOutputParser;
import me.karubidev.devagent.agents.code.apply.CodeOutputParser.ParseSource;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.FileApplyService;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.agents.doc.CodeDocChainService;
import me.karubidev.devagent.agents.doc.DocGenerateResponse;
import me.karubidev.devagent.agents.review.CodeReviewChainService;
import me.karubidev.devagent.agents.review.ReviewGenerateResponse;
import me.karubidev.devagent.context.ContextBundle;
import me.karubidev.devagent.context.ProjectContextManager;
import me.karubidev.devagent.llm.LlmExecutionResult;
import me.karubidev.devagent.llm.LlmGenerationResult;
import me.karubidev.devagent.llm.LlmOrchestratorService;
import me.karubidev.devagent.orchestration.routing.AgentType;
import me.karubidev.devagent.orchestration.routing.ModelRef;
import me.karubidev.devagent.orchestration.routing.ModelRouter;
import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import me.karubidev.devagent.orchestration.routing.RoutingMode;
import me.karubidev.devagent.prompt.PromptRegistry;
import me.karubidev.devagent.state.RunStateStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

class CodeAgentServiceTest {

  @Test
  void generateAcceptsSpecInputPath(@TempDir Path tempDir) throws Exception {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    Path specPath = tempDir.resolve("spec.json");
    Files.writeString(specPath, """
        {"title":"Spec","tasks":[{"id":"TASK-1"}]}
        """, StandardCharsets.UTF_8);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setSpecInputPath("spec.json");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), contains("specInputPath")))
        .thenReturn("run-1");
    when(contextManager.buildCodeContext(anyString(), eq("p1"), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(new ContextBundle("ctx", List.of("docs/rule.md")));
    when(promptRegistry.buildPrompt(eq("code"), eq(tempDir.toAbsolutePath().normalize()), anyString(), anyString()))
        .thenReturn("prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2-codex", "raw-output", "{}"),
            List.of()
        )
    );
    when(codeOutputParser.parse(eq("raw-output"))).thenReturn(
        new CodeOutputParser.ParseResult(
            List.of(new GeneratedFile("src/A.java", "class A {}")),
            ParseSource.JSON
        )
    );
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), anyBoolean())).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/A.java", "DRY_RUN", "ok")))
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    CodeGenerateResponse response = service.generate(request);

    assertThat(response.runId()).isEqualTo("run-1");
    assertThat(response.files()).hasSize(1);
    assertThat(response.files().get(0).path()).isEqualTo("src/A.java");
    verify(runStateStore).appendEvent(eq("run-1"), eq("SPEC_INPUT_LOADED"), contains("spec.json"));

    ArgumentCaptor<String> userRequestCaptor = ArgumentCaptor.forClass(String.class);
    verify(contextManager).buildCodeContext(userRequestCaptor.capture(), eq("p1"), eq(tempDir.toAbsolutePath().normalize()));
    assertThat(userRequestCaptor.getValue()).contains("[SPEC_INPUT_PATH]");
  }

  @Test
  void generateRejectsAbsoluteSpecInputPath(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setSpecInputPath(tempDir.resolve("spec.json").toString());

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), contains("specInputPath")))
        .thenReturn("run-abs");

    assertThatThrownBy(() -> service.generate(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("relative path");

    verify(runStateStore).appendEvent(eq("run-abs"), eq("SPEC_INPUT_FAILED"), contains("relative path"));
    verify(runStateStore).appendEvent(eq("run-abs"), eq("CODE_FAILED"), contains("relative path"));
  }

  @Test
  void generateRejectsTraversalSpecInputPath(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setSpecInputPath("../outside/spec.json");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), contains("specInputPath")))
        .thenReturn("run-up");

    assertThatThrownBy(() -> service.generate(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not contain '..'");

    verify(runStateStore).appendEvent(eq("run-up"), eq("SPEC_INPUT_FAILED"), contains("must not contain '..'"));
    verify(runStateStore).appendEvent(eq("run-up"), eq("CODE_FAILED"), contains("must not contain '..'"));
  }

  @Test
  void generateRecordsCodeFailedWhenTargetProjectRootIsInvalid() {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setUserRequest("implement");
    request.setTargetProjectRoot("bad\u0000root");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), eq("implement")))
        .thenReturn("run-root");

    assertThatThrownBy(() -> service.generate(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("targetProjectRoot is invalid");

    verify(runStateStore).appendEvent(eq("run-root"), eq("CODE_FAILED"), contains("targetProjectRoot is invalid"));
    verify(runStateStore).completeFailure(eq("run-root"), eq("p1"), contains("targetProjectRoot is invalid"));
  }

  @Test
  void generateRecordsWarningEventWhenMarkdownFallbackUsed(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setUserRequest("implement");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), eq("implement")))
        .thenReturn("run-fallback");
    when(contextManager.buildCodeContext(eq("implement"), eq("p1"), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("code"), eq(tempDir.toAbsolutePath().normalize()), eq("implement"), anyString()))
        .thenReturn("prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2-codex", "raw-output", "{}"),
            List.of()
        )
    );
    when(codeOutputParser.parse(eq("raw-output"))).thenReturn(
        new CodeOutputParser.ParseResult(
            List.of(new GeneratedFile("src/Fallback.java", "class Fallback {}")),
            ParseSource.MARKDOWN_FALLBACK
        )
    );
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), eq(false))).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/Fallback.java", "DRY_RUN", "ok")))
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    CodeGenerateResponse response = service.generate(request);

    assertThat(response.files()).hasSize(1);
    verify(runStateStore).appendEvent(eq("run-fallback"), eq("CODE_OUTPUT_FALLBACK_WARNING"), contains("MARKDOWN_FALLBACK"));
  }

  @Test
  void generateRunsCodeToDocChainWhenRequested(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setUserRequest("implement");
    request.setChainToDoc(true);
    request.setChainFailurePolicy(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), eq("implement")))
        .thenReturn("run-chain");
    when(contextManager.buildCodeContext(eq("implement"), eq("p1"), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("code"), eq(tempDir.toAbsolutePath().normalize()), eq("implement"), anyString()))
        .thenReturn("prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2-codex", "raw-output", "{}"),
            List.of()
        )
    );
    when(codeOutputParser.parse(eq("raw-output"))).thenReturn(
        new CodeOutputParser.ParseResult(
            List.of(new GeneratedFile("src/A.java", "class A {}")),
            ParseSource.JSON
        )
    );
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), eq(false))).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/A.java", "DRY_RUN", "ok")))
    );

    DocGenerateResponse docResponse = new DocGenerateResponse(
        "doc-run-1",
        "p1",
        tempDir.toString(),
        null,
        "openai",
        "gpt-5.2",
        new ObjectMapper().createObjectNode().put("title", "Doc"),
        List.of(),
        List.of(),
        "summary",
        "run-chain"
    );
    when(codeDocChainService.runChain(eq("run-chain"), eq(request), any(CodeGenerateResponse.class), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(docResponse);
    when(codeReviewChainService.runChain(eq("run-chain"), eq(request), any(CodeGenerateResponse.class), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(null);
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    CodeGenerateResponse response = service.generate(request);

    assertThat(response.chainedDocResult()).isNotNull();
    assertThat(response.chainedDocResult().runId()).isEqualTo("doc-run-1");
    assertThat(response.chainFailures()).isEmpty();
    verify(codeDocChainService).runChain(eq("run-chain"), eq(request), any(CodeGenerateResponse.class), eq(tempDir.toAbsolutePath().normalize()));
  }

  @Test
  void generateRunsCodeToReviewChainWhenRequested(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setUserRequest("implement");
    request.setChainToReview(true);
    request.setChainFailurePolicy(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), eq("implement")))
        .thenReturn("run-review");
    when(contextManager.buildCodeContext(eq("implement"), eq("p1"), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("code"), eq(tempDir.toAbsolutePath().normalize()), eq("implement"), anyString()))
        .thenReturn("prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2-codex", "raw-output", "{}"),
            List.of()
        )
    );
    when(codeOutputParser.parse(eq("raw-output"))).thenReturn(
        new CodeOutputParser.ParseResult(
            List.of(new GeneratedFile("src/A.java", "class A {}")),
            ParseSource.JSON
        )
    );
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), eq(false))).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/A.java", "DRY_RUN", "ok")))
    );
    when(codeDocChainService.runChain(eq("run-review"), eq(request), any(CodeGenerateResponse.class), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(null);

    ReviewGenerateResponse reviewResponse = new ReviewGenerateResponse(
        "review-run-1",
        "p1",
        tempDir.toString(),
        null,
        "anthropic",
        "claude-sonnet-4.5",
        new ObjectMapper().createObjectNode().put("summary", "Looks good"),
        List.of(),
        List.of(),
        "summary",
        "run-review"
    );
    when(codeReviewChainService.runChain(eq("run-review"), eq(request), any(CodeGenerateResponse.class), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(reviewResponse);
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    CodeGenerateResponse response = service.generate(request);

    assertThat(response.chainedReviewResult()).isNotNull();
    assertThat(response.chainedReviewResult().runId()).isEqualTo("review-run-1");
    assertThat(response.chainFailures()).isEmpty();
    verify(codeReviewChainService).runChain(eq("run-review"), eq(request), any(CodeGenerateResponse.class), eq(tempDir.toAbsolutePath().normalize()));
  }

  @Test
  void generateKeepsFailFastAsDefaultWhenDocChainFails(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setUserRequest("implement");
    request.setChainToDoc(true);

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), eq("implement")))
        .thenReturn("run-fail-fast");
    when(contextManager.buildCodeContext(eq("implement"), eq("p1"), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("code"), eq(tempDir.toAbsolutePath().normalize()), eq("implement"), anyString()))
        .thenReturn("prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2-codex", "raw-output", "{}"),
            List.of()
        )
    );
    when(codeOutputParser.parse(eq("raw-output"))).thenReturn(
        new CodeOutputParser.ParseResult(
            List.of(new GeneratedFile("src/A.java", "class A {}")),
            ParseSource.JSON
        )
    );
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), eq(false))).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/A.java", "DRY_RUN", "ok")))
    );
    when(codeDocChainService.runChain(
        eq("run-fail-fast"),
        eq(request),
        any(CodeGenerateResponse.class),
        eq(tempDir.toAbsolutePath().normalize())
    )).thenThrow(new IllegalStateException("doc failure"));

    assertThatThrownBy(() -> service.generate(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("doc failure");

    verify(runStateStore).appendEvent(eq("run-fail-fast"), eq("CODE_FAILED"), contains("doc failure"));
    verify(runStateStore).completeFailure(eq("run-fail-fast"), eq("p1"), eq("doc failure"));
  }

  @Test
  void generateReturnsPartialSuccessWhenChainPolicyIsPartialSuccess(@TempDir Path tempDir) {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeOutputParser codeOutputParser = Mockito.mock(CodeOutputParser.class);
    FileApplyService fileApplyService = Mockito.mock(FileApplyService.class);
    CodeDocChainService codeDocChainService = Mockito.mock(CodeDocChainService.class);
    CodeReviewChainService codeReviewChainService = Mockito.mock(CodeReviewChainService.class);

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService,
        codeDocChainService,
        codeReviewChainService
    );

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(tempDir.toString());
    request.setUserRequest("implement");
    request.setChainToDoc(true);
    request.setChainToReview(true);
    request.setChainFailurePolicy(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("CODE"), eq("BALANCED"), eq("implement")))
        .thenReturn("run-partial");
    when(contextManager.buildCodeContext(eq("implement"), eq("p1"), eq(tempDir.toAbsolutePath().normalize())))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("code"), eq(tempDir.toAbsolutePath().normalize()), eq("implement"), anyString()))
        .thenReturn("prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2-codex", "raw-output", "{}"),
            List.of()
        )
    );
    when(codeOutputParser.parse(eq("raw-output"))).thenReturn(
        new CodeOutputParser.ParseResult(
            List.of(new GeneratedFile("src/A.java", "class A {}")),
            ParseSource.JSON
        )
    );
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), eq(false))).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/A.java", "DRY_RUN", "ok")))
    );
    when(codeDocChainService.runChain(
        eq("run-partial"),
        eq(request),
        any(CodeGenerateResponse.class),
        eq(tempDir.toAbsolutePath().normalize())
    )).thenThrow(new IllegalStateException("doc failure"));

    ReviewGenerateResponse reviewResponse = new ReviewGenerateResponse(
        "review-run-1",
        "p1",
        tempDir.toString(),
        null,
        "anthropic",
        "claude-sonnet-4.5",
        new ObjectMapper().createObjectNode().put("summary", "Looks good"),
        List.of(),
        List.of(),
        "summary",
        "run-partial"
    );
    when(codeReviewChainService.runChain(
        eq("run-partial"),
        eq(request),
        any(CodeGenerateResponse.class),
        eq(tempDir.toAbsolutePath().normalize())
    )).thenReturn(reviewResponse);
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    CodeGenerateResponse response = service.generate(request);

    assertThat(response.chainedDocResult()).isNull();
    assertThat(response.chainedReviewResult()).isNotNull();
    assertThat(response.chainedReviewResult().runId()).isEqualTo("review-run-1");
    assertThat(response.chainFailures()).hasSize(1);
    assertThat(response.chainFailures().get(0).agent()).isEqualTo("DOC");
    assertThat(response.chainFailures().get(0).failedStage()).isEqualTo("CHAIN_DOC");
    assertThat(response.chainFailures().get(0).errorMessage()).contains("doc failure");
    verify(runStateStore, never()).completeFailure(anyString(), anyString(), anyString());
  }
}
