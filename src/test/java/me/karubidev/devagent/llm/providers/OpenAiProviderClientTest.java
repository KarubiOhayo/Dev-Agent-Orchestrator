package me.karubidev.devagent.llm.providers;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import me.karubidev.devagent.llm.LlmApiProperties;
import me.karubidev.devagent.llm.LlmGenerationOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OpenAiProviderClientTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AtomicReference<String> capturedRequestBody = new AtomicReference<>();
  private HttpServer server;
  private String baseUrl;

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/v1/responses", exchange -> {
      capturedRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      byte[] response = "{\"output_text\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
      exchange.getResponseBody().write(response);
      exchange.close();
    });
    server.start();
    baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
  }

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void omitsTemperatureForCodexModels() throws Exception {
    OpenAiProviderClient client = new OpenAiProviderClient(buildProperties(), objectMapper);

    client.generate("gpt-5.2-codex", "hello", new LlmGenerationOptions(256, 0.2));

    var requestBody = objectMapper.readTree(capturedRequestBody.get());
    assertThat(requestBody.path("model").asText()).isEqualTo("gpt-5.2-codex");
    assertThat(requestBody.has("temperature")).isFalse();
  }

  @Test
  void keepsTemperatureForNonCodexModels() throws Exception {
    OpenAiProviderClient client = new OpenAiProviderClient(buildProperties(), objectMapper);

    client.generate("gpt-5.2", "hello", new LlmGenerationOptions(256, 0.2));

    var requestBody = objectMapper.readTree(capturedRequestBody.get());
    assertThat(requestBody.path("model").asText()).isEqualTo("gpt-5.2");
    assertThat(requestBody.path("temperature").asDouble()).isEqualTo(0.2d);
  }

  private LlmApiProperties buildProperties() {
    LlmApiProperties properties = new LlmApiProperties();
    properties.setRequestTimeoutSeconds(5);
    LlmApiProperties.Provider openAi = properties.getProviders().getOpenai();
    openAi.setEnabled(true);
    openAi.setBaseUrl(baseUrl);
    openAi.setApiKey("test-key");
    return properties;
  }
}
