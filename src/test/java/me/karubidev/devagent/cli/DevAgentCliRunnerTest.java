package me.karubidev.devagent.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.spec.SpecAgentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;

class DevAgentCliRunnerTest {

  @Test
  void runPrintsFriendlyMessageAndKeepsExitCodeForUnknownOption() {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        Mockito.mock(SpecAgentService.class),
        Mockito.mock(CliResultFormatter.class),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("generate", "--unknown-option=true"));

    String err = errBytes.toString();
    assertThat(runner.getExitCode()).isEqualTo(2);
    assertThat(err).contains("[devagent-cli] 지원하지 않는 옵션입니다: --unknown-option");
    assertThat(err).doesNotContain("Exception");
    assertThat(outBytes.toString()).isBlank();
  }

  @Test
  void runHelpPrintsUsageWithoutError() {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        Mockito.mock(SpecAgentService.class),
        Mockito.mock(CliResultFormatter.class),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("help"));

    assertThat(runner.getExitCode()).isZero();
    assertThat(outBytes.toString()).contains("devagent CLI (draft)");
    assertThat(errBytes.toString()).isBlank();
  }
}
