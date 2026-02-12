package me.karubidev.devagent.agents.review;

import java.util.List;
import me.karubidev.devagent.llm.LlmAttempt;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import tools.jackson.databind.JsonNode;

public record ReviewGenerateResponse(
    String runId,
    String projectId,
    String targetProjectRoot,
    RouteDecision routeDecision,
    String usedProvider,
    String usedModel,
    JsonNode review,
    List<LlmAttempt> attempts,
    List<String> referencedContextFiles,
    String projectSummary,
    String sourceCodeRunId
) {
}
