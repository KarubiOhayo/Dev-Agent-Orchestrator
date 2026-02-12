package me.karubidev.devagent.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import org.junit.jupiter.api.Test;

class CliResultFormatterTest {

  @Test
  void formatGenerateRendersTableAndFileListForDryRun() {
    CliResultFormatter formatter = new CliResultFormatter();
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-123",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null
    );

    String output = formatter.formatGenerate(response);

    assertThat(output).contains("run-123");
    assertThat(output).contains("openai:gpt-5.2-codex");
    assertThat(output).contains("parsedFiles");
    assertThat(output).contains("applyOutcome");
    assertThat(output).contains("DRY_RUN");
    assertThat(output).contains("- DRY_RUN src/main/java/AuthController.java (planned)");
  }
}
