package me.karubidev.devagent.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptRegistryTest {

  @Test
  void buildPromptIncludesAgentBaseForSpecDocAndReview() {
    PromptRegistry registry = new PromptRegistry(defaultProperties());
    Map<String, String> expectedContracts = new LinkedHashMap<>();
    expectedContracts.put("spec", "SPEC_AGENT_SCHEMA_V1");
    expectedContracts.put("doc", "DOC_AGENT_SCHEMA_V1");
    expectedContracts.put("review", "REVIEW_AGENT_SCHEMA_V1");

    expectedContracts.forEach((agent, contractId) -> {
      String prompt = registry.buildPrompt(
          agent,
          Path.of(".").toAbsolutePath().normalize(),
          "request",
          "context"
      );

      assertThat(prompt).contains("[AGENT_BASE]");
      assertThat(prompt).contains(contractId);
    });
  }

  @Test
  void buildPromptKeepsCodeAgentFilesContract() {
    PromptRegistry registry = new PromptRegistry(defaultProperties());

    String prompt = registry.buildPrompt(
        "code",
        Path.of(".").toAbsolutePath().normalize(),
        "request",
        "context"
    );

    assertThat(prompt).contains("[AGENT_BASE]");
    assertThat(prompt).contains("\"files\"");
    assertThat(prompt).contains("`files[].path`");
  }

  private PromptProperties defaultProperties() {
    PromptProperties properties = new PromptProperties();
    properties.setOrchestratorRootDir(".");
    properties.setOrchestratorPromptsDir("prompts");
    properties.setProjectPromptsDir(".devagent/prompts");
    properties.setGlobalFile("global.md");
    properties.setAgentsDir("agents");
    properties.setMaxCharsPerPrompt(8000);
    return properties;
  }
}
