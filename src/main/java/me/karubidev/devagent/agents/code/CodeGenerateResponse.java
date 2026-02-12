package me.karubidev.devagent.agents.code;

import java.util.List;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.doc.DocGenerateResponse;
import me.karubidev.devagent.agents.review.ReviewGenerateResponse;
import me.karubidev.devagent.llm.LlmAttempt;
import me.karubidev.devagent.orchestration.routing.RouteDecision;

public record CodeGenerateResponse(
    String runId,
    String projectId,
    String targetProjectRoot,
    RouteDecision routeDecision,
    String usedProvider,
    String usedModel,
    String output,
    List<LlmAttempt> attempts,
    List<String> referencedContextFiles,
    String projectSummary,
    List<GeneratedFile> files,
    FileApplyResult applyResult,
    DocGenerateResponse chainedDocResult,
    ReviewGenerateResponse chainedReviewResult
) {
}
