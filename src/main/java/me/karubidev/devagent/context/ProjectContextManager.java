package me.karubidev.devagent.context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import me.karubidev.devagent.state.RunStateProperties;
import me.karubidev.devagent.state.RunStateStore;
import org.springframework.stereotype.Service;

@Service
public class ProjectContextManager {

  private final ContextProperties properties;
  private final RunStateStore runStateStore;
  private final RunStateProperties runStateProperties;

  public ProjectContextManager(ContextProperties properties, RunStateStore runStateStore,
      RunStateProperties runStateProperties) {
    this.properties = properties;
    this.runStateStore = runStateStore;
    this.runStateProperties = runStateProperties;
  }

  public ContextBundle buildCodeContext(String userRequest, String projectId, Path projectRoot) {
    Set<String> queryTokens = tokenize(userRequest);
    List<String> referenced = new ArrayList<>();
    StringBuilder sb = new StringBuilder();

    Path globalRoot = Path.of(properties.getRootDir()).toAbsolutePath().normalize();
    appendSection(
        sb,
        referenced,
        "GLOBAL_RULES",
        globalRoot.resolve(properties.getRulesDir()),
        properties.getGlobalMaxFilesPerSection(),
        queryTokens
    );
    appendSection(
        sb,
        referenced,
        "GLOBAL_EXAMPLES",
        globalRoot.resolve(properties.getExamplesDir()),
        properties.getGlobalMaxFilesPerSection(),
        queryTokens
    );
    appendSection(
        sb,
        referenced,
        "PROJECT_RULES",
        projectRoot.resolve(properties.getProjectRulesDir()),
        properties.getProjectMaxFilesPerSection(),
        queryTokens
    );
    appendSection(
        sb,
        referenced,
        "PROJECT_EXAMPLES",
        projectRoot.resolve(properties.getProjectExamplesDir()),
        properties.getProjectMaxFilesPerSection(),
        queryTokens
    );

    if (properties.isIncludeProjectSummary()) {
      String summary = runStateStore.getProjectSummary(projectId);
      if (!summary.isBlank()) {
        sb.append("\n## PROJECT_SUMMARY\n").append(summary).append('\n');
      }
    }

    if (properties.isIncludeRecentOutputs()) {
      List<String> recent = runStateStore.getRecentOutputs(projectId, runStateProperties.getRecentOutputLimit());
      if (!recent.isEmpty()) {
        sb.append("\n## RECENT_OUTPUTS\n");
        int i = 1;
        for (String snippet : recent) {
          sb.append("\n### OUTPUT_").append(i++).append('\n')
              .append(trim(snippet, 1200)).append('\n');
        }
      }
    }

    if (sb.isEmpty()) {
      sb.append("(No context files selected)");
    }

    return new ContextBundle(sb.toString(), referenced);
  }

  private void appendSection(StringBuilder sb, List<String> referenced, String title, Path directory, int limit,
      Set<String> queryTokens) {
    List<Path> files = collectRankedFiles(directory, limit, queryTokens);
    if (files.isEmpty()) {
      return;
    }

    sb.append("\n## ").append(title).append('\n');
    for (Path file : files) {
      String content = readTrimmed(file, properties.getMaxCharsPerFile());
      sb.append("\n### ").append(file.toString()).append('\n').append(content).append('\n');
      referenced.add(file.toString());
    }
  }

  private List<Path> collectRankedFiles(Path dir, int limit, Set<String> queryTokens) {
    if (!Files.exists(dir) || !Files.isDirectory(dir)) {
      return List.of();
    }

    record ScoredFile(Path path, int score) {
    }

    try (Stream<Path> stream = Files.walk(dir, 1)) {
      return stream
          .filter(Files::isRegularFile)
          .map(path -> new ScoredFile(path, score(path, queryTokens)))
          .sorted(Comparator.<ScoredFile>comparingInt(ScoredFile::score).reversed()
              .thenComparing(sf -> sf.path().toString()))
          .limit(Math.max(1, limit))
          .map(ScoredFile::path)
          .toList();
    } catch (IOException e) {
      return List.of();
    }
  }

  private int score(Path path, Set<String> tokens) {
    int score = 0;
    String pathText = path.toString().toLowerCase(Locale.ROOT);
    String sample = readTrimmed(path, properties.getScoringSampleChars()).toLowerCase(Locale.ROOT);

    for (String token : tokens) {
      if (token.isBlank()) {
        continue;
      }
      if (pathText.contains(token)) {
        score += 3;
      }
      if (sample.contains(token)) {
        score += 1;
      }
    }

    return score;
  }

  private Set<String> tokenize(String input) {
    if (input == null || input.isBlank()) {
      return Set.of();
    }

    String[] parts = input.toLowerCase(Locale.ROOT).split("[^a-z0-9가-힣_]+");
    Set<String> tokens = new HashSet<>();
    for (String part : parts) {
      if (part.length() >= 2) {
        tokens.add(part);
      }
    }
    return tokens;
  }

  private String readTrimmed(Path file, int maxChars) {
    try {
      String content = Files.readString(file, StandardCharsets.UTF_8);
      return trim(content, maxChars);
    } catch (IOException e) {
      return "(failed to read: " + file + ")";
    }
  }

  private String trim(String content, int maxChars) {
    if (content == null) {
      return "";
    }
    if (content.length() <= maxChars) {
      return content;
    }
    return content.substring(0, maxChars) + "\n...(truncated)...";
  }
}
