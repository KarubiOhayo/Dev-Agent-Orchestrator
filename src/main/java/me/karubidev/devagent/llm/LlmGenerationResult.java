package me.karubidev.devagent.llm;

public record LlmGenerationResult(String provider, String model, String text, String rawResponse) {
}
