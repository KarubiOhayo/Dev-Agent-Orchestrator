package me.karubidev.devagent.agents.code.apply;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileApplyServiceTest {

  private final FileApplyService fileApplyService = new FileApplyService();

  @Test
  void dryRunDoesNotWriteFiles(@TempDir Path tempDir) {
    List<GeneratedFile> files = List.of(new GeneratedFile("src/A.java", "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, true, false);

    assertThat(result.dryRun()).isTrue();
    assertThat(result.writtenFiles()).isEqualTo(0);
    assertThat(Files.exists(tempDir.resolve("src/A.java"))).isFalse();
    assertThat(result.files().get(0).status()).isEqualTo("DRY_RUN");
  }

  @Test
  void applyWritesFiles(@TempDir Path tempDir) throws Exception {
    List<GeneratedFile> files = List.of(new GeneratedFile("src/B.java", "class B {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.dryRun()).isFalse();
    assertThat(result.writtenFiles()).isEqualTo(1);
    assertThat(Files.readString(tempDir.resolve("src/B.java"))).contains("class B {}");
    assertThat(result.files().get(0).status()).isEqualTo("WRITTEN");
  }

  @Test
  void rejectEmptyPath(@TempDir Path tempDir) {
    List<GeneratedFile> files = List.of(new GeneratedFile("   ", "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("REJECTED");
    assertThat(result.files().get(0).message()).isEqualTo("empty path");
  }

  @Test
  void rejectAbsolutePath(@TempDir Path tempDir) {
    String absolutePath = tempDir.resolve("A.java").toString();
    List<GeneratedFile> files = List.of(new GeneratedFile(absolutePath, "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("REJECTED");
    assertThat(result.files().get(0).message()).isEqualTo("absolute path is not allowed");
  }

  @Test
  void rejectTraversalPath(@TempDir Path tempDir) {
    List<GeneratedFile> files = List.of(new GeneratedFile("../outside/A.java", "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("REJECTED");
    assertThat(result.files().get(0).message()).isEqualTo("path traversal is not allowed");
  }

  @Test
  void rejectNormalizedTraversalBypass(@TempDir Path tempDir) {
    List<GeneratedFile> files = List.of(new GeneratedFile("src/../..", "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("REJECTED");
    assertThat(result.files().get(0).message()).isEqualTo("path traversal is not allowed");
  }

  @Test
  void rejectSymlinkBoundaryBypass(@TempDir Path tempDir) throws Exception {
    Path parent = tempDir.getParent();
    Assumptions.assumeTrue(parent != null, "tempDir parent is required");

    Path outsideDir = Files.createTempDirectory(parent, "outside-");
    Path symlinkPath = tempDir.resolve("link-outside");
    createSymlinkOrSkip(symlinkPath, outsideDir);
    List<GeneratedFile> files = List.of(new GeneratedFile("link-outside/evil.txt", "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("REJECTED");
    assertThat(result.files().get(0).message()).isEqualTo("path traversal is not allowed");
    assertThat(Files.exists(outsideDir.resolve("evil.txt"))).isFalse();
  }

  @Test
  void rejectInvalidPath(@TempDir Path tempDir) {
    List<GeneratedFile> files = List.of(new GeneratedFile("bad\u0000path.java", "class A {}"));

    FileApplyResult result = fileApplyService.apply(tempDir, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("REJECTED");
    assertThat(result.files().get(0).message()).isEqualTo("invalid path");
  }

  @Test
  void ioFailureIsRecordedAsError(@TempDir Path tempDir) throws Exception {
    Path fileAsRoot = tempDir.resolve("root.txt");
    Files.writeString(fileAsRoot, "not-directory");
    List<GeneratedFile> files = List.of(new GeneratedFile("src/A.java", "class A {}"));

    FileApplyResult result = fileApplyService.apply(fileAsRoot, files, false, false);

    assertThat(result.writtenFiles()).isZero();
    assertThat(result.skippedFiles()).isEqualTo(1);
    assertThat(result.files().get(0).status()).isEqualTo("ERROR");
  }

  @Test
  void rejectNullTargetRoot() {
    assertThatThrownBy(() -> fileApplyService.apply(null, List.of(), false, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("targetRoot is required");
  }

  @Test
  void rejectNullFiles(@TempDir Path tempDir) {
    assertThatThrownBy(() -> fileApplyService.apply(tempDir, null, false, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("files is required");
  }

  private void createSymlinkOrSkip(Path link, Path target) throws IOException {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | FileSystemException | SecurityException e) {
      Assumptions.assumeTrue(false, "symbolic links are not supported in this environment");
    }
  }
}
