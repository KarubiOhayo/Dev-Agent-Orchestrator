package me.karubidev.devagent.agents.doc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
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

class CodeDocChainServiceTest {

  @Test
  void chainRunRecordsRunStateEvents(@TempDir Path tempDir) {
    DocAgentService docAgentService = Mockito.mock(DocAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeDocChainService service = new CodeDocChainService(docAgentService, runStateStore);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setChainToDoc(true);
    request.setDocUserRequest("코드 결과 문서화");

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
        "raw-output",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/AuthController.java", "class AuthController {}")),
        new FileApplyResult(true, 1, 0, 0, List.of()),
        null,
        null
    );

    ObjectNode docNode = new ObjectMapper().createObjectNode();
    docNode.put("title", "Auth Docs");
    DocGenerateResponse docResponse = new DocGenerateResponse(
        "doc-run-1",
        "p1",
        tempDir.toString(),
        null,
        "openai",
        "gpt-5.2",
        docNode,
        List.of(),
        List.of(),
        "summary",
        "code-run-1"
    );
    when(docAgentService.generate(any(DocGenerateRequest.class))).thenReturn(docResponse);

    DocGenerateResponse result = service.runChain("code-run-1", request, codeResponse, tempDir);

    assertThat(result.runId()).isEqualTo("doc-run-1");

    ArgumentCaptor<DocGenerateRequest> requestCaptor = ArgumentCaptor.forClass(DocGenerateRequest.class);
    verify(docAgentService).generate(requestCaptor.capture());
    DocGenerateRequest chainedRequest = requestCaptor.getValue();
    assertThat(chainedRequest.getCodeRunId()).isEqualTo("code-run-1");
    assertThat(chainedRequest.getCodeFiles()).hasSize(1);
    assertThat(chainedRequest.getUserRequest()).isEqualTo("코드 결과 문서화");

    verify(runStateStore).appendEvent(eq("code-run-1"), eq("CHAIN_DOC_TRIGGERED"), contains("files=1"));
    verify(runStateStore).appendEvent(eq("code-run-1"), eq("CHAIN_DOC_DONE"), eq("docRunId=doc-run-1"));
  }

  @Test
  void chainSkipsWhenDisabled(@TempDir Path tempDir) {
    DocAgentService docAgentService = Mockito.mock(DocAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeDocChainService service = new CodeDocChainService(docAgentService, runStateStore);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setChainToDoc(false);

    CodeGenerateResponse codeResponse = new CodeGenerateResponse(
        "code-run-1",
        "p1",
        tempDir.toString(),
        null,
        "openai",
        "gpt-5.2-codex",
        "raw-output",
        List.of(),
        List.of(),
        "summary",
        List.of(),
        new FileApplyResult(true, 0, 0, 0, List.of()),
        null,
        null
    );

    DocGenerateResponse result = service.runChain("code-run-1", request, codeResponse, tempDir);

    assertThat(result).isNull();
    verify(docAgentService, never()).generate(any());
  }

  @Test
  void chainRecordsFailureEventWhenDocGenerationFails(@TempDir Path tempDir) {
    DocAgentService docAgentService = Mockito.mock(DocAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeDocChainService service = new CodeDocChainService(docAgentService, runStateStore);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setChainToDoc(true);

    CodeGenerateResponse codeResponse = new CodeGenerateResponse(
        "code-run-1",
        "p1",
        tempDir.toString(),
        null,
        "openai",
        "gpt-5.2-codex",
        "raw-output",
        List.of(),
        List.of(),
        "summary",
        List.of(),
        new FileApplyResult(true, 0, 0, 0, List.of()),
        null,
        null
    );

    when(docAgentService.generate(any(DocGenerateRequest.class))).thenThrow(new IllegalStateException("doc failure"));

    assertThatThrownBy(() -> service.runChain("code-run-1", request, codeResponse, tempDir))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("doc failure");

    verify(runStateStore).appendEvent(eq("code-run-1"), eq("CHAIN_DOC_FAILED"), contains("doc failure"));
  }
}
