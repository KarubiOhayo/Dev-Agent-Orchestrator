package me.karubidev.devagent.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class PromptRegistry {

  private final PromptProperties properties;

  public PromptRegistry(PromptProperties properties) {
    this.properties = properties;
  }

  public String buildPrompt(String agentName, Path projectRoot, String userRequest, String context) {
    String orchestratorGlobal = readPrompt(orchestratorGlobalPath());
    String orchestratorAgent = readPrompt(orchestratorAgentPath(agentName));
    String projectGlobal = readPrompt(projectGlobalPath(projectRoot));
    String projectAgent = readPrompt(projectAgentPath(projectRoot, agentName));

    StringBuilder prompt = new StringBuilder();
    append(prompt, "GLOBAL_BASE", orchestratorGlobal);
    append(prompt, "AGENT_BASE", orchestratorAgent);
    append(prompt, "PROJECT_GLOBAL", projectGlobal);
    append(prompt, "PROJECT_AGENT", projectAgent);

    if (prompt.isEmpty()) {
      append(prompt, "DEFAULT", defaultPrompt(agentName));
    }

    prompt.append("\n[USER_REQUEST]\n")
        .append(userRequest)
        .append("\n\n[CONTEXT]\n")
        .append(context);

    return prompt.toString();
  }

  private void append(StringBuilder sb, String title, String text) {
    if (text == null || text.isBlank()) {
      return;
    }
    sb.append("\n[").append(title).append("]\n").append(text.strip()).append("\n");
  }

  private Path orchestratorGlobalPath() {
    return Path.of(properties.getOrchestratorRootDir())
        .resolve(properties.getOrchestratorPromptsDir())
        .resolve(properties.getGlobalFile())
        .toAbsolutePath()
        .normalize();
  }

  private Path orchestratorAgentPath(String agentName) {
    return Path.of(properties.getOrchestratorRootDir())
        .resolve(properties.getOrchestratorPromptsDir())
        .resolve(properties.getAgentsDir())
        .resolve(agentName + ".md")
        .toAbsolutePath()
        .normalize();
  }

  private Path projectGlobalPath(Path projectRoot) {
    return projectRoot
        .resolve(properties.getProjectPromptsDir())
        .resolve(properties.getGlobalFile())
        .toAbsolutePath()
        .normalize();
  }

  private Path projectAgentPath(Path projectRoot, String agentName) {
    return projectRoot
        .resolve(properties.getProjectPromptsDir())
        .resolve(properties.getAgentsDir())
        .resolve(agentName + ".md")
        .toAbsolutePath()
        .normalize();
  }

  private String readPrompt(Path path) {
    if (!Files.exists(path) || !Files.isRegularFile(path)) {
      return "";
    }

    try {
      String text = Files.readString(path, StandardCharsets.UTF_8);
      if (text.length() <= properties.getMaxCharsPerPrompt()) {
        return text;
      }
      return text.substring(0, properties.getMaxCharsPerPrompt()) + "\n...(truncated)...";
    } catch (IOException e) {
      return "";
    }
  }

  private String defaultPrompt(String agentName) {
    return """
        You are DevAgent %s.
        Follow project rules. If uncertain, write assumptions explicitly.
        Output concise, implementation-focused results.
        """.formatted(agentName);
  }
}
