package me.karubidev.devagent.llm;

import java.util.List;

public record LlmExecutionResult(LlmGenerationResult result, List<LlmAttempt> attempts) {
}
