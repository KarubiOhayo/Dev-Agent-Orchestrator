package me.karubidev.devagent.api.error;

public record ApiErrorDetail(
    String field,
    String reason,
    String rejectedValue
) {
}
