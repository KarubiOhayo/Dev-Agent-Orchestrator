package me.karubidev.devagent.orchestration.routing;

import java.util.List;

public record RouteDecision(
	AgentType agentType,
	RoutingMode mode,
	RiskLevel riskLevel,
	ModelRef primary,
	List<ModelRef> fallbacks,
	List<String> reasons
) {
}
