package me.karubidev.devagent.orchestration.routing;

public class RouteRequest {

	private AgentType agentType;
	private RoutingMode mode = RoutingMode.BALANCED;
	private RiskLevel riskLevel = RiskLevel.MEDIUM;
	private boolean largeContext;
	private Boolean strictJsonRequired;

	public AgentType getAgentType() {
		return agentType;
	}

	public void setAgentType(AgentType agentType) {
		this.agentType = agentType;
	}

	public RoutingMode getMode() {
		return mode;
	}

	public void setMode(RoutingMode mode) {
		this.mode = mode;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}

	public boolean isLargeContext() {
		return largeContext;
	}

	public void setLargeContext(boolean largeContext) {
		this.largeContext = largeContext;
	}

	public boolean isStrictJsonRequired() {
		return Boolean.TRUE.equals(strictJsonRequired);
	}

	public void setStrictJsonRequired(Boolean strictJsonRequired) {
		this.strictJsonRequired = strictJsonRequired;
	}

	public boolean isStrictJsonExplicitlyRequired() {
		return Boolean.TRUE.equals(strictJsonRequired);
	}
}
