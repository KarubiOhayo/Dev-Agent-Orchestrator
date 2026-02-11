package me.karubidev.devagent.llm.providers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
public class GoogleGeminiProviderClient implements LlmProviderClient {

  private final LlmApiProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public GoogleGeminiProviderClient(LlmApiProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
        .build();
  }

  @Override
  public String provider() {
    return "google";
  }

  @Override
  public boolean supports(String providerName) {
    return provider().equalsIgnoreCase(providerName);
  }

  @Override
  public LlmGenerationResult generate(String model, String prompt, LlmGenerationOptions options) {
    LlmApiProperties.Provider config = properties.getProviders().getGoogle();
    if (!config.isEnabled()) {
      throw new LlmProviderException("Google provider is disabled");
    }
    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
      throw new LlmProviderException("Google API key is missing");
    }

    String baseUrl = trimTrailingSlash(config.getBaseUrl());
    String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
    String encodedKey = URLEncoder.encode(config.getApiKey(), StandardCharsets.UTF_8);
    URI uri = URI.create(baseUrl + "/v1beta/models/" + encodedModel + ":generateContent?key=" + encodedKey);

    Map<String, Object> requestBody = new LinkedHashMap<>();
    requestBody.put("contents", List.of(
        Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
    ));
    requestBody.put("generationConfig", Map.of(
        "temperature", options.temperature(),
        "maxOutputTokens", options.maxTokens()
    ));

    return doRequest(uri, requestBody, model);
  }

  private LlmGenerationResult doRequest(URI uri, Map<String, Object> requestBody, String model) {
    try {
      String jsonBody = objectMapper.writeValueAsString(requestBody);
      HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Content-Type", "application/json")
          .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        throw new LlmProviderException("Google HTTP " + response.statusCode() + ": " + truncate(response.body()));
      }

      JsonNode root = objectMapper.readTree(response.body());
      String text = LlmJsonExtractor.geminiText(root);
      return new LlmGenerationResult(provider(), model, text, response.body());
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new LlmProviderException("Google call failed", e);
    }
  }

  private String trimTrailingSlash(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new LlmProviderException("Google baseUrl is missing");
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
