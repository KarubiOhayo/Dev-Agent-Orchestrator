package me.karubidev.devagent.agents.code.apply;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FileApplyService {

  public FileApplyResult apply(
      Path targetRoot,
      List<GeneratedFile> files,
      boolean dryRun,
      boolean overwriteExisting
  ) {
    Path normalizedRoot = targetRoot.toAbsolutePath().normalize();
    List<FileApplyItem> items = new ArrayList<>();
    int written = 0;
    int skipped = 0;

    for (GeneratedFile file : files) {
      String rawPath = file.path();
      if (rawPath == null || rawPath.isBlank()) {
        skipped++;
        items.add(new FileApplyItem("", "REJECTED", "empty path"));
        continue;
      }

      Path relativePath = Path.of(rawPath);
      if (relativePath.isAbsolute()) {
        skipped++;
        items.add(new FileApplyItem(rawPath, "REJECTED", "absolute path is not allowed"));
        continue;
      }

      Path resolved = normalizedRoot.resolve(relativePath).normalize();
      if (!resolved.startsWith(normalizedRoot)) {
        skipped++;
        items.add(new FileApplyItem(rawPath, "REJECTED", "path traversal is not allowed"));
        continue;
      }

      try {
        if (Files.exists(resolved) && !overwriteExisting) {
          skipped++;
          items.add(new FileApplyItem(rawPath, "SKIPPED", "file exists (overwriteExisting=false)"));
          continue;
        }

        if (dryRun) {
          items.add(new FileApplyItem(rawPath, "DRY_RUN", "planned"));
          continue;
        }

        Path parent = resolved.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
        Files.writeString(
            resolved,
            file.content() == null ? "" : file.content(),
            StandardCharsets.UTF_8,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
            java.nio.file.StandardOpenOption.WRITE
        );
        written++;
        items.add(new FileApplyItem(rawPath, "WRITTEN", "ok"));
      } catch (IOException e) {
        skipped++;
        items.add(new FileApplyItem(rawPath, "ERROR", e.getMessage()));
      }
    }

    return new FileApplyResult(dryRun, files.size(), written, skipped, items);
  }
}
