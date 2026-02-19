package me.karubidev.devagent.api.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Pattern REQUIRED_FIELD_PATTERN = Pattern.compile("^(.+) is required$");
  private static final Pattern CAMEL_CASE_FIELD_PATTERN = Pattern.compile("([a-z]+[A-Z][A-Za-z0-9]*)");
  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("([A-Za-z][A-Za-z0-9]*)");

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex,
      HttpServletRequest request
  ) {
    String message = normalizedMessage(ex.getMessage(), "Invalid request");
    String code = resolveIllegalArgumentCode(message);
    List<ApiErrorDetail> details = resolveIllegalArgumentDetails(message);
    return buildResponse(HttpStatus.BAD_REQUEST, code, message, request, details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleNotReadable(
      HttpMessageNotReadableException ex,
      HttpServletRequest request
  ) {
    Throwable root = rootCause(ex);
    String rootMessage = normalizedMessage(root.getMessage(), "");

    if (rootMessage.contains("Required request body is missing")) {
      return buildResponse(
          HttpStatus.BAD_REQUEST,
          "REQUEST_BODY_REQUIRED",
          "Request body is required",
          request,
          List.of()
      );
    }

    if (isMalformedJsonSyntax(root, rootMessage)) {
      return buildResponse(
          HttpStatus.BAD_REQUEST,
          "MALFORMED_JSON",
          "Malformed JSON request body",
          request,
          List.of()
      );
    }

    if (root instanceof InvalidFormatException invalidFormat && isEnumTarget(invalidFormat)) {
      return buildResponse(
          HttpStatus.BAD_REQUEST,
          "INVALID_ENUM_VALUE",
          "Invalid enum value",
          request,
          List.of(buildEnumDetail(invalidFormat))
      );
    }

    if (root instanceof MismatchedInputException mismatchedInputException) {
      return buildResponse(
          HttpStatus.BAD_REQUEST,
          "INVALID_JSON_VALUE",
          "Invalid JSON request body",
          request,
          List.of(buildMismatchedInputDetail(mismatchedInputException))
      );
    }

    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "INVALID_JSON_REQUEST",
        "Invalid JSON request body",
        request,
        List.of()
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception ex,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_SERVER_ERROR",
        "Internal server error",
        request,
        List.of()
    );
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status,
      String code,
      String message,
      HttpServletRequest request,
      List<ApiErrorDetail> details
  ) {
    ApiErrorResponse body = new ApiErrorResponse(
        code,
        message,
        request.getRequestURI(),
        Instant.now().toString(),
        details
    );
    return ResponseEntity.status(status).body(body);
  }

  private String resolveIllegalArgumentCode(String message) {
    Matcher matcher = REQUIRED_FIELD_PATTERN.matcher(message);
    if (matcher.matches()) {
      if (isAnyOfRequiredExpression(matcher.group(1))) {
        return "MISSING_REQUIRED_ANY_OF";
      }
      return "MISSING_REQUIRED_FIELD";
    }
    return "INVALID_ARGUMENT";
  }

  private List<ApiErrorDetail> resolveIllegalArgumentDetails(String message) {
    Matcher matcher = REQUIRED_FIELD_PATTERN.matcher(message);
    if (!matcher.matches()) {
      return List.of();
    }
    String rawFieldExpression = matcher.group(1);
    if (isAnyOfRequiredExpression(rawFieldExpression)) {
      return buildAnyOfRequiredDetails(rawFieldExpression);
    }
    return List.of(new ApiErrorDetail(rawFieldExpression.trim(), "required", null));
  }

  private boolean isAnyOfRequiredExpression(String rawFieldExpression) {
    return rawFieldExpression != null && rawFieldExpression.contains(" or ");
  }

  private List<ApiErrorDetail> buildAnyOfRequiredDetails(String rawFieldExpression) {
    return Arrays.stream(rawFieldExpression.split("\\s+or\\s+"))
        .map(this::normalizeAnyOfFieldName)
        .filter(field -> !field.isBlank())
        .distinct()
        .map(field -> new ApiErrorDetail(field, "any_of_required", null))
        .toList();
  }

  private String normalizeAnyOfFieldName(String fieldExpression) {
    if (fieldExpression == null || fieldExpression.isBlank()) {
      return "";
    }

    Matcher camelCaseMatcher = CAMEL_CASE_FIELD_PATTERN.matcher(fieldExpression);
    String candidate = "";
    while (camelCaseMatcher.find()) {
      candidate = camelCaseMatcher.group(1);
    }
    if (!candidate.isBlank()) {
      return candidate;
    }

    Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(fieldExpression);
    while (identifierMatcher.find()) {
      candidate = identifierMatcher.group(1);
    }
    return candidate;
  }

  private boolean isEnumTarget(InvalidFormatException invalidFormatException) {
    Class<?> targetType = invalidFormatException.getTargetType();
    return targetType != null && targetType.isEnum();
  }

  private ApiErrorDetail buildEnumDetail(InvalidFormatException invalidFormatException) {
    String field = extractFieldFromPathReference(invalidFormatException.getPathReference());
    String rejectedValue = String.valueOf(invalidFormatException.getValue());
    String reason = "must be one of [" + allowedEnumValues(invalidFormatException.getTargetType()) + "]";
    return new ApiErrorDetail(field, reason, rejectedValue);
  }

  private ApiErrorDetail buildMismatchedInputDetail(MismatchedInputException ex) {
    String field = extractFieldFromPathReference(ex.getPathReference());
    return new ApiErrorDetail(field, "invalid type or value", null);
  }

  private String allowedEnumValues(Class<?> enumType) {
    if (enumType == null || !enumType.isEnum()) {
      return "";
    }
    return Arrays.stream(enumType.getEnumConstants())
        .map(String::valueOf)
        .collect(Collectors.joining(", "));
  }

  private String extractFieldFromPathReference(String pathReference) {
    if (pathReference == null || pathReference.isBlank()) {
      return "";
    }

    Matcher fieldMatcher = Pattern.compile("\\[\"([^\"]+)\"\\]").matcher(pathReference);
    String field = "";
    while (fieldMatcher.find()) {
      field = fieldMatcher.group(1);
    }
    if (!field.isBlank()) {
      return field;
    }

    Matcher indexMatcher = Pattern.compile("\\[(\\d+)\\]").matcher(pathReference);
    String index = "";
    while (indexMatcher.find()) {
      index = indexMatcher.group(1);
    }
    if (!index.isBlank()) {
      return "[" + index + "]";
    }

    return "";
  }

  private boolean isMalformedJsonSyntax(Throwable root, String rootMessage) {
    String className = root.getClass().getName();
    if (className.endsWith("JsonParseException") || className.endsWith("StreamReadException")) {
      return true;
    }
    return rootMessage.contains("Unexpected character")
        || rootMessage.contains("Unexpected end-of-input");
  }

  private Throwable rootCause(Throwable throwable) {
    Throwable current = throwable;
    while (current.getCause() != null && current.getCause() != current) {
      current = current.getCause();
    }
    return current;
  }

  private String normalizedMessage(String message, String fallback) {
    if (message == null || message.isBlank()) {
      return fallback;
    }
    return message;
  }
}
