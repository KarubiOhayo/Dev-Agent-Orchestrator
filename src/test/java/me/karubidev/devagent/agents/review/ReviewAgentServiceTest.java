package me.karubidev.devagent.agents.review;

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

class ReviewAgentServiceTest {

  @Test
  void generateReturnsStructuredReviewJson() {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    ReviewOutputSchemaParser parser = new ReviewOutputSchemaParser(new ObjectMapper());

    ReviewAgentService service = new ReviewAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        parser
    );

    ReviewGenerateRequest request = new ReviewGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(".");
    request.setUserRequest("인증 코드 리뷰");
    request.setCodeRunId("code-run-1");
    request.setCodeFiles(List.of(new GeneratedFile("src/AuthController.java", "class AuthController {}")));

    RouteDecision routeDecision = new RouteDecision(
        AgentType.REVIEW,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("anthropic", "claude-sonnet-4.5"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("REVIEW"), eq("BALANCED"), anyString())).thenReturn("review-run-1");
    when(contextManager.buildCodeContext(eq("인증 코드 리뷰"), eq("p1"), any(Path.class)))
        .thenReturn(new ContextBundle("ctx", List.of("docs/rules/review-style.md")));
    when(promptRegistry.buildPrompt(eq("review"), any(Path.class), contains("Create a strict JSON code review output"), contains("ctx")))
        .thenReturn("review-prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("review-prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult(
                "anthropic",
                "claude-sonnet-4.5",
                """
                    {"summary":"리뷰 요약","overallRisk":"MEDIUM","findings":[{"title":"입력 검증 누락","severity":"HIGH","file":"src/AuthController.java","line":42,"description":"null 체크 누락","suggestion":"validation 추가"}],"strengths":["구조 명확"],"nextActions":["검증 로직 추가"]}
                    """,
                "{}"
            ),
            List.of()
        )
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    ReviewGenerateResponse response = service.generate(request);

    assertThat(response.runId()).isEqualTo("review-run-1");
    assertThat(response.review().path("overallRisk").asText()).isEqualTo("MEDIUM");
    assertThat(response.review().path("findings").isArray()).isTrue();
    verify(runStateStore).appendEvent(eq("review-run-1"), eq("REVIEW_SCHEMA_READY"), contains("findings="));
    verify(runStateStore).appendEvent(eq("review-run-1"), eq("REVIEW_LLM_DONE"), contains("anthropic:claude-sonnet-4.5"));
  }

  @Test
  void generateRecordsFallbackWarningEventWhenJsonParsingFails() {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    ReviewOutputSchemaParser parser = new ReviewOutputSchemaParser(new ObjectMapper());

    ReviewAgentService service = new ReviewAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        parser
    );

    ReviewGenerateRequest request = new ReviewGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(".");
    request.setUserRequest("리뷰 생성");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.REVIEW,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("anthropic", "claude-sonnet-4.5"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("REVIEW"), eq("BALANCED"), anyString())).thenReturn("review-run-fallback");
    when(contextManager.buildCodeContext(eq("리뷰 생성"), eq("p1"), any(Path.class)))
        .thenReturn(new ContextBundle("ctx", List.of()));
    when(promptRegistry.buildPrompt(eq("review"), any(Path.class), contains("Create a strict JSON code review output"), anyString()))
        .thenReturn("review-prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("review-prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult("anthropic", "claude-sonnet-4.5", "리뷰 본문입니다.", "{}"),
            List.of()
        )
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    ReviewGenerateResponse response = service.generate(request);

    assertThat(response.review().path("findings").isArray()).isTrue();
    verify(runStateStore).appendEvent(eq("review-run-fallback"), eq("REVIEW_OUTPUT_FALLBACK_WARNING"), contains("FALLBACK"));
  }
}
