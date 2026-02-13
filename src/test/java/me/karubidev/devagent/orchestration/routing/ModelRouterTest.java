package me.karubidev.devagent.orchestration.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ModelRouterTest {

	@Autowired
	private ModelRouter modelRouter;

	@Test
	void resolveRejectsNullRequest() {
		assertThatThrownBy(() -> modelRouter.resolve(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("agentType is required");
	}

	@Test
	void resolveRejectsNullAgentType() {
		RouteRequest request = new RouteRequest();
		request.setMode(RoutingMode.BALANCED);

		assertThatThrownBy(() -> modelRouter.resolve(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("agentType is required");
	}

	@Test
	void routeRouterCostSaverUsesFlashLite() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.ROUTER);
		request.setMode(RoutingMode.COST_SAVER);
		request.setStrictJsonRequired(false);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("google");
		assertThat(decision.primary().model()).isEqualTo("gemini-2.5-flash-lite");
	}

	@Test
	void routeCodeBalancedWithoutStrictJsonEscalationUsesCodexByDefault() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.CODE);
		request.setMode(RoutingMode.BALANCED);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("openai");
		assertThat(decision.primary().model()).isEqualTo("gpt-5.2-codex");
		assertThat(decision.reasons()).doesNotContain("strict-json escalation");
	}

	@Test
	void routeCodeBalancedNullStrictJsonDoesNotEscalate() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.CODE);
		request.setMode(RoutingMode.BALANCED);
		request.setStrictJsonRequired(null);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("openai");
		assertThat(decision.primary().model()).isEqualTo("gpt-5.2-codex");
		assertThat(decision.reasons()).doesNotContain("strict-json escalation");
	}

	@Test
	void routeCodeBalancedWithStrictJsonEscalatesToStrictJsonModel() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.CODE);
		request.setMode(RoutingMode.BALANCED);
		request.setStrictJsonRequired(true);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("openai");
		assertThat(decision.primary().model()).isEqualTo("gpt-5.2");
		assertThat(decision.fallbacks())
			.extracting(ModelRef::provider, ModelRef::model)
			.containsExactly(
				tuple("openai", "gpt-5.2-codex"),
				tuple("anthropic", "claude-sonnet-4.5"),
				tuple("google", "gemini-2.5-pro")
			);
		assertThat(decision.reasons()).containsExactly("strict-json escalation");
	}

	@Test
	void highRiskReviewEscalatesToOpus() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.REVIEW);
		request.setMode(RoutingMode.BALANCED);
		request.setRiskLevel(RiskLevel.HIGH);
		request.setStrictJsonRequired(false);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("anthropic");
		assertThat(decision.primary().model()).isEqualTo("claude-opus-4.6");
		assertThat(decision.reasons()).contains("high-risk review escalation");
	}

	@Test
	void reviewHighRiskStrictJsonAndLargeContextPreservesEscalationPriorityOrder() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.REVIEW);
		request.setMode(RoutingMode.BALANCED);
		request.setRiskLevel(RiskLevel.HIGH);
		request.setStrictJsonRequired(true);
		request.setLargeContext(true);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("anthropic");
		assertThat(decision.primary().model()).isEqualTo("claude-opus-4.6");
		assertThat(decision.fallbacks())
			.extracting(ModelRef::provider, ModelRef::model)
			.containsExactly(
				tuple("openai", "gpt-5.2"),
				tuple("google", "gemini-2.5-pro"),
				tuple("anthropic", "claude-sonnet-4.5")
			);
		assertThat(decision.reasons())
			.containsExactly(
				"high-risk review escalation",
				"strict-json escalation",
				"large-context escalation"
			);
	}

	@Test
	void routeDocGemini3CanaryUsesPreview() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.DOC);
		request.setMode(RoutingMode.GEMINI3_CANARY);
		request.setStrictJsonRequired(false);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("google");
		assertThat(decision.primary().model()).isEqualTo("gemini-3.0-preview");
	}

	@Test
	void routeRefactorBalancedDefaultsToCodex() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.REFACTOR);
		request.setMode(RoutingMode.BALANCED);
		request.setStrictJsonRequired(false);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("openai");
		assertThat(decision.primary().model()).isEqualTo("gpt-5.2-codex");
	}

	@Test
	void unsupportedCanaryFallsBackToDefaultModePolicy() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.REVIEW);
		request.setMode(RoutingMode.GEMINI3_CANARY);
		request.setStrictJsonRequired(false);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("anthropic");
		assertThat(decision.primary().model()).isEqualTo("claude-sonnet-4.5");
	}
}
