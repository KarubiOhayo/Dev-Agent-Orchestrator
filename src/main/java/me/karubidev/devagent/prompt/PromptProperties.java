package me.karubidev.devagent.prompt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devagent.prompt")
public class PromptProperties {

  private String orchestratorRootDir = ".";
  private String orchestratorPromptsDir = "prompts";
  private String projectPromptsDir = ".devagent/prompts";
  private String globalFile = "global.md";
  private String agentsDir = "agents";
  private int maxCharsPerPrompt = 8000;

  public String getOrchestratorRootDir() {
    return orchestratorRootDir;
  }

  public void setOrchestratorRootDir(String orchestratorRootDir) {
    this.orchestratorRootDir = orchestratorRootDir;
  }

  public String getOrchestratorPromptsDir() {
    return orchestratorPromptsDir;
  }

  public void setOrchestratorPromptsDir(String orchestratorPromptsDir) {
    this.orchestratorPromptsDir = orchestratorPromptsDir;
  }

  public String getProjectPromptsDir() {
    return projectPromptsDir;
  }

  public void setProjectPromptsDir(String projectPromptsDir) {
    this.projectPromptsDir = projectPromptsDir;
  }

  public String getGlobalFile() {
    return globalFile;
  }

  public void setGlobalFile(String globalFile) {
    this.globalFile = globalFile;
  }

  public String getAgentsDir() {
    return agentsDir;
  }

  public void setAgentsDir(String agentsDir) {
    this.agentsDir = agentsDir;
  }

  public int getMaxCharsPerPrompt() {
    return maxCharsPerPrompt;
  }

  public void setMaxCharsPerPrompt(int maxCharsPerPrompt) {
    this.maxCharsPerPrompt = maxCharsPerPrompt;
  }
}
