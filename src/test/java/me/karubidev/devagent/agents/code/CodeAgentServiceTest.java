package me.karubidev.devagent.agents.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.apply.CodeOutputParser;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.FileApplyService;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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

    CodeAgentService service = new CodeAgentService(
        modelRouter,
        llmOrchestrator,
        contextManager,
        promptRegistry,
        runStateStore,
        codeOutputParser,
        fileApplyService
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
    when(codeOutputParser.parseFiles(eq("raw-output"))).thenReturn(List.of(new GeneratedFile("src/A.java", "class A {}")));
    when(fileApplyService.apply(eq(tempDir.toAbsolutePath().normalize()), any(), eq(true), anyBoolean())).thenReturn(
        new FileApplyResult(true, 1, 0, 0, List.of(new FileApplyItem("src/A.java", "DRY_RUN", "ok")))
    );
    when(runStateStore.getProjectSummary("p1")).thenReturn("summary");

    CodeGenerateResponse response = service.generate(request);

    assertThat(response.runId()).isEqualTo("run-1");
    verify(runStateStore).appendEvent(eq("run-1"), eq("SPEC_INPUT_LOADED"), contains("spec.json"));

    ArgumentCaptor<String> userRequestCaptor = ArgumentCaptor.forClass(String.class);
    verify(contextManager).buildCodeContext(userRequestCaptor.capture(), eq("p1"), eq(tempDir.toAbsolutePath().normalize()));
    assertThat(userRequestCaptor.getValue()).contains("[SPEC_INPUT_PATH]");
  }
}
