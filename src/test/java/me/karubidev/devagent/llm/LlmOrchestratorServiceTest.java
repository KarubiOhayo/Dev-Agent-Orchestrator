package me.karubidev.devagent.llm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.karubidev.devagent.orchestration.routing.AgentType;
import me.karubidev.devagent.orchestration.routing.ModelRef;
import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import me.karubidev.devagent.orchestration.routing.RoutingMode;
import org.junit.jupiter.api.Test;

class LlmOrchestratorServiceTest {

  @Test
  void fallbackModelIsUsedWhenPrimaryFails() {
    LlmApiProperties properties = new LlmApiProperties();
    properties.getGeneration().setMaxTokens(1000);
    properties.getGeneration().setTemperature(0.1);

    LlmProviderClient openAiFailing = new LlmProviderClient() {
      @Override
      public String provider() {
        return "openai";
      }

      @Override
      public boolean supports(String providerName) {
        return "openai".equalsIgnoreCase(providerName);
      }

      @Override
      public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
        throw new LlmProviderException("simulated fail");
      }
    };

    LlmProviderClient googleSuccess = new LlmProviderClient() {
      @Override
      public String provider() {
        return "google";
      }

      @Override
      public boolean supports(String providerName) {
        return "google".equalsIgnoreCase(providerName);
      }

      @Override
      public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
        return new LlmGenerationResult("google", model, "ok", "{}");
      }
    };

    LlmOrchestratorService service = new LlmOrchestratorService(List.of(openAiFailing, googleSuccess), properties);

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("openai", "gpt-5.2-codex"),
        List.of(new ModelRef("google", "gemini-2.5-pro")),
        List.of("test")
    );

    LlmExecutionResult result = service.generate(routeDecision, "hello");

    assertThat(result.result().provider()).isEqualTo("google");
    assertThat(result.result().model()).isEqualTo("gemini-2.5-pro");
    assertThat(result.attempts()).hasSize(2);
    assertThat(result.attempts().get(0).success()).isFalse();
    assertThat(result.attempts().get(1).success()).isTrue();
  }

  @Test
  void fallbackContinuesWhenPrimaryFailsWithGeminiNoTextDiagnostic() {
    LlmApiProperties properties = new LlmApiProperties();
    properties.getGeneration().setMaxTokens(1000);
    properties.getGeneration().setTemperature(0.1);

    LlmProviderClient googleNoText = new LlmProviderClient() {
      @Override
      public String provider() {
        return "google";
      }

      @Override
      public boolean supports(String providerName) {
        return "google".equalsIgnoreCase(providerName);
      }

      @Override
      public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
        throw new LlmProviderException(
            "Google response did not contain text output (finishReason=SAFETY, partTypes=functionCall)");
      }
    };

    LlmProviderClient openAiSuccess = new LlmProviderClient() {
      @Override
      public String provider() {
        return "openai";
      }

      @Override
      public boolean supports(String providerName) {
        return "openai".equalsIgnoreCase(providerName);
      }

      @Override
      public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
        return new LlmGenerationResult("openai", model, "ok", "{}");
      }
    };

    LlmOrchestratorService service = new LlmOrchestratorService(List.of(googleNoText, openAiSuccess), properties);

    RouteDecision routeDecision = new RouteDecision(
        AgentType.CODE,
        RoutingMode.BALANCED,
        RiskLevel.MEDIUM,
        new ModelRef("google", "gemini-2.5-pro"),
        List.of(new ModelRef("openai", "gpt-5.2")),
        List.of("test")
    );

    LlmExecutionResult result = service.generate(routeDecision, "hello");

    assertThat(result.result().provider()).isEqualTo("openai");
    assertThat(result.attempts()).hasSize(2);
    assertThat(result.attempts().get(0).success()).isFalse();
    assertThat(result.attempts().get(0).message()).contains("finishReason=SAFETY");
    assertThat(result.attempts().get(1).success()).isTrue();
  }
}
