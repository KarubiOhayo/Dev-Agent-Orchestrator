package me.karubidev.devagent.llm;

public interface LlmProviderClient {

  String provider();

  boolean supports(String providerName);

  LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options);
}
