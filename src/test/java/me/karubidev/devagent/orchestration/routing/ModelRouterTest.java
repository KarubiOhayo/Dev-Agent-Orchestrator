package me.karubidev.devagent.orchestration.routing;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ModelRouterTest {

	@Autowired
	private ModelRouter modelRouter;

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
	void routeCodeBalancedDefaultsToGpt52() {
		RouteRequest request = new RouteRequest();
		request.setAgentType(AgentType.CODE);
		request.setMode(RoutingMode.BALANCED);
		request.setStrictJsonRequired(false);

		RouteDecision decision = modelRouter.resolve(request);

		assertThat(decision.primary().provider()).isEqualTo("openai");
		assertThat(decision.primary().model()).isEqualTo("gpt-5.2-codex");
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
