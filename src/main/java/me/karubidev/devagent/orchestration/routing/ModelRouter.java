package me.karubidev.devagent.orchestration.routing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ModelRouter {

	private final ModelRoutingProperties properties;

	public ModelRouter(ModelRoutingProperties properties) {
		this.properties = properties;
	}

	public RouteDecision resolve(RouteRequest request) {
		if (request == null || request.getAgentType() == null) {
			throw new IllegalArgumentException("agentType is required");
		}

		RoutingMode requestedMode = request.getMode() == null ? properties.getDefaultMode() : request.getMode();
		RiskLevel riskLevel = request.getRiskLevel() == null ? RiskLevel.MEDIUM : request.getRiskLevel();

		ModelRoutingProperties.AgentPolicy agentPolicy = findAgentPolicy(request.getAgentType());
		ModelRoutingProperties.ModePolicy modePolicy = findModePolicy(agentPolicy, requestedMode);

		LinkedHashSet<ModelRef> candidateSet = new LinkedHashSet<>();
		List<String> reasons = new ArrayList<>();

		applyEscalationRules(request, riskLevel, candidateSet, reasons);

		addCandidate(candidateSet, modePolicy.getPrimary());
		for (String fallback : modePolicy.getFallbacks()) {
			addCandidate(candidateSet, fallback);
		}

		if (candidateSet.isEmpty()) {
			throw new IllegalStateException("No model candidates found for agent " + request.getAgentType());
		}

		List<ModelRef> ordered = new ArrayList<>(candidateSet);
		ModelRef primary = ordered.remove(0);
		if (reasons.isEmpty()) {
			reasons.add("mode policy applied: " + requestedMode);
		}

		return new RouteDecision(request.getAgentType(), requestedMode, riskLevel, primary, ordered, reasons);
	}

	private void applyEscalationRules(
		RouteRequest request,
		RiskLevel riskLevel,
		LinkedHashSet<ModelRef> candidateSet,
		List<String> reasons
	) {
		ModelRoutingProperties.EscalationPolicy escalation = properties.getEscalation();

		if (request.getAgentType() == AgentType.REVIEW && riskLevel == RiskLevel.HIGH) {
			if (addCandidate(candidateSet, escalation.getReviewHighRisk())) {
				reasons.add("high-risk review escalation");
			}
		}

		if (request.isLargeContext()) {
			if (addCandidate(candidateSet, escalation.getHugeContext())) {
				reasons.add("large-context escalation");
			}
		}

		if (request.isStrictJsonRequired()) {
			if (addCandidate(candidateSet, escalation.getStrictJson())) {
				reasons.add("strict-json escalation");
			}
		}
	}

	private ModelRoutingProperties.AgentPolicy findAgentPolicy(AgentType agentType) {
		String key = agentType.name().toLowerCase(Locale.ROOT);
		ModelRoutingProperties.AgentPolicy policy = properties.getAgents().get(key);
		if (policy == null) {
			throw new IllegalStateException("No agent policy found for " + key);
		}
		return policy;
	}

	private ModelRoutingProperties.ModePolicy findModePolicy(
		ModelRoutingProperties.AgentPolicy agentPolicy,
		RoutingMode requestedMode
	) {
		Map<RoutingMode, ModelRoutingProperties.ModePolicy> modes = agentPolicy.getModes();
		ModelRoutingProperties.ModePolicy modePolicy = modes.get(requestedMode);
		if (modePolicy != null) {
			return modePolicy;
		}

		ModelRoutingProperties.ModePolicy defaultModePolicy = modes.get(properties.getDefaultMode());
		if (defaultModePolicy != null) {
			return defaultModePolicy;
		}

		return modes.values().stream()
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No mode policy configured"));
	}

	private boolean addCandidate(LinkedHashSet<ModelRef> candidateSet, String rawModelRef) {
		if (rawModelRef == null || rawModelRef.isBlank()) {
			return false;
		}
		return candidateSet.add(ModelRef.parse(rawModelRef));
	}
}
