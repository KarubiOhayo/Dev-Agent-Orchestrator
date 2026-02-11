package me.karubidev.devagent.agents.code.apply;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
}
