package me.karubidev.devagent.agents.review;

import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.state.RunStateStore;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewChainService {

  private final ReviewAgentService reviewAgentService;
  private final RunStateStore runStateStore;

  public CodeReviewChainService(ReviewAgentService reviewAgentService, RunStateStore runStateStore) {
    this.reviewAgentService = reviewAgentService;
    this.runStateStore = runStateStore;
  }

  public ReviewGenerateResponse runChain(
      String codeRunId,
      CodeGenerateRequest request,
      CodeGenerateResponse codeResponse,
      Path targetRoot
  ) {
    if (!request.isChainToReview()) {
      return null;
    }

    try {
      Path safeTargetRoot = targetRoot.toAbsolutePath().normalize();
      ReviewGenerateRequest reviewRequest = new ReviewGenerateRequest();
      reviewRequest.setProjectId(request.getProjectId());
      reviewRequest.setTargetProjectRoot(safeTargetRoot.toString());
      reviewRequest.setUserRequest(resolveReviewRequest(request));
      reviewRequest.setMode(request.getMode());
      reviewRequest.setRiskLevel(request.getRiskLevel());
      reviewRequest.setLargeContext(request.isLargeContext());
      reviewRequest.setStrictJsonRequired(request.isStrictJsonRequired());
      reviewRequest.setCodeRunId(codeRunId);
      reviewRequest.setCodeOutput(codeResponse.output());

      List<GeneratedFile> files = codeResponse.files() == null ? List.of() : codeResponse.files();
      reviewRequest.setCodeFiles(files);

      runStateStore.appendEvent(codeRunId, "CHAIN_REVIEW_TRIGGERED", "files=" + files.size());
      ReviewGenerateResponse response = reviewAgentService.generate(reviewRequest);
      runStateStore.appendEvent(codeRunId, "CHAIN_REVIEW_DONE", "reviewRunId=" + response.runId());
      return response;
    } catch (Exception e) {
      runStateStore.appendEvent(codeRunId, "CHAIN_REVIEW_FAILED", "reason=" + errorMessage(e));
      throw e;
    }
  }

  private String resolveReviewRequest(CodeGenerateRequest request) {
    if (request.getReviewUserRequest() != null && !request.getReviewUserRequest().isBlank()) {
      return request.getReviewUserRequest();
    }
    return "Review the generated code and provide prioritized, actionable findings.";
  }

  private String errorMessage(Exception e) {
    if (e.getMessage() == null || e.getMessage().isBlank()) {
      return e.getClass().getSimpleName();
    }
    return e.getMessage();
  }
}
