package me.karubidev.devagent.llm;

import java.util.ArrayList;
import java.util.List;
import me.karubidev.devagent.orchestration.routing.ModelRef;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import org.springframework.stereotype.Service;

@Service
public class LlmOrchestratorService {

  private final List<LlmProviderClient> providers;
  private final LlmApiProperties properties;

  public LlmOrchestratorService(List<LlmProviderClient> providers, LlmApiProperties properties) {
    this.providers = providers;
    this.properties = properties;
  }

  public LlmExecutionResult generate(RouteDecision routeDecision, String prompt) {
    List<ModelRef> candidates = new ArrayList<>();
    candidates.add(routeDecision.primary());
    candidates.addAll(routeDecision.fallbacks());

    LlmGenerationOptions options = new LlmGenerationOptions(
        properties.getGeneration().getMaxTokens(),
        properties.getGeneration().getTemperature()
    );

    List<LlmAttempt> attempts = new ArrayList<>();
    for (ModelRef modelRef : candidates) {
      LlmProviderClient client = findProviderClient(modelRef.provider());
      if (client == null) {
        attempts.add(new LlmAttempt(modelRef.provider(), modelRef.model(), false, "provider not registered"));
        continue;
      }

      try {
        LlmGenerationResult result = client.generate(modelRef.model(), prompt, options);
        attempts.add(new LlmAttempt(modelRef.provider(), modelRef.model(), true, "ok"));
        return new LlmExecutionResult(result, attempts);
      } catch (Exception e) {
        attempts.add(new LlmAttempt(modelRef.provider(), modelRef.model(), false, messageOf(e)));
      }
    }

    throw new LlmProviderException("All model candidates failed: " + attempts);
  }

  private LlmProviderClient findProviderClient(String providerName) {
    return providers.stream()
        .filter(p -> p.supports(providerName))
        .findFirst()
        .orElse(null);
  }

  private String messageOf(Exception e) {
    if (e.getMessage() == null || e.getMessage().isBlank()) {
      return e.getClass().getSimpleName();
    }
    return e.getMessage();
  }
}
