package me.karubidev.devagent.agents.code.apply;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CodeOutputParser {

  private static final Pattern FILE_BLOCK_PATTERN = Pattern.compile(
      "###\\s+`([^`]+)`\\s*\\R```[a-zA-Z0-9_-]*\\s*\\R([\\s\\S]*?)\\R```",
      Pattern.MULTILINE
  );

  public List<GeneratedFile> parseFiles(String output) {
    if (output == null || output.isBlank()) {
      return List.of();
    }

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

    List<GeneratedFile> files = new ArrayList<>();
    for (Map.Entry<String, String> entry : byPath.entrySet()) {
      files.add(new GeneratedFile(entry.getKey(), entry.getValue()));
    }
    return files;
  }

  private String trimTrailingWhitespace(String text) {
    if (text == null) {
      return "";
    }
    return text.replaceFirst("\\s+$", "");
  }
}
