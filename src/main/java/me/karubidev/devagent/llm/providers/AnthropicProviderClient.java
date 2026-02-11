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
import java.util.Map;
import me.karubidev.devagent.llm.LlmApiProperties;
import me.karubidev.devagent.llm.LlmGenerationOptions;
import me.karubidev.devagent.llm.LlmGenerationResult;
import me.karubidev.devagent.llm.LlmJsonExtractor;
import me.karubidev.devagent.llm.LlmProviderClient;
import me.karubidev.devagent.llm.LlmProviderException;
import org.springframework.stereotype.Component;

@Component
public class AnthropicProviderClient implements LlmProviderClient {

  private final LlmApiProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public AnthropicProviderClient(LlmApiProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
        .build();
  }

  @Override
  public String provider() {
    return "anthropic";
  }

  @Override
  public boolean supports(String providerName) {
    return provider().equalsIgnoreCase(providerName);
  }

  @Override
  public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
    LlmApiProperties.AnthropicProvider config = properties.getProviders().getAnthropic();
    if (!config.isEnabled()) {
      throw new LlmProviderException("Anthropic provider is disabled");
    }
    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
      throw new LlmProviderException("Anthropic API key is missing");
    }

    String baseUrl = trimTrailingSlash(config.getBaseUrl());
    URI uri = URI.create(baseUrl + "/v1/messages");

    Map<String, Object> requestBody = new LinkedHashMap<>();
    requestBody.put("model", model);
    requestBody.put("max_tokens", options.maxTokens());
    requestBody.put("temperature", options.temperature());
    requestBody.put("messages", List.of(
        Map.of(
            "role", "user",
            "content", List.of(Map.of("type", "text", "text", prompt))
        )
    ));

    return doRequest(uri, requestBody, config.getApiKey(), config.getApiVersion(), model);
  }

  private LlmGenerationResult doRequest(URI uri, Map<String, Object> requestBody, String apiKey, String version,
      String model) {
    try {
      String jsonBody = objectMapper.writeValueAsString(requestBody);
      HttpRequest request = HttpRequest.newBuilder(uri)
          .header("x-api-key", apiKey)
          .header("anthropic-version", version)
          .header("Content-Type", "application/json")
          .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        throw new LlmProviderException(
            "Anthropic HTTP " + response.statusCode() + ": " + truncate(response.body()));
      }

      JsonNode root = objectMapper.readTree(response.body());
      String text = LlmJsonExtractor.anthropicText(root);
      return new LlmGenerationResult(provider(), model, text, response.body());
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new LlmProviderException("Anthropic call failed", e);
    }
  }

  private String trimTrailingSlash(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new LlmProviderException("Anthropic baseUrl is missing");
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
