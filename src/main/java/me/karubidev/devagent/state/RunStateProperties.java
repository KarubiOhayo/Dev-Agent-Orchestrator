package me.karubidev.devagent.state;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devagent.run-state")
public class RunStateProperties {

  private boolean enabled = true;
  private String jdbcUrl = "jdbc:sqlite:storage/devagent.db";
  private String fallbackLogFile = "storage/run-log.jsonl";
  private String fallbackMemoryFile = "storage/project-memory.properties";
  private int outputPreviewChars = 3000;
  private int recentOutputLimit = 3;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getFallbackLogFile() {
    return fallbackLogFile;
  }

  public void setFallbackLogFile(String fallbackLogFile) {
    this.fallbackLogFile = fallbackLogFile;
  }

  public String getFallbackMemoryFile() {
    return fallbackMemoryFile;
  }

  public void setFallbackMemoryFile(String fallbackMemoryFile) {
    this.fallbackMemoryFile = fallbackMemoryFile;
  }

  public int getOutputPreviewChars() {
    return outputPreviewChars;
  }

  public void setOutputPreviewChars(int outputPreviewChars) {
    this.outputPreviewChars = outputPreviewChars;
  }

  public int getRecentOutputLimit() {
    return recentOutputLimit;
  }

  public void setRecentOutputLimit(int recentOutputLimit) {
    this.recentOutputLimit = recentOutputLimit;
  }
}
