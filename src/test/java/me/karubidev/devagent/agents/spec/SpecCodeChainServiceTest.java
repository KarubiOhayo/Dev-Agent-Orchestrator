package me.karubidev.devagent.agents.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
  void runChainSkipsWhenChainToCodeIsDisabled(@TempDir Path tempDir) {
    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    SpecCodeChainService service = new SpecCodeChainService(codeAgentService, runStateStore, new ObjectMapper());

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId("p1");
    request.setChainToCode(false);

    ObjectNode spec = new ObjectMapper().createObjectNode();
    spec.put("title", "Test Spec");

    CodeGenerateResponse result = service.runChain("spec-run-disabled", request, spec, tempDir);

    assertThat(result).isNull();
    verify(codeAgentService, never()).generate(any());
    verify(runStateStore, never()).appendEvent(eq("spec-run-disabled"), anyString(), anyString());
  }

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
    request.setCodeChainToDoc(true);
    request.setCodeDocUserRequest("코드 기준 문서를 생성해줘");
    request.setCodeChainToReview(true);
    request.setCodeReviewUserRequest("보안 관점 리뷰를 생성해줘");
    request.setCodeChainFailurePolicy(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);

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
        List.of(),
        new FileApplyResult(true, 0, 0, 0, List.of()),
        null,
        null
    );
    when(codeAgentService.generate(Mockito.any(CodeGenerateRequest.class))).thenReturn(codeResponse);

    CodeGenerateResponse result = service.runChain("spec-run-1", request, spec, tempDir);

    assertThat(result.runId()).isEqualTo("code-run-1");

    ArgumentCaptor<CodeGenerateRequest> requestCaptor = ArgumentCaptor.forClass(CodeGenerateRequest.class);
    verify(codeAgentService).generate(requestCaptor.capture());
    CodeGenerateRequest chainedRequest = requestCaptor.getValue();
    assertThat(chainedRequest.getSpecInputPath()).isNotBlank();
    assertThat(Path.of(chainedRequest.getSpecInputPath()).isAbsolute()).isFalse();
    assertThat(Files.exists(tempDir.resolve(chainedRequest.getSpecInputPath()))).isTrue();
    assertThat(chainedRequest.isChainToDoc()).isTrue();
    assertThat(chainedRequest.getDocUserRequest()).isEqualTo("코드 기준 문서를 생성해줘");
    assertThat(chainedRequest.isChainToReview()).isTrue();
    assertThat(chainedRequest.getReviewUserRequest()).isEqualTo("보안 관점 리뷰를 생성해줘");
    assertThat(chainedRequest.getChainFailurePolicy()).isEqualTo(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);

    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("CHAIN_SPEC_WRITTEN"), contains(".json"));
    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("CHAIN_CODE_TRIGGERED"), contains(".json"));
    verify(runStateStore).appendEvent(eq("spec-run-1"), eq("CHAIN_CODE_DONE"), eq("codeRunId=code-run-1"));
  }

  @Test
  void chainRejectsAbsoluteSpecOutputPath(@TempDir Path tempDir) {
    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    SpecCodeChainService service = new SpecCodeChainService(codeAgentService, runStateStore, new ObjectMapper());

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId("p1");
    request.setChainToCode(true);
    request.setSpecOutputPath(tempDir.resolve("spec.json").toString());

    ObjectNode spec = new ObjectMapper().createObjectNode();
    spec.put("title", "Test");

    assertThatThrownBy(() -> service.runChain("spec-run-abs", request, spec, tempDir))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("relative path");

    verify(runStateStore).appendEvent(eq("spec-run-abs"), eq("CHAIN_CODE_FAILED"), contains("relative path"));
    verify(codeAgentService, never()).generate(any());
  }

  @Test
  void chainRejectsTraversalSpecOutputPath(@TempDir Path tempDir) {
    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    SpecCodeChainService service = new SpecCodeChainService(codeAgentService, runStateStore, new ObjectMapper());

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId("p1");
    request.setChainToCode(true);
    request.setSpecOutputPath("../escape/spec.json");

    ObjectNode spec = new ObjectMapper().createObjectNode();
    spec.put("title", "Test");

    assertThatThrownBy(() -> service.runChain("spec-run-up", request, spec, tempDir))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not contain '..'");

    verify(runStateStore).appendEvent(eq("spec-run-up"), eq("CHAIN_CODE_FAILED"), contains("must not contain '..'"));
    verify(codeAgentService, never()).generate(any());
  }

  @Test
  void chainRejectsOverwriteWhenSpecFileExists(@TempDir Path tempDir) throws Exception {
    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    SpecCodeChainService service = new SpecCodeChainService(codeAgentService, runStateStore, new ObjectMapper());

    Path existingSpec = tempDir.resolve(".devagent/specs/spec-run-exists.json");
    Files.createDirectories(existingSpec.getParent());
    Files.writeString(existingSpec, "{\"title\":\"existing\"}");

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId("p1");
    request.setChainToCode(true);

    ObjectNode spec = new ObjectMapper().createObjectNode();
    spec.put("title", "New");

    assertThatThrownBy(() -> service.runChain("spec-run-exists", request, spec, tempDir))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("overwrite disabled");

    verify(runStateStore).appendEvent(eq("spec-run-exists"), eq("CHAIN_CODE_FAILED"), contains("overwrite disabled"));
    verify(codeAgentService, never()).generate(any());
  }
}
