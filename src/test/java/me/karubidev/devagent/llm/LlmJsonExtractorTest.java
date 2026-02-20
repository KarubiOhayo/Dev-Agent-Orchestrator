package me.karubidev.devagent.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class LlmJsonExtractorTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void geminiTextIncludesDiagnosticWhenTextPartIsMissing() throws Exception {
    var root = objectMapper.readTree("""
        {
          "candidates": [
            {
              "finishReason": "SAFETY",
              "content": {
                "parts": [
                  {"functionCall": {"name": "lookupUser"}}
                ]
              }
            }
          ]
        }
        """);

    assertThatThrownBy(() -> LlmJsonExtractor.geminiText(root))
        .isInstanceOf(LlmProviderException.class)
        .hasMessageContaining("finishReason=SAFETY")
        .hasMessageContaining("partTypes=functionCall");
  }

  @Test
  void geminiTextReturnsConcatenatedTextWhenPresent() throws Exception {
    var root = objectMapper.readTree("""
        {
          "candidates": [
            {
              "finishReason": "STOP",
              "content": {
                "parts": [
                  {"text": "line-1"},
                  {"text": "line-2"}
                ]
              }
            }
          ]
        }
        """);

    String text = LlmJsonExtractor.geminiText(root);

    assertThat(text).isEqualTo("line-1\nline-2");
  }
}
