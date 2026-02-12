package me.karubidev.devagent.agents.review;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class ReviewOutputSchemaParser {

  private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
      "```json\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.CASE_INSENSITIVE
  );

  private final ObjectMapper objectMapper;

  public ReviewOutputSchemaParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ParseResult parse(String rawText, String userRequest, List<GeneratedFile> codeFiles) {
    JsonNode direct = readObject(rawText);
    if (direct != null) {
      return new ParseResult(normalizeSchema(direct, userRequest, codeFiles), ParseSource.JSON);
    }

    JsonNode fromBlock = readObject(extractJsonCodeBlock(rawText));
    if (fromBlock != null) {
      return new ParseResult(normalizeSchema(fromBlock, userRequest, codeFiles), ParseSource.JSON_CODE_BLOCK);
    }

    return new ParseResult(fallbackSchema(rawText, userRequest, codeFiles), ParseSource.FALLBACK);
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

  private JsonNode normalizeSchema(JsonNode parsed, String userRequest, List<GeneratedFile> codeFiles) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("summary", defaultValue(parsed.path("summary").asText(""), defaultValue(userRequest, "")));
    root.put("overallRisk", normalizeSeverity(parsed.path("overallRisk").asText(""), "MEDIUM"));
    root.set("findings", normalizeFindings(parsed.path("findings"), codeFiles));
    root.set("strengths", normalizeStringArray(parsed.path("strengths")));
    root.set("nextActions", normalizeStringArray(parsed.path("nextActions")));
    return root;
  }

  private JsonNode fallbackSchema(String rawText, String userRequest, List<GeneratedFile> codeFiles) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("summary", defaultValue(userRequest, "Generated review output."));
    root.put("overallRisk", "MEDIUM");

    ArrayNode findings = objectMapper.createArrayNode();
    if (rawText != null && !rawText.isBlank()) {
      ObjectNode finding = objectMapper.createObjectNode();
      finding.put("title", "Generated Review");
      finding.put("severity", "MEDIUM");
      finding.put("file", firstCodeFilePath(codeFiles));
      finding.put("line", 0);
      finding.put("description", trim(rawText, 1800));
      finding.put("suggestion", "");
      findings.add(finding);
    }
    root.set("findings", findings);
    root.set("strengths", objectMapper.createArrayNode());
    root.set("nextActions", objectMapper.createArrayNode());
    return root;
  }

  private ArrayNode normalizeFindings(JsonNode findingsNode, List<GeneratedFile> codeFiles) {
    ArrayNode findings = objectMapper.createArrayNode();
    if (findingsNode == null || !findingsNode.isArray()) {
      return findings;
    }

    for (JsonNode findingNode : findingsNode) {
      if (!findingNode.isObject()) {
        continue;
      }
      String title = findingNode.path("title").asText("").trim();
      String description = findingNode.path("description").asText("").trim();
      if (title.isBlank() && description.isBlank()) {
        continue;
      }

      ObjectNode finding = objectMapper.createObjectNode();
      finding.put("title", defaultValue(title, "Untitled finding"));
      finding.put("severity", normalizeSeverity(findingNode.path("severity").asText(""), "MEDIUM"));
      String file = findingNode.path("file").asText("").trim();
      finding.put("file", file.isBlank() ? firstCodeFilePath(codeFiles) : file);
      int line = findingNode.path("line").asInt(0);
      finding.put("line", Math.max(0, line));
      finding.put("description", description);
      finding.put("suggestion", findingNode.path("suggestion").asText("").trim());
      findings.add(finding);
    }
    return findings;
  }

  private ArrayNode normalizeStringArray(JsonNode node) {
    ArrayNode array = objectMapper.createArrayNode();
    if (node == null || !node.isArray()) {
      return array;
    }
    Set<String> dedup = new LinkedHashSet<>();
    for (JsonNode item : node) {
      String text = item.asText("").trim();
      if (!text.isBlank()) {
        dedup.add(text);
      }
    }
    for (String item : dedup) {
      array.add(item);
    }
    return array;
  }

  private String firstCodeFilePath(List<GeneratedFile> codeFiles) {
    if (codeFiles == null) {
      return "";
    }
    for (GeneratedFile file : codeFiles) {
      if (file == null || file.path() == null) {
        continue;
      }
      String path = file.path().trim();
      if (!path.isBlank()) {
        return path;
      }
    }
    return "";
  }

  private String normalizeSeverity(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    String upper = value.trim().toUpperCase();
    return switch (upper) {
      case "LOW", "MEDIUM", "HIGH", "CRITICAL" -> upper;
      default -> fallback;
    };
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

  public record ParseResult(JsonNode review, ParseSource source) {

    public boolean usedFallback() {
      return source == ParseSource.FALLBACK;
    }
  }

  public enum ParseSource {
    JSON,
    JSON_CODE_BLOCK,
    FALLBACK
  }
}
