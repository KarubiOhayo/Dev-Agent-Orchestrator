package me.karubidev.devagent.agents.review;

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

class CodeReviewChainServiceTest {

  @Test
  void chainRunRecordsRunStateEvents(@TempDir Path tempDir) {
    ReviewAgentService reviewAgentService = Mockito.mock(ReviewAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeReviewChainService service = new CodeReviewChainService(reviewAgentService, runStateStore);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setChainToReview(true);
    request.setReviewUserRequest("코드 품질 리뷰");

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

    ObjectNode reviewNode = new ObjectMapper().createObjectNode();
    reviewNode.put("summary", "ok");
    ReviewGenerateResponse reviewResponse = new ReviewGenerateResponse(
        "review-run-1",
        "p1",
        tempDir.toString(),
        null,
        "anthropic",
        "claude-sonnet-4-5-20250929",
        reviewNode,
        List.of(),
        List.of(),
        "summary",
        "code-run-1"
    );
    when(reviewAgentService.generate(any(ReviewGenerateRequest.class))).thenReturn(reviewResponse);

    ReviewGenerateResponse result = service.runChain("code-run-1", request, codeResponse, tempDir);

    assertThat(result.runId()).isEqualTo("review-run-1");

    ArgumentCaptor<ReviewGenerateRequest> requestCaptor = ArgumentCaptor.forClass(ReviewGenerateRequest.class);
    verify(reviewAgentService).generate(requestCaptor.capture());
    ReviewGenerateRequest chainedRequest = requestCaptor.getValue();
    assertThat(chainedRequest.getCodeRunId()).isEqualTo("code-run-1");
    assertThat(chainedRequest.getCodeFiles()).hasSize(1);
    assertThat(chainedRequest.getUserRequest()).isEqualTo("코드 품질 리뷰");

    verify(runStateStore).appendEvent(eq("code-run-1"), eq("CHAIN_REVIEW_TRIGGERED"), contains("files=1"));
    verify(runStateStore).appendEvent(eq("code-run-1"), eq("CHAIN_REVIEW_DONE"), eq("reviewRunId=review-run-1"));
  }

  @Test
  void chainSkipsWhenDisabled(@TempDir Path tempDir) {
    ReviewAgentService reviewAgentService = Mockito.mock(ReviewAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeReviewChainService service = new CodeReviewChainService(reviewAgentService, runStateStore);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setChainToReview(false);

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

    ReviewGenerateResponse result = service.runChain("code-run-1", request, codeResponse, tempDir);

    assertThat(result).isNull();
    verify(reviewAgentService, never()).generate(any());
  }

  @Test
  void chainRecordsFailureEventWhenReviewGenerationFails(@TempDir Path tempDir) {
    ReviewAgentService reviewAgentService = Mockito.mock(ReviewAgentService.class);
    RunStateStore runStateStore = Mockito.mock(RunStateStore.class);
    CodeReviewChainService service = new CodeReviewChainService(reviewAgentService, runStateStore);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId("p1");
    request.setChainToReview(true);

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

    when(reviewAgentService.generate(any(ReviewGenerateRequest.class)))
        .thenThrow(new IllegalStateException("review failure"));

    assertThatThrownBy(() -> service.runChain("code-run-1", request, codeResponse, tempDir))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("review failure");

    verify(runStateStore).appendEvent(eq("code-run-1"), eq("CHAIN_REVIEW_FAILED"), contains("review failure"));
  }
}
