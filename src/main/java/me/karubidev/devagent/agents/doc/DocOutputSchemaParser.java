package me.karubidev.devagent.agents.doc;

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
public class DocOutputSchemaParser {

  private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
      "```json\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.CASE_INSENSITIVE
  );

  private final ObjectMapper objectMapper;

  public DocOutputSchemaParser(ObjectMapper objectMapper) {
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
    root.put("title", defaultValue(parsed.path("title").asText(""), defaultValue(userRequest, "Generated Documentation")));
    root.put("summary", defaultValue(parsed.path("summary").asText(""), defaultValue(userRequest, "")));
    root.set("sections", normalizeSections(parsed.path("sections")));
    root.set("relatedFiles", normalizeRelatedFiles(parsed.path("relatedFiles"), codeFiles));
    root.set("notes", normalizeStringArray(parsed.path("notes")));
    return root;
  }

  private JsonNode fallbackSchema(String rawText, String userRequest, List<GeneratedFile> codeFiles) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("title", defaultValue(userRequest, "Generated Documentation"));
    root.put("summary", defaultValue(userRequest, ""));

    ArrayNode sections = objectMapper.createArrayNode();
    if (rawText != null && !rawText.isBlank()) {
      ObjectNode section = objectMapper.createObjectNode();
      section.put("heading", "Generated Output");
      section.put("content", trim(rawText, 1800));
      sections.add(section);
    }
    root.set("sections", sections);
    root.set("relatedFiles", normalizeRelatedFiles(null, codeFiles));
    root.set("notes", objectMapper.createArrayNode());
    return root;
  }

  private ArrayNode normalizeSections(JsonNode sectionsNode) {
    ArrayNode sections = objectMapper.createArrayNode();
    if (sectionsNode == null || !sectionsNode.isArray()) {
      return sections;
    }

    for (JsonNode sectionNode : sectionsNode) {
      if (!sectionNode.isObject()) {
        continue;
      }
      String heading = sectionNode.path("heading").asText("").trim();
      String content = sectionNode.path("content").asText("").trim();
      if (heading.isBlank() && content.isBlank()) {
        continue;
      }
      ObjectNode section = objectMapper.createObjectNode();
      section.put("heading", heading);
      section.put("content", content);
      sections.add(section);
    }
    return sections;
  }

  private ArrayNode normalizeRelatedFiles(JsonNode relatedFilesNode, List<GeneratedFile> codeFiles) {
    Set<String> paths = new LinkedHashSet<>();
    if (relatedFilesNode != null && relatedFilesNode.isArray()) {
      for (JsonNode fileNode : relatedFilesNode) {
        String path = fileNode.asText("").trim();
        if (!path.isBlank()) {
          paths.add(path);
        }
      }
    }
    if (paths.isEmpty() && codeFiles != null) {
      for (GeneratedFile codeFile : codeFiles) {
        if (codeFile == null || codeFile.path() == null) {
          continue;
        }
        String path = codeFile.path().trim();
        if (!path.isBlank()) {
          paths.add(path);
        }
      }
    }

    ArrayNode array = objectMapper.createArrayNode();
    for (String path : paths) {
      array.add(path);
    }
    return array;
  }

  private ArrayNode normalizeStringArray(JsonNode node) {
    ArrayNode array = objectMapper.createArrayNode();
    if (node == null || !node.isArray()) {
      return array;
    }
    for (JsonNode item : node) {
      String text = item.asText("").trim();
      if (!text.isBlank()) {
        array.add(text);
      }
    }
    return array;
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

  public record ParseResult(JsonNode document, ParseSource source) {

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
