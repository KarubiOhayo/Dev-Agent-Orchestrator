package me.karubidev.devagent.llm;

import java.util.ArrayList;
import java.util.List;
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
    JsonNode candidate = root.path("candidates").path(0);
    StringBuilder sb = new StringBuilder();
    for (JsonNode part : candidate.path("content").path("parts")) {
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

    String finishReason = text(candidate.path("finishReason"));
    String partTypes = String.join(",", collectGeminiPartTypes(candidate.path("content").path("parts")));
    if (partTypes.isBlank()) {
      partTypes = "none";
    }
    if (!hasText(finishReason)) {
      finishReason = "unknown";
    }
    throw new LlmProviderException(
        "Google response did not contain text output (finishReason=%s, partTypes=%s)"
            .formatted(finishReason, partTypes)
    );
  }

  private static List<String> collectGeminiPartTypes(JsonNode parts) {
    List<String> types = new ArrayList<>();
    for (JsonNode part : parts) {
      if (!part.isObject()) {
        types.add("unknown");
        continue;
      }
      if (hasText(text(part.path("text")))) {
        types.add("text");
        continue;
      }
      if (part.has("functionCall")) {
        types.add("functionCall");
        continue;
      }
      if (part.has("functionResponse")) {
        types.add("functionResponse");
        continue;
      }
      if (part.has("inlineData")) {
        types.add("inlineData");
        continue;
      }
      if (part.has("fileData")) {
        types.add("fileData");
        continue;
      }
      types.add("object");
    }
    return types;
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
