package me.karubidev.devagent.llm;

public record LlmAttempt(String provider, String model, boolean success, String message) {
}
