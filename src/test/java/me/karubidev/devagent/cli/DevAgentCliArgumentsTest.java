package me.karubidev.devagent.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RoutingMode;
import org.junit.jupiter.api.Test;

class DevAgentCliArgumentsTest {

  @Test
  void parseGenerateCommandAndOptions() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--user-request", "로그인 API 스켈레톤",
        "--apply",
        "--mode=quality",
        "--risk-level", "high"
    });

    assertThat(parsed.cliMode()).isTrue();
    assertThat(parsed.command()).isEqualTo("generate");
    assertThat(parsed.option("user-request")).isEqualTo("로그인 API 스켈레톤");
    assertThat(parsed.optionAsBoolean("apply", false)).isTrue();
    assertThat(parsed.optionAsEnum("mode", RoutingMode.class, RoutingMode.BALANCED)).isEqualTo(RoutingMode.QUALITY);
    assertThat(parsed.optionAsEnum("risk-level", RiskLevel.class, RiskLevel.MEDIUM)).isEqualTo(RiskLevel.HIGH);
  }

  @Test
  void parseGenerateSupportsDocReviewChainOptions() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--chain-to-doc", "true",
        "--doc-user-request", "문서를 생성해줘",
        "--chain-to-review=yes",
        "--review-user-request=리뷰를 생성해줘",
        "--chain-failure-policy=partial_success"
    });

    assertThat(parsed.optionAsBoolean("chain-to-doc", false)).isTrue();
    assertThat(parsed.option("doc-user-request")).isEqualTo("문서를 생성해줘");
    assertThat(parsed.optionAsBoolean("chain-to-review", false)).isTrue();
    assertThat(parsed.option("review-user-request")).isEqualTo("리뷰를 생성해줘");
    assertThat(parsed.optionAsEnum(
        "chain-failure-policy",
        me.karubidev.devagent.agents.code.CodeGenerateRequest.ChainFailurePolicy.class,
        me.karubidev.devagent.agents.code.CodeGenerateRequest.ChainFailurePolicy.FAIL_FAST
    )).isEqualTo(me.karubidev.devagent.agents.code.CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);
  }

  @Test
  void parseGenerateSupportsFailOnChainFailuresOption() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--fail-on-chain-failures", "true"
    });

    assertThat(parsed.optionAsBoolean("fail-on-chain-failures", false)).isTrue();
  }

  @Test
  void parseSpecSupportsCodePrefixedChainOptions() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "spec",
        "--code-chain-to-doc",
        "--code-doc-user-request", "코드 문서를 생성해줘",
        "--code-chain-to-review=1",
        "--code-review-user-request=코드 리뷰를 생성해줘",
        "--code-chain-failure-policy", "PARTIAL_SUCCESS"
    });

    assertThat(parsed.optionAsBoolean("code-chain-to-doc", false)).isTrue();
    assertThat(parsed.option("code-doc-user-request")).isEqualTo("코드 문서를 생성해줘");
    assertThat(parsed.optionAsBoolean("code-chain-to-review", false)).isTrue();
    assertThat(parsed.option("code-review-user-request")).isEqualTo("코드 리뷰를 생성해줘");
    assertThat(parsed.optionAsEnum(
        "code-chain-failure-policy",
        me.karubidev.devagent.agents.code.CodeGenerateRequest.ChainFailurePolicy.class,
        me.karubidev.devagent.agents.code.CodeGenerateRequest.ChainFailurePolicy.FAIL_FAST
    )).isEqualTo(me.karubidev.devagent.agents.code.CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);
  }

  @Test
  void parseReturnsNonCliModeForServerOptions() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "--spring.profiles.active=dev"
    });

    assertThat(parsed.cliMode()).isFalse();
  }

  @Test
  void parseRejectsUnknownCommandWhenPrefixed() {
    assertThatThrownBy(() -> DevAgentCliArguments.parse(new String[]{"devagent", "unknown"}))
        .isInstanceOf(CliCommandException.class)
        .hasMessageContaining("지원하지 않는 명령어");
  }

  @Test
  void parseSupportsRepresentativeAliases() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--project", "demo-auth",
        "-r", "./workspace",
        "-u", "요청",
        "-m", "quality",
        "--risk", "high",
        "-j"
    });

    assertThat(parsed.option("project-id")).isEqualTo("demo-auth");
    assertThat(parsed.option("target-root")).isEqualTo("./workspace");
    assertThat(parsed.option("user-request")).isEqualTo("요청");
    assertThat(parsed.option("mode")).isEqualTo("quality");
    assertThat(parsed.option("risk-level")).isEqualTo("high");
    assertThat(parsed.optionAsBoolean("json", false)).isTrue();
  }

  @Test
  void parseRejectsConflictingAliasValues() {
    assertThatThrownBy(() -> DevAgentCliArguments.parse(new String[]{
        "generate",
        "--project-id=demo-auth",
        "-p", "another-project"
    }))
        .isInstanceOf(CliCommandException.class)
        .hasMessageContaining("옵션 값이 충돌합니다");
  }

  @Test
  void parseAllowsEquivalentEnumAliasValues() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--mode=quality",
        "-m", "QUALITY",
        "--json"
    });

    assertThat(parsed.optionAsEnum("mode", RoutingMode.class, RoutingMode.BALANCED))
        .isEqualTo(RoutingMode.QUALITY);
    assertThat(parsed.optionAsBoolean("json", false)).isTrue();
  }

  @Test
  void parseAllowsEquivalentBooleanAliasValues() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--json=true",
        "-j", "yes"
    });

    assertThat(parsed.optionAsBoolean("json", false)).isTrue();
  }

  @Test
  void parseAllowsEquivalentBooleanValuesForFailOnChainFailures() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--fail-on-chain-failures=true",
        "--fail-on-chain-failures=yes"
    });

    assertThat(parsed.optionAsBoolean("fail-on-chain-failures", false)).isTrue();
  }

  @Test
  void parseConsumesDashPrefixedSeparatedValueForUserRequest() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--user-request", "-hello",
        "--json"
    });

    assertThat(parsed.option("user-request")).isEqualTo("-hello");
    assertThat(parsed.optionAsBoolean("json", false)).isTrue();
  }

  @Test
  void parseConsumesDashPrefixedSeparatedValueForTargetRoot() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--target-root", "-tmp"
    });

    assertThat(parsed.option("target-root")).isEqualTo("-tmp");
  }

  @Test
  void parseConsumesRiskLevelDashValueThenFailsEnumValidation() {
    DevAgentCliArguments parsed = DevAgentCliArguments.parse(new String[]{
        "generate",
        "--risk-level", "-1"
    });

    assertThat(parsed.option("risk-level")).isEqualTo("-1");
    assertThatThrownBy(() -> parsed.optionAsEnum("risk-level", RiskLevel.class, RiskLevel.MEDIUM))
        .isInstanceOf(CliCommandException.class)
        .hasMessageContaining("열거형 옵션 값이 올바르지 않습니다");
  }
}
