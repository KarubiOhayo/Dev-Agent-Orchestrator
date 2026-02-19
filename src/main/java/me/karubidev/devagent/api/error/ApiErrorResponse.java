package me.karubidev.devagent.api.error;

import java.util.List;

public record ApiErrorResponse(
    String code,
    String message,
    String path,
    String timestamp,
    List<ApiErrorDetail> details
) {
  public ApiErrorResponse {
    details = details == null ? List.of() : List.copyOf(details);
  }
}
