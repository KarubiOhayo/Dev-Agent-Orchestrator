package me.karubidev.devagent.agents.code.apply;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FileApplyService {

  private static final String STATUS_REJECTED = "REJECTED";
  private static final String STATUS_ERROR = "ERROR";
  private static final String MESSAGE_EMPTY_PATH = "empty path";
  private static final String MESSAGE_ABSOLUTE_PATH = "absolute path is not allowed";
  private static final String MESSAGE_TRAVERSAL_PATH = "path traversal is not allowed";
  private static final String MESSAGE_INVALID_PATH = "invalid path";

  public FileApplyResult apply(
      Path targetRoot,
      List<GeneratedFile> files,
      boolean dryRun,
      boolean overwriteExisting
  ) {
    if (targetRoot == null) {
      throw new IllegalArgumentException("targetRoot is required");
    }
    if (files == null) {
      throw new IllegalArgumentException("files is required");
    }

    Path normalizedRoot = targetRoot.toAbsolutePath().normalize();
    List<FileApplyItem> items = new ArrayList<>();
    int written = 0;
    int skipped = 0;

    for (GeneratedFile file : files) {
      String rawPath = file == null ? null : file.path();
      if (rawPath == null || rawPath.isBlank()) {
        skipped++;
        items.add(new FileApplyItem("", STATUS_REJECTED, MESSAGE_EMPTY_PATH));
        continue;
      }

      Path relativePath;
      try {
        relativePath = Path.of(rawPath);
      } catch (InvalidPathException e) {
        skipped++;
        items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_INVALID_PATH));
        continue;
      }

      if (relativePath.isAbsolute()) {
        skipped++;
        items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_ABSOLUTE_PATH));
        continue;
      }

      if (containsTraversal(relativePath)) {
        skipped++;
        items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_TRAVERSAL_PATH));
        continue;
      }

      Path normalizedRelative = relativePath.normalize();
      if (normalizedRelative.toString().isBlank()) {
        skipped++;
        items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_EMPTY_PATH));
        continue;
      }

      Path resolved = normalizedRoot.resolve(normalizedRelative).normalize();
      if (!resolved.startsWith(normalizedRoot)) {
        skipped++;
        items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_TRAVERSAL_PATH));
        continue;
      }

      try {
        if (!isWithinRealBoundary(normalizedRoot, resolved)) {
          skipped++;
          items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_TRAVERSAL_PATH));
          continue;
        }

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
        if (!isWithinRealBoundary(normalizedRoot, resolved)) {
          skipped++;
          items.add(new FileApplyItem(rawPath, STATUS_REJECTED, MESSAGE_TRAVERSAL_PATH));
          continue;
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
        items.add(new FileApplyItem(rawPath, STATUS_ERROR, e.getMessage()));
      }
    }

    return new FileApplyResult(dryRun, files.size(), written, skipped, items);
  }

  private boolean containsTraversal(Path path) {
    for (Path part : path) {
      if ("..".equals(part.toString())) {
        return true;
      }
    }
    return false;
  }

  private boolean isWithinRealBoundary(Path normalizedRoot, Path resolvedPath) throws IOException {
    Path realRootBoundary = resolveRealPathWithFallback(normalizedRoot);
    Path existingAncestor = findNearestExistingAncestor(resolvedPath);
    Path realExistingAncestor = existingAncestor.toRealPath();
    Path unresolvedSuffix = existingAncestor.relativize(resolvedPath);
    Path realResolvedPath = realExistingAncestor.resolve(unresolvedSuffix).normalize();
    return realResolvedPath.startsWith(realRootBoundary);
  }

  private Path resolveRealPathWithFallback(Path path) throws IOException {
    if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
      return path.toRealPath().normalize();
    }
    Path existingAncestor = findNearestExistingAncestor(path);
    Path realExistingAncestor = existingAncestor.toRealPath();
    Path unresolvedSuffix = existingAncestor.relativize(path);
    return realExistingAncestor.resolve(unresolvedSuffix).normalize();
  }

  private Path findNearestExistingAncestor(Path path) throws IOException {
    Path cursor = path;
    while (cursor != null && Files.notExists(cursor, LinkOption.NOFOLLOW_LINKS)) {
      cursor = cursor.getParent();
    }
    if (cursor == null) {
      throw new IOException("unable to resolve existing ancestor");
    }
    return cursor;
  }
}
