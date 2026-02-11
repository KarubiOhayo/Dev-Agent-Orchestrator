package me.karubidev.devagent.agents.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.llm.LlmAttempt;
import me.karubidev.devagent.orchestration.routing.AgentType;
import me.karubidev.devagent.orchestration.routing.ModelRef;
import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import me.karubidev.devagent.orchestration.routing.RoutingMode;
import me.karubidev.devagent.state.RunStateStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class SpecCodeChainServiceTest {

  @Test
  void chainRunRecordsRunStateEvents(@TempDir Path tempDir) {
    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    SpecCodeChainService service = new SpecCodeChainService(codeAgentService, runStateStore, new ObjectMapper());

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId("p1");
    request.setUserRequest("명세 생성");
    request.setChainToCode(true);
    request.setCodeApply(false);

    ObjectNode spec = new ObjectMapper().createObjectNode();
    spec.put("title", "Test Spec");
    spec.putArray("tasks");

    CodeGenerateResponse codeResponse = new CodeGenerateResponse(
        "code-run-1",
        "p1",
        tempDir.toString(),
        new RouteDecision(
            AgentType.CODE,
            RoutingMode.BALANCED,
            RiskLevel.MEDIUM,
            new ModelRef("openai", "gpt-5.2-codex"),
            List.of(),
            List.of("test")
        ),
        "openai",
        "gpt-5.2-codex",
        "output",
        List.of(new LlmAttempt("openai", "gpt-5.2-codex", true, "ok")),
        List.of(),
        "summary",
        new FileApplyResult(true, 0, 0, 0, List.of())
    );
    when(codeAgentService.generate(Mockito.any(CodeGenerateRequest.class))).thenReturn(codeResponse);

    CodeGenerateResponse result = service.runChain("spec-run-1", request, spec, tempDir);

    assertThat(result.runId()).isEqualTo("code-run-1");

    ArgumentCaptor<CodeGenerateRequest> requestCaptor = ArgumentCaptor.forClass(CodeGenerateRequest.class);
    verify(codeAgentService).generate(requestCaptor.capture());
    CodeGenerateRequest chainedRequest = requestCaptor.getValue();
    assertThat(chainedRequest.getSpecInputPath()).isNotBlank();
    assertThat(Files.exists(Path.of(chainedRequest.getSpecInputPath()))).isTrue();

    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("CHAIN_SPEC_WRITTEN"), contains(".json"));
    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("CHAIN_CODE_TRIGGERED"), contains(".json"));
    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("CHAIN_CODE_DONE"), eq("codeRunId=code-run-1"));
  }
}
