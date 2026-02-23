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
  private static final Pattern FILES_ARRAY_PATTERN = Pattern.compile("(?<!\\\\)\"files\"\\s*:\\s*\\[");
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
    Matcher matcher = FILES_ARRAY_PATTERN.matcher(output);
    while (matcher.find()) {
      int arrayStart = matcher.end() - 1;
      if (arrayStart < 0 || arrayStart >= output.length() || output.charAt(arrayStart) != '[') {
        continue;
      }

      int arrayEnd = findArrayEndOrOutputEnd(output, arrayStart);
      collectLooseFilesFromArray(output, arrayStart, arrayEnd, byPath);
    }
    return mapToFiles(byPath);
  }

  private void collectLooseFilesFromArray(String text, int arrayStart, int arrayEndExclusive, Map<String, String> byPath) {
    int cursor = arrayStart + 1;
    while (cursor < arrayEndExclusive) {
      int objectStart = findCharOutsideString(text, '{', cursor, arrayEndExclusive);
      if (objectStart < 0) {
        return;
      }

      int objectEnd = findObjectEndOrSectionEnd(text, objectStart, arrayEndExclusive);
      LooseFile looseFile = extractLooseFileFromObject(text, objectStart, objectEnd);
      if (looseFile != null) {
        byPath.putIfAbsent(looseFile.path(), looseFile.content());
      }
      cursor = Math.max(objectEnd, objectStart + 1);
    }
  }

  private LooseFile extractLooseFileFromObject(String text, int objectStart, int objectEndExclusive) {
    int depth = 0;
    int cursor = objectStart;
    String path = null;
    String content = null;

    while (cursor < objectEndExclusive) {
      char c = text.charAt(cursor);
      if (c == '{') {
        depth++;
        cursor++;
        continue;
      }
      if (c == '}') {
        depth = Math.max(0, depth - 1);
        cursor++;
        continue;
      }
      if (c != '"') {
        cursor++;
        continue;
      }

      ParsedJsonString key = readJsonString(text, cursor, objectEndExclusive);
      if (key == null) {
        cursor++;
        continue;
      }

      if (depth != 1) {
        cursor = key.nextIndex();
        continue;
      }

      int valueStart = findJsonStringValueStart(text, key.nextIndex(), objectEndExclusive);
      if (valueStart < 0) {
        cursor = key.nextIndex();
        continue;
      }

      ParsedJsonString value = readJsonString(text, valueStart, objectEndExclusive);
      if (value == null) {
        cursor = valueStart + 1;
        continue;
      }

      String decodedKey = decodeJsonString(key.value());
      if ("path".equals(decodedKey)) {
        path = decodeJsonString(value.value()).trim();
      } else if ("content".equals(decodedKey)) {
        content = decodeJsonString(value.value());
      }
      cursor = value.nextIndex();
    }

    if (path == null || path.isBlank() || content == null) {
      return null;
    }
    return new LooseFile(path, trimTrailingWhitespace(content));
  }

  private int findArrayEndOrOutputEnd(String text, int arrayStart) {
    int depth = 0;
    boolean inString = false;
    boolean escaping = false;

    for (int i = arrayStart; i < text.length(); i++) {
      char c = text.charAt(i);
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
      if (c == '[') {
        depth++;
        continue;
      }
      if (c == ']') {
        depth--;
        if (depth == 0) {
          return i + 1;
        }
      }
    }

    return text.length();
  }

  private int findObjectEndOrSectionEnd(String text, int objectStart, int sectionEndExclusive) {
    int depth = 0;
    boolean inString = false;
    boolean escaping = false;

    for (int i = objectStart; i < sectionEndExclusive; i++) {
      char c = text.charAt(i);
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
        depth++;
        continue;
      }
      if (c == '}') {
        depth--;
        if (depth == 0) {
          return i + 1;
        }
      }
    }

    return sectionEndExclusive;
  }

  private int findCharOutsideString(String text, char target, int start, int endExclusive) {
    boolean inString = false;
    boolean escaping = false;

    for (int i = start; i < endExclusive; i++) {
      char c = text.charAt(i);
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
      if (c == target) {
        return i;
      }
    }

    return -1;
  }

  private int findJsonStringValueStart(String text, int startIndex, int limitExclusive) {
    int i = startIndex;
    while (i < limitExclusive && Character.isWhitespace(text.charAt(i))) {
      i++;
    }
    if (i >= limitExclusive || text.charAt(i) != ':') {
      return -1;
    }

    i++;
    while (i < limitExclusive && Character.isWhitespace(text.charAt(i))) {
      i++;
    }
    if (i >= limitExclusive || text.charAt(i) != '"') {
      return -1;
    }
    return i;
  }

  private ParsedJsonString readJsonString(String text, int quoteStartIndex) {
    return readJsonString(text, quoteStartIndex, text.length());
  }

  private ParsedJsonString readJsonString(String text, int quoteStartIndex, int limitExclusive) {
    if (quoteStartIndex < 0 || quoteStartIndex >= text.length() || text.charAt(quoteStartIndex) != '"') {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    boolean escaping = false;
    int end = Math.min(limitExclusive, text.length());
    for (int i = quoteStartIndex + 1; i < end; i++) {
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

  private record LooseFile(String path, String content) {
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
