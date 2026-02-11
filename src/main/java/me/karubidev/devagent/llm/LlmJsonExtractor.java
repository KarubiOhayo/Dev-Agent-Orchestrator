package me.karubidev.devagent.llm;

import tools.jackson.databind.JsonNode;

public final class LlmJsonExtractor {

  private LlmJsonExtractor() {
  }

  public static String openAiText(JsonNode root) {
    String outputText = text(root.path("output_text"));
    if (hasText(outputText)) {
      return outputText;
    }

    for (JsonNode item : root.path("output")) {
      for (JsonNode content : item.path("content")) {
        String text = text(content.path("text"));
        if (hasText(text)) {
          return text;
        }
      }
    }

    String chatText = text(root.path("choices").path(0).path("message").path("content"));
    if (hasText(chatText)) {
      return chatText;
    }

    throw new LlmProviderException("OpenAI response did not contain text output");
  }

  public static String anthropicText(JsonNode root) {
    for (JsonNode item : root.path("content")) {
      String text = text(item.path("text"));
      if (hasText(text)) {
        return text;
      }
    }
    throw new LlmProviderException("Anthropic response did not contain text output");
  }

  public static String geminiText(JsonNode root) {
    StringBuilder sb = new StringBuilder();
    for (JsonNode part : root.path("candidates").path(0).path("content").path("parts")) {
      String text = text(part.path("text"));
      if (hasText(text)) {
        if (!sb.isEmpty()) {
          sb.append('\n');
        }
        sb.append(text);
      }
    }

    if (!sb.isEmpty()) {
      return sb.toString();
    }

    throw new LlmProviderException("Google response did not contain text output");
  }

  private static String text(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    if (!node.isTextual()) {
      return null;
    }
    return node.textValue();
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
