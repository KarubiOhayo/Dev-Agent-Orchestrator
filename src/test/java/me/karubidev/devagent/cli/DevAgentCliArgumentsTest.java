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
}
