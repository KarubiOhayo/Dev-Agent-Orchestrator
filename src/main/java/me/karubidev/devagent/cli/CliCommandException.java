package me.karubidev.devagent.cli;

import org.springframework.boot.ExitCodeGenerator;

public class CliCommandException extends RuntimeException implements ExitCodeGenerator {

  private final int exitCode;

  public CliCommandException(String message, int exitCode) {
    super(message);
    this.exitCode = exitCode;
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }
}
