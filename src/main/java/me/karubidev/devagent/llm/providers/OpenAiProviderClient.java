package me.karubidev.devagent.llm.providers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.karubidev.devagent.llm.LlmApiProperties;
import me.karubidev.devagent.llm.LlmGenerationOptions;
import me.karubidev.devagent.llm.LlmGenerationResult;
import me.karubidev.devagent.llm.LlmJsonExtractor;
import me.karubidev.devagent.llm.LlmProviderClient;
import me.karubidev.devagent.llm.LlmProviderException;
import org.springframework.stereotype.Component;

@Component
public class OpenAiProviderClient implements LlmProviderClient {

  private final LlmApiProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public OpenAiProviderClient(LlmApiProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
        .build();
  }

  @Override
  public String provider() {
    return "openai";
  }

  @Override
  public boolean supports(String providerName) {
    return provider().equalsIgnoreCase(providerName);
  }

  @Override
  public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
    LlmApiProperties.Provider config = properties.getProviders().getOpenai();
    if (!config.isEnabled()) {
      throw new LlmProviderException("OpenAI provider is disabled");
    }
    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
      throw new LlmProviderException("OpenAI API key is missing");
    }

    String baseUrl = trimTrailingSlash(config.getBaseUrl());
    URI uri = URI.create(baseUrl + "/v1/responses");

    Map<String, Object> requestBody = new LinkedHashMap<>();
    requestBody.put("model", model);
    requestBody.put("max_output_tokens", options.maxTokens());
    if (shouldIncludeTemperature(model)) {
      requestBody.put("temperature", options.temperature());
    }
    requestBody.put("input", List.of(
        Map.of(
            "role", "user",
            "content", List.of(Map.of("type", "input_text", "text", prompt))
        )
    ));

    return doRequest(uri, requestBody, config.getApiKey(), model);
  }

  boolean shouldIncludeTemperature(String model) {
    return !isCodexModel(model);
  }

  boolean isCodexModel(String model) {
    return model != null && model.toLowerCase(Locale.ROOT).contains("codex");
  }

  private LlmGenerationResult doRequest(URI uri, Map<String, Object> requestBody, String apiKey, String model) {
    try {
      String jsonBody = objectMapper.writeValueAsString(requestBody);
      HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        throw new LlmProviderException("OpenAI HTTP " + response.statusCode() + ": " + truncate(response.body()));
      }

      JsonNode root = objectMapper.readTree(response.body());
      String text = LlmJsonExtractor.openAiText(root);
      return new LlmGenerationResult(provider(), model, text, response.body());
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new LlmProviderException("OpenAI call failed", e);
    }
  }

  private String trimTrailingSlash(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new LlmProviderException("OpenAI baseUrl is missing");
    }
    if (baseUrl.endsWith("/")) {
      return baseUrl.substring(0, baseUrl.length() - 1);
    }
    return baseUrl;
  }

  private String truncate(String body) {
    if (body == null) {
      return "";
    }
    if (body.length() <= 600) {
      return body;
    }
    return body.substring(0, 600) + "...";
  }
}
