package me.karubidev.devagent.agents.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
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

class SpecAgentServiceTest {

  @Test
  void generateReturnsJsonSchema() {
    ModelRouter modelRouter = Mockito.mock(ModelRouter.class);
    LlmOrchestratorService llmOrchestrator = Mockito.mock(LlmOrchestratorService.class);
    ProjectContextManager contextManager = Mockito.mock(ProjectContextManager.class);
    PromptRegistry promptRegistry = Mockito.mock(PromptRegistry.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    SpecCodeChainService specCodeChainService = Mockito.mock(SpecCodeChainService.class);
    SpecOutputSchemaParser schemaParser = new SpecOutputSchemaParser(new ObjectMapper());

    SpecAgentService service = new SpecAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        schemaParser,
        specCodeChainService
    );

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId("p1");
    request.setTargetProjectRoot(".");
    request.setUserRequest("로그인 API 명세 작성");

    RouteDecision routeDecision = new RouteDecision(
        AgentType.SPEC,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2"),
        List.of(),
        List.of("test")
    );
    when(modelRouter.resolve(any())).thenReturn(routeDecision);
    when(runStateStore.startRun(eq("p1"), eq("SPEC"), eq("BALANCED"), anyString())).thenReturn("spec-run-1");
    when(contextManager.buildCodeContext(eq("로그인 API 명세 작성"), eq("p1"), any(Path.class)))
        .thenReturn(new ContextBundle("ctx", List.of("docs/rules/auth.md")));
    when(promptRegistry.buildPrompt(eq("spec"), any(Path.class), contains("Create a strict JSON specification"), eq("ctx")))
        .thenReturn("spec-prompt");
    when(llmOrchestrator.generate(eq(routeDecision), eq("spec-prompt"))).thenReturn(
        new LlmExecutionResult(
            new LlmGenerationResult(
                "openai",
                "gpt-5.2",
                "{\"title\":\"Login\",\"overview\":\"Auth spec\",\"tasks\":[]}",
                "{}"
            ),
            List.of()
        )
    );
    when(specCodeChainService.runChain(eq("spec-run-1"), eq(request), any(), any(Path.class))).thenReturn(null);
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    SpecGenerateResponse response = service.generate(request);

    assertThat(response.runId()).isEqualTo("spec-run-1");
    assertThat(response.spec().path("title").asText()).isEqualTo("Login");
    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("SPEC_SCHEMA_READY"), contains("keys="));
  }
}
