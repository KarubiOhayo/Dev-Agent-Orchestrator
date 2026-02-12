package me.karubidev.devagent.agents.doc;

import java.util.List;
import me.karubidev.devagent.llm.LlmAttempt;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import tools.jackson.databind.JsonNode;

public record DocGenerateResponse(
    String runId,
    String projectId,
    String targetProjectRoot,
    RouteDecision routeDecision,
    String usedProvider,
    String usedModel,
    JsonNode document,
    List<LlmAttempt> attempts,
    List<String> referencedContextFiles,
    String projectSummary,
    String sourceCodeRunId
) {
}
