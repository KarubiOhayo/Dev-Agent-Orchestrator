package me.karubidev.devagent.agents.doc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
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
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

class DocAgentServiceTest {

  @Test
  void generateReturnsJsonSchema() {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    DocOutputSchemaParser parser = new DocOutputSchemaParser(new ObjectMapper());

    DocAgentService service = new DocAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        parser
    );

    DocGenerateRequest request = new DocGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(".");
    request.setUserRequest("로그인 API 문서 생성");
    request.setCodeRunId("code-run-1");
    request.setCodeFiles(List.of(new GeneratedFile("src/AuthController.java", "class AuthController {}")));

    RouteDecision routeDecision = new RouteDecision(
        AgentType.DOC,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("DOC"), eq("BALANCED"), anyString())).thenReturn("doc-run-1");
    when(contextManager.buildCodeContext(eq("로그인 API 문서 생성"), eq("p1"), any(Path.class)))
        .thenReturn(new ContextBundle("ctx", List.of("docs/rules/doc-style.md")));
    when(promptRegistry.buildPrompt(eq("doc"), any(Path.class), contains("Create a strict JSON documentation output"), contains("ctx")))
        .thenReturn("doc-prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("doc-prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult(
                "openai",
                "gpt-5.2",
                """
                    {"title":"Login API","summary":"Auth docs","sections":[{"heading":"Endpoints","content":"POST /login"}],"relatedFiles":["src/AuthController.java"],"notes":[]}
                    """,
                "{}"
            ),
            List.of()
        )
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    DocGenerateResponse response = service.generate(request);

    assertThat(response.runId()).isEqualTo("doc-run-1");
    assertThat(response.document().path("title").asText()).isEqualTo("Login API");
    verify(runStateStore).appendEvent(eq("doc-run-1"), eq("DOC_SCHEMA_READY"), contains("keys="));
    verify(runStateStore).appendEvent(eq("doc-run-1"), eq("DOC_LLM_DONE"), contains("openai:gpt-5.2"));
  }

  @Test
  void generateRecordsFallbackWarningEventWhenJsonParsingFails() {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    DocOutputSchemaParser parser = new DocOutputSchemaParser(new ObjectMapper());

    DocAgentService service = new DocAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        parser
    );

    DocGenerateRequest request = new DocGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(".");
    request.setUserRequest("문서 생성");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.DOC,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("DOC"), eq("BALANCED"), anyString())).thenReturn("doc-run-fallback");
    when(contextManager.buildCodeContext(eq("문서 생성"), eq("p1"), any(Path.class)))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("doc"), any(Path.class), contains("Create a strict JSON documentation output"), anyString()))
        .thenReturn("doc-prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("doc-prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("openai", "gpt-5.2", "요약 본문입니다.", "{}"),
            List.of()
        )
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    DocGenerateResponse response = service.generate(request);

    assertThat(response.document().path("sections").isArray()).isTrue();
    verify(runStateStore).appendEvent(eq("doc-run-fallback"), eq("DOC_OUTPUT_FALLBACK_WARNING"), contains("FALLBACK"));
  }
}
