package me.karubidev.devagent.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devagent.llm")
public class LlmApiProperties {

  private int requestTimeoutSeconds = 60;
  private Generation generation = new Generation();
  private Providers providers = new Providers();

  public int getRequestTimeoutSeconds() {
    return requestTimeoutSeconds;
  }

  public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
    this.requestTimeoutSeconds = requestTimeoutSeconds;
  }

  public Generation getGeneration() {
    return generation;
  }

  public void setGeneration(Generation generation) {
    this.generation = generation;
  }

  public Providers getProviders() {
    return providers;
  }

  public void setProviders(Providers providers) {
    this.providers = providers;
  }

  public static class Generation {

    private int maxTokens = 1600;
    private double temperature = 0.2;

    public int getMaxTokens() {
      return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
      this.maxTokens = maxTokens;
    }

    public double getTemperature() {
      return temperature;
    }

    public void setTemperature(double temperature) {
      this.temperature = temperature;
    }
  }

  public static class Providers {

    private Provider openai = new Provider();
    private AnthropicProvider anthropic = new AnthropicProvider();
    private Provider google = new Provider();

    public Provider getOpenai() {
      return openai;
    }

    public void setOpenai(Provider openai) {
      this.openai = openai;
    }

    public AnthropicProvider getAnthropic() {
      return anthropic;
    }

    public void setAnthropic(AnthropicProvider anthropic) {
      this.anthropic = anthropic;
    }

    public Provider getGoogle() {
      return google;
    }

    public void setGoogle(Provider google) {
      this.google = google;
    }
  }

  public static class Provider {

    private boolean enabled = true;
    private String baseUrl;
    private String apiKey;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }
  }

  public static class AnthropicProvider extends Provider {

    private String apiVersion = "2023-06-01";

    public String getApiVersion() {
      return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
      this.apiVersion = apiVersion;
    }
  }
}
