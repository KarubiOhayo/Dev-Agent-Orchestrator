package me.karubidev.devagent.orchestration.routing;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devagent.model-routing")
public class ModelRoutingProperties {

	private RoutingMode defaultMode = RoutingMode.BALANCED;
	private Map<String, AgentPolicy> agents = new LinkedHashMap<>();
	private EscalationPolicy escalation = new EscalationPolicy();

	public RoutingMode getDefaultMode() {
		return defaultMode;
	}

	public void setDefaultMode(RoutingMode defaultMode) {
		this.defaultMode = defaultMode;
	}

	public Map<String, AgentPolicy> getAgents() {
		return agents;
	}

	public void setAgents(Map<String, AgentPolicy> agents) {
		this.agents = agents;
	}

	public EscalationPolicy getEscalation() {
		return escalation;
	}

	public void setEscalation(EscalationPolicy escalation) {
		this.escalation = escalation;
	}

	public static class AgentPolicy {

		private Map<RoutingMode, ModePolicy> modes = new EnumMap<>(RoutingMode.class);

		public Map<RoutingMode, ModePolicy> getModes() {
			return modes;
		}

		public void setModes(Map<RoutingMode, ModePolicy> modes) {
			this.modes = modes;
		}
	}

	public static class ModePolicy {

		private String primary;
		private List<String> fallbacks = new ArrayList<>();

		public String getPrimary() {
			return primary;
		}

		public void setPrimary(String primary) {
			this.primary = primary;
		}

		public List<String> getFallbacks() {
			return fallbacks;
		}

		public void setFallbacks(List<String> fallbacks) {
			this.fallbacks = fallbacks;
		}
	}

	public static class EscalationPolicy {

		private String reviewHighRisk;
		private String hugeContext;
		private String strictJson;

		public String getReviewHighRisk() {
			return reviewHighRisk;
		}

		public void setReviewHighRisk(String reviewHighRisk) {
			this.reviewHighRisk = reviewHighRisk;
		}

		public String getHugeContext() {
			return hugeContext;
		}

		public void setHugeContext(String hugeContext) {
			this.hugeContext = hugeContext;
		}

		public String getStrictJson() {
			return strictJson;
		}

		public void setStrictJson(String strictJson) {
			this.strictJson = strictJson;
		}
	}
}
