package me.karubidev.devagent.context;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devagent.context")
public class ContextProperties {

  private String rootDir = ".";
  private String rulesDir = "docs/rules";
  private String examplesDir = "docs/examples";
  private String projectRulesDir = ".devagent/rules";
  private String projectExamplesDir = ".devagent/examples";
  private int globalMaxFilesPerSection = 3;
  private int projectMaxFilesPerSection = 3;
  private int maxCharsPerFile = 2500;
  private int scoringSampleChars = 1200;
  private boolean includeProjectSummary = true;
  private boolean includeRecentOutputs = true;

  public String getRootDir() {
    return rootDir;
  }

  public void setRootDir(String rootDir) {
    this.rootDir = rootDir;
  }

  public String getRulesDir() {
    return rulesDir;
  }

  public void setRulesDir(String rulesDir) {
    this.rulesDir = rulesDir;
  }

  public String getExamplesDir() {
    return examplesDir;
  }

  public void setExamplesDir(String examplesDir) {
    this.examplesDir = examplesDir;
  }

  public String getProjectRulesDir() {
    return projectRulesDir;
  }

  public void setProjectRulesDir(String projectRulesDir) {
    this.projectRulesDir = projectRulesDir;
  }

  public String getProjectExamplesDir() {
    return projectExamplesDir;
  }

  public void setProjectExamplesDir(String projectExamplesDir) {
    this.projectExamplesDir = projectExamplesDir;
  }

  public int getGlobalMaxFilesPerSection() {
    return globalMaxFilesPerSection;
  }

  public void setGlobalMaxFilesPerSection(int globalMaxFilesPerSection) {
    this.globalMaxFilesPerSection = globalMaxFilesPerSection;
  }

  public int getProjectMaxFilesPerSection() {
    return projectMaxFilesPerSection;
  }

  public void setProjectMaxFilesPerSection(int projectMaxFilesPerSection) {
    this.projectMaxFilesPerSection = projectMaxFilesPerSection;
  }

  public int getMaxCharsPerFile() {
    return maxCharsPerFile;
  }

  public void setMaxCharsPerFile(int maxCharsPerFile) {
    this.maxCharsPerFile = maxCharsPerFile;
  }

  public int getScoringSampleChars() {
    return scoringSampleChars;
  }

  public void setScoringSampleChars(int scoringSampleChars) {
    this.scoringSampleChars = scoringSampleChars;
  }

  public boolean isIncludeProjectSummary() {
    return includeProjectSummary;
  }

  public void setIncludeProjectSummary(boolean includeProjectSummary) {
    this.includeProjectSummary = includeProjectSummary;
  }

  public boolean isIncludeRecentOutputs() {
    return includeRecentOutputs;
  }

  public void setIncludeRecentOutputs(boolean includeRecentOutputs) {
    this.includeRecentOutputs = includeRecentOutputs;
  }
}
