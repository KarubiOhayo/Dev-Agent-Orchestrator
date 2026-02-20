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
  private static final Pattern GENERIC_CODE_BLOCK_PATTERN = Pattern.compile(
      "```[a-zA-Z0-9_-]*\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.CASE_INSENSITIVE
  );
  private static final List<String> WRAPPER_KEYS = List.of("data", "result", "output", "payload", "response");

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

    ParseResult fromDirectJson = parseJsonCandidate(output, ParseSource.JSON);
    if (fromDirectJson != null) {
      return fromDirectJson;
    }

    ParseResult fromJsonBlock = parseJsonCandidate(extractJsonCodeBlock(output), ParseSource.JSON_CODE_BLOCK);
    if (fromJsonBlock != null) {
      return fromJsonBlock;
    }

    ParseResult fromGenericCodeBlock = parseFirstGenericCodeBlock(output);
    if (fromGenericCodeBlock != null) {
      return fromGenericCodeBlock;
    }

    ParseResult fromEmbeddedJson = parseJsonCandidate(extractFirstJsonPayload(output), ParseSource.JSON_EMBEDDED);
    if (fromEmbeddedJson != null) {
      return fromEmbeddedJson;
    }

    List<GeneratedFile> fromLooseJson = parseLooseJsonFilePairs(output);
    if (!fromLooseJson.isEmpty()) {
      return new ParseResult(fromLooseJson, ParseSource.LOOSE_JSON_FALLBACK);
    }

    List<GeneratedFile> fromMarkdown = parseMarkdownFiles(output);
    if (!fromMarkdown.isEmpty()) {
      return new ParseResult(fromMarkdown, ParseSource.MARKDOWN_FALLBACK);
    }

    return new ParseResult(List.of(), ParseSource.EMPTY);
  }

  private ParseResult parseJsonCandidate(String candidate, ParseSource source) {
    JsonNode root = readJson(candidate);
    if (!hasFilesArray(root)) {
      return null;
    }
    return new ParseResult(parseJsonFiles(root), source);
  }

  private ParseResult parseFirstGenericCodeBlock(String output) {
    Matcher matcher = GENERIC_CODE_BLOCK_PATTERN.matcher(output);
    while (matcher.find()) {
      ParseResult result = parseJsonCandidate(matcher.group(1), ParseSource.JSON_GENERIC_CODE_BLOCK);
      if (result != null) {
        return result;
      }
    }
    return null;
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
    JsonNode filesNode = findFilesArrayNode(root);
    if (filesNode == null || !filesNode.isArray()) {
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
    return findFilesArrayNode(root) != null;
  }

  private JsonNode findFilesArrayNode(JsonNode root) {
    if (root == null) {
      return null;
    }
    if (root.isArray()) {
      return looksLikeFileArray(root) ? root : null;
    }
    if (!root.isObject()) {
      return null;
    }

    JsonNode filesNode = root.path("files");
    if (filesNode.isArray()) {
      return filesNode;
    }

    for (String wrapperKey : WRAPPER_KEYS) {
      JsonNode nested = root.path(wrapperKey);
      if (!nested.isObject()) {
        continue;
      }
      JsonNode nestedFiles = nested.path("files");
      if (nestedFiles.isArray()) {
        return nestedFiles;
      }
    }

    return null;
  }

  private boolean looksLikeFileArray(JsonNode node) {
    if (!node.isArray()) {
      return false;
    }
    if (node.isEmpty()) {
      return true;
    }

    for (JsonNode item : node) {
      if (!item.isObject()) {
        return false;
      }
      if (!item.has("path")) {
        return false;
      }
    }
    return true;
  }

  private List<GeneratedFile> mapToFiles(Map<String, String> byPath) {
    List<GeneratedFile> files = new ArrayList<>();
    for (Map.Entry<String, String> entry : byPath.entrySet()) {
      files.add(new GeneratedFile(entry.getKey(), entry.getValue()));
    }
    return files;
  }

  private JsonNode readJson(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }

    try {
      return objectMapper.readTree(text);
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

  private String extractFirstJsonPayload(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }

    int start = -1;
    int objectDepth = 0;
    int arrayDepth = 0;
    boolean inString = false;
    boolean escaping = false;

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (start < 0) {
        if (c == '{' || c == '[') {
          start = i;
          if (c == '{') {
            objectDepth = 1;
          } else {
            arrayDepth = 1;
          }
        }
        continue;
      }

      if (inString) {
        if (escaping) {
          escaping = false;
        } else if (c == '\\') {
          escaping = true;
        } else if (c == '"') {
          inString = false;
        }
        continue;
      }

      if (c == '"') {
        inString = true;
        continue;
      }
      if (c == '{') {
        objectDepth++;
        continue;
      }
      if (c == '}') {
        objectDepth--;
      } else if (c == '[') {
        arrayDepth++;
      } else if (c == ']') {
        arrayDepth--;
      }

      if (objectDepth == 0 && arrayDepth == 0) {
        return text.substring(start, i + 1);
      }
    }

    return "";
  }

  private List<GeneratedFile> parseLooseJsonFilePairs(String output) {
    if (output == null || output.isBlank()) {
      return List.of();
    }

    Map<String, String> byPath = new LinkedHashMap<>();
    int cursor = 0;
    while (cursor < output.length()) {
      int pathKey = output.indexOf("\"path\"", cursor);
      if (pathKey < 0) {
        break;
      }
      int pathValueStart = findJsonStringValueStart(output, pathKey + "\"path\"".length());
      if (pathValueStart < 0) {
        cursor = pathKey + "\"path\"".length();
        continue;
      }

      ParsedJsonString pathValue = readJsonString(output, pathValueStart);
      if (pathValue == null) {
        cursor = pathValueStart + 1;
        continue;
      }

      int contentKey = output.indexOf("\"content\"", pathValue.nextIndex());
      if (contentKey < 0) {
        cursor = pathValue.nextIndex();
        continue;
      }
      int contentValueStart = findJsonStringValueStart(output, contentKey + "\"content\"".length());
      if (contentValueStart < 0) {
        cursor = contentKey + "\"content\"".length();
        continue;
      }

      ParsedJsonString contentValue = readJsonString(output, contentValueStart);
      if (contentValue == null) {
        cursor = contentValueStart + 1;
        continue;
      }

      String path = decodeJsonString(pathValue.value()).trim();
      if (path.isBlank()) {
        cursor = contentValue.nextIndex();
        continue;
      }
      String content = decodeJsonString(contentValue.value());
      byPath.putIfAbsent(path, trimTrailingWhitespace(content));
      cursor = contentValue.nextIndex();
    }
    return mapToFiles(byPath);
  }

  private int findJsonStringValueStart(String text, int startIndex) {
    int colonIndex = text.indexOf(':', startIndex);
    if (colonIndex < 0) {
      return -1;
    }

    int i = colonIndex + 1;
    while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
      i++;
    }
    if (i >= text.length() || text.charAt(i) != '"') {
      return -1;
    }
    return i;
  }

  private ParsedJsonString readJsonString(String text, int quoteStartIndex) {
    if (quoteStartIndex < 0 || quoteStartIndex >= text.length() || text.charAt(quoteStartIndex) != '"') {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    boolean escaping = false;
    for (int i = quoteStartIndex + 1; i < text.length(); i++) {
      char c = text.charAt(i);
      if (escaping) {
        sb.append(c);
        escaping = false;
        continue;
      }
      if (c == '\\') {
        sb.append(c);
        escaping = true;
        continue;
      }
      if (c == '"') {
        return new ParsedJsonString(sb.toString(), i + 1);
      }
      sb.append(c);
    }
    return null;
  }

  private String decodeJsonString(String raw) {
    if (raw == null) {
      return "";
    }
    try {
      return objectMapper.readValue("\"" + raw + "\"", String.class);
    } catch (Exception ignored) {
      return raw;
    }
  }

  private record ParsedJsonString(String value, int nextIndex) {
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
    JSON_GENERIC_CODE_BLOCK,
    JSON_EMBEDDED,
    LOOSE_JSON_FALLBACK,
    MARKDOWN_FALLBACK,
    EMPTY
  }
}
