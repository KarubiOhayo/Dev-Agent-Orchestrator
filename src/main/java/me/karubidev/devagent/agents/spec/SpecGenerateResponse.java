package me.karubidev.devagent.agents.spec;

import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.llm.LlmAttempt;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import tools.jackson.databind.JsonNode;

public record SpecGenerateResponse(
    String runId,
    String projectId,
    String targetProjectRoot,
    RouteDecision routeDecision,
    String usedProvider,
    String usedModel,
    JsonNode spec,
    List<LlmAttempt> attempts,
    List<String> referencedContextFiles,
    String projectSummary,
    CodeGenerateResponse chainedCodeResult
) {
}
