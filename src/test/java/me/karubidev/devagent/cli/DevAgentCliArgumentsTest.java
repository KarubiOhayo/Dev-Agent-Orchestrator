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
