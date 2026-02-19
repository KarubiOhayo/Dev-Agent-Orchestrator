package me.karubidev.devagent.agents.spec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class SpecOutputSchemaParser {

  private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
      "```json\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.CASE_INSENSITIVE
  );

  private final ObjectMapper objectMapper;

  public SpecOutputSchemaParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ParseResult parse(String rawText, String userRequest) {
    JsonNode direct = readObject(rawText);
    if (direct != null) {
      return new ParseResult(direct, ParseSource.DIRECT_JSON);
    }

    String codeBlock = extractJsonCodeBlock(rawText);
    JsonNode fromBlock = readObject(codeBlock);
    if (fromBlock != null) {
      return new ParseResult(fromBlock, ParseSource.JSON_CODE_BLOCK);
    }

    return new ParseResult(fallbackSchema(rawText, userRequest), ParseSource.FALLBACK_SCHEMA);
  }

  public JsonNode parseOrFallback(String rawText, String userRequest) {
    return parse(rawText, userRequest).spec();
  }

  private JsonNode readObject(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }

    try {
      JsonNode root = objectMapper.readTree(text);
      if (root != null && root.isObject()) {
        return root;
      }
    } catch (Exception ignored) {
      // fallback schema will be used when JSON parse fails
    }
    return null;
  }

  private String extractJsonCodeBlock(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }
    Matcher matcher = JSON_BLOCK_PATTERN.matcher(text);
    if (!matcher.find()) {
      return "";
    }
    return matcher.group(1);
  }

  private JsonNode fallbackSchema(String rawText, String userRequest) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("title", defaultValue(userRequest, "Generated Specification"));
    root.put("overview", defaultValue(userRequest, ""));
    root.set("constraints", objectMapper.createArrayNode());
    root.set("acceptanceCriteria", objectMapper.createArrayNode());
    root.set("tasks", objectMapper.createArrayNode());

    ArrayNode notes = objectMapper.createArrayNode();
    if (rawText != null && !rawText.isBlank()) {
      notes.add(trim(rawText, 1200));
    }
    root.set("notes", notes);
    return root;
  }

  private String defaultValue(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value;
  }

  private String trim(String value, int maxChars) {
    if (value == null) {
      return "";
    }
    if (value.length() <= maxChars) {
      return value;
    }
    return value.substring(0, maxChars) + "...";
  }

  public record ParseResult(JsonNode spec, ParseSource source) {
  }

  public enum ParseSource {
    DIRECT_JSON,
    JSON_CODE_BLOCK,
    FALLBACK_SCHEMA
  }
}
