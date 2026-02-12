package me.karubidev.devagent.agents.code.apply;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class CodeOutputParser {

  private static final Pattern FILE_BLOCK_PATTERN = Pattern.compile(
      "###\\s+`([^`]+)`\\s*\\R```[a-zA-Z0-9_-]*\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.MULTILINE
  );
  private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
      "```json\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.CASE_INSENSITIVE
  );

  private final ObjectMapper objectMapper;

  public CodeOutputParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<GeneratedFile> parseFiles(String output) {
    return parse(output).files();
  }

  public ParseResult parse(String output) {
    if (output == null || output.isBlank()) {
      return new ParseResult(List.of(), ParseSource.EMPTY);
    }

    JsonNode direct = readObject(output);
    List<GeneratedFile> fromDirectJson = parseJsonFiles(direct);
    if (hasFilesArray(direct)) {
      return new ParseResult(fromDirectJson, ParseSource.JSON);
    }

    JsonNode fromJsonBlock = readObject(extractJsonCodeBlock(output));
    List<GeneratedFile> fromJsonCodeBlock = parseJsonFiles(fromJsonBlock);
    if (hasFilesArray(fromJsonBlock)) {
      return new ParseResult(fromJsonCodeBlock, ParseSource.JSON_CODE_BLOCK);
    }

    List<GeneratedFile> fromMarkdown = parseMarkdownFiles(output);
    if (!fromMarkdown.isEmpty()) {
      return new ParseResult(fromMarkdown, ParseSource.MARKDOWN_FALLBACK);
    }

    return new ParseResult(List.of(), ParseSource.EMPTY);
  }

  private List<GeneratedFile> parseMarkdownFiles(String output) {
    Matcher matcher = FILE_BLOCK_PATTERN.matcher(output);
    Map<String, String> byPath = new LinkedHashMap<>();
    while (matcher.find()) {
      String path = matcher.group(1).trim();
      String content = matcher.group(2);
      if (path.isBlank()) {
        continue;
      }
      byPath.putIfAbsent(path, trimTrailingWhitespace(content));
    }
    return mapToFiles(byPath);
  }

  private List<GeneratedFile> parseJsonFiles(JsonNode root) {
    if (root == null || !root.isObject()) {
      return List.of();
    }

    JsonNode filesNode = root.path("files");
    if (!filesNode.isArray()) {
      return List.of();
    }

    Map<String, String> byPath = new LinkedHashMap<>();
    for (JsonNode fileNode : filesNode) {
      if (!fileNode.isObject()) {
        continue;
      }

      String path = fileNode.path("path").asText("").trim();
      if (path.isBlank()) {
        continue;
      }

      String content = fileNode.path("content").asText("");
      byPath.putIfAbsent(path, trimTrailingWhitespace(content));
    }
    return mapToFiles(byPath);
  }

  private boolean hasFilesArray(JsonNode root) {
    return root != null && root.isObject() && root.path("files").isArray();
  }

  private List<GeneratedFile> mapToFiles(Map<String, String> byPath) {
    List<GeneratedFile> files = new ArrayList<>();
    for (Map.Entry<String, String> entry : byPath.entrySet()) {
      files.add(new GeneratedFile(entry.getKey(), entry.getValue()));
    }
    return files;
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
      // markdown fallback will be used when JSON parse fails
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

  private String trimTrailingWhitespace(String text) {
    if (text == null) {
      return "";
    }
    return text.replaceFirst("\\s+$", "");
  }

  public record ParseResult(List<GeneratedFile> files, ParseSource source) {

    public boolean usedMarkdownFallback() {
      return source == ParseSource.MARKDOWN_FALLBACK;
    }
  }

  public enum ParseSource {
    JSON,
    JSON_CODE_BLOCK,
    MARKDOWN_FALLBACK,
    EMPTY
  }
}
