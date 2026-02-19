package me.karubidev.devagent.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.agents.spec.SpecGenerateResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class CliResultFormatterTest {

  @Test
  void formatGenerateRendersTableAndFileListForDryRun() {
    CliResultFormatter formatter = new CliResultFormatter(new ObjectMapper());
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-123",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null
    );

    String output = formatter.formatGenerate(response);

    assertThat(output).contains("run-123");
    assertThat(output).contains("openai:gpt-5.2-codex");
    assertThat(output).contains("parsedFiles");
    assertThat(output).contains("applyOutcome");
    assertThat(output).contains("DRY_RUN");
    assertThat(output).contains("chainedDoc");
    assertThat(output).contains("chainedReview");
    assertThat(output).contains("chainFailures");
    assertThat(output).doesNotContain("[warning] chainFailures detected");
    assertThat(output).contains("- DRY_RUN src/main/java/AuthController.java (planned)");
  }

  @Test
  void formatGenerateJsonRendersStableFields() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CliResultFormatter formatter = new CliResultFormatter(mapper);
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-123",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null
    );

    String output = formatter.formatGenerateJson(response);
    var json = mapper.readTree(output);

    assertThat(json.path("ok").asBoolean()).isTrue();
    assertThat(json.path("command").asText()).isEqualTo("generate");
    assertThat(json.path("runId").asText()).isEqualTo("run-123");
    assertThat(json.path("model").path("id").asText()).isEqualTo("openai:gpt-5.2-codex");
    assertThat(json.path("data").path("summary").path("applyOutcome").asText()).isEqualTo("DRY_RUN");
    assertThat(json.path("data").path("summary").path("chainedDoc").asBoolean()).isFalse();
    assertThat(json.path("data").path("summary").path("chainedReview").asBoolean()).isFalse();
    assertThat(json.path("data").path("summary").path("chainFailures").asInt()).isZero();
    assertThat(json.path("data").path("hasChainFailures").asBoolean()).isFalse();
    assertThat(json.path("data").path("guardrailTriggered").asBoolean()).isFalse();
    assertThat(json.path("data").path("fileResults")).hasSize(1);
    assertThat(json.path("data").path("chainFailures")).hasSize(0);
    assertThat(json.path("error").isNull()).isTrue();
  }

  @Test
  void formatSpecJsonIncludesSpecAndOptionalChainedCode() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CliResultFormatter formatter = new CliResultFormatter(mapper);

    ObjectNode spec = mapper.createObjectNode();
    spec.put("title", "로그인 명세");

    SpecGenerateResponse response = new SpecGenerateResponse(
        "spec-run-1",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2",
        spec,
        List.of(),
        List.of(),
        "summary",
        null
    );

    String output = formatter.formatSpecJson(response);
    var json = mapper.readTree(output);

    assertThat(json.path("ok").asBoolean()).isTrue();
    assertThat(json.path("command").asText()).isEqualTo("spec");
    assertThat(json.path("data").path("summary").path("specKeys").asInt()).isEqualTo(1);
    assertThat(json.path("data").path("summary").path("chainedCode").asBoolean()).isFalse();
    assertThat(json.path("data").path("summary").path("chainedDoc").asBoolean()).isFalse();
    assertThat(json.path("data").path("summary").path("chainedReview").asBoolean()).isFalse();
    assertThat(json.path("data").path("summary").path("chainFailures").asInt()).isZero();
    assertThat(json.path("data").path("hasChainFailures").asBoolean()).isFalse();
    assertThat(json.path("data").path("guardrailTriggered").asBoolean()).isFalse();
    assertThat(json.path("data").path("spec").path("title").asText()).isEqualTo("로그인 명세");
    assertThat(json.path("data").path("chainedCode").isNull()).isTrue();
    assertThat(json.path("data").path("chainFailures")).hasSize(0);
  }

  @Test
  void formatGenerateJsonIncludesStructuredChainFailures() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CliResultFormatter formatter = new CliResultFormatter(mapper);
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-456",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null,
        List.of(new CodeGenerateResponse.ChainFailure("DOC", "CHAIN_DOC", "doc failure"))
    );

    String output = formatter.formatGenerateJson(response);
    var json = mapper.readTree(output);

    assertThat(json.path("data").path("summary").path("chainFailures").asInt()).isEqualTo(1);
    assertThat(json.path("data").path("hasChainFailures").asBoolean()).isTrue();
    assertThat(json.path("data").path("guardrailTriggered").asBoolean()).isFalse();
    assertThat(json.path("data").path("chainFailures")).hasSize(1);
    assertThat(json.path("data").path("chainFailures").get(0).path("agent").asText()).isEqualTo("DOC");
    assertThat(json.path("data").path("chainFailures").get(0).path("failedStage").asText()).isEqualTo("CHAIN_DOC");
    assertThat(json.path("data").path("chainFailures").get(0).path("errorMessage").asText()).isEqualTo("doc failure");
  }

  @Test
  void formatGenerateJsonSetsGuardrailTriggeredWhenEnabledAndFailuresExist() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CliResultFormatter formatter = new CliResultFormatter(mapper);
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-456",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null,
        List.of(new CodeGenerateResponse.ChainFailure("DOC", "CHAIN_DOC", "doc failure"))
    );

    String output = formatter.formatGenerateJson(response, true);
    var json = mapper.readTree(output);

    assertThat(json.path("data").path("guardrailTriggered").asBoolean()).isTrue();
  }

  @Test
  void formatSpecJsonSetsGuardrailTriggeredWhenEnabledAndChainedCodeFailuresExist() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CliResultFormatter formatter = new CliResultFormatter(mapper);

    ObjectNode spec = mapper.createObjectNode();
    spec.put("title", "로그인 명세");
    CodeGenerateResponse chainedCode = new CodeGenerateResponse(
        "run-456",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null,
        List.of(new CodeGenerateResponse.ChainFailure("DOC", "CHAIN_DOC", "doc failure"))
    );
    SpecGenerateResponse response = new SpecGenerateResponse(
        "spec-run-1",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2",
        spec,
        List.of(),
        List.of(),
        "summary",
        chainedCode
    );

    String output = formatter.formatSpecJson(response, true);
    var json = mapper.readTree(output);

    assertThat(json.path("data").path("guardrailTriggered").asBoolean()).isTrue();
  }

  @Test
  void formatGenerateRendersChainFailureWarningWhenFailuresExist() {
    CliResultFormatter formatter = new CliResultFormatter(new ObjectMapper());
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-456",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null,
        List.of(new CodeGenerateResponse.ChainFailure("DOC", "CHAIN_DOC", "doc failure"))
    );

    String output = formatter.formatGenerate(response);

    assertThat(output)
        .contains("[warning] chainFailures detected: 1")
        .contains("guardrail=disabled");
  }

  @Test
  void formatGenerateRendersChainFailureWarningWhenGuardrailEnabled() {
    CliResultFormatter formatter = new CliResultFormatter(new ObjectMapper());
    CodeGenerateResponse response = new CodeGenerateResponse(
        "run-456",
        "demo-auth",
        ".",
        null,
        "openai",
        "gpt-5.2-codex",
        "{}",
        List.of(),
        List.of(),
        "summary",
        List.of(new GeneratedFile("src/main/java/AuthController.java", "class AuthController {}")),
        new FileApplyResult(
            true,
            1,
            0,
            0,
            List.of(new FileApplyItem("src/main/java/AuthController.java", "DRY_RUN", "planned"))
        ),
        null,
        null,
        List.of(new CodeGenerateResponse.ChainFailure("DOC", "CHAIN_DOC", "doc failure"))
    );

    String output = formatter.formatGenerate(response, true);

    assertThat(output).contains("guardrail=enabled");
  }

  @Test
  void formatErrorJsonRendersConsistentEnvelope() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CliResultFormatter formatter = new CliResultFormatter(mapper);

    String output = formatter.formatErrorJson("generate", 2, "지원하지 않는 옵션입니다");
    var json = mapper.readTree(output);

    assertThat(json.path("ok").asBoolean()).isFalse();
    assertThat(json.path("command").asText()).isEqualTo("generate");
    assertThat(json.path("runId").isNull()).isTrue();
    assertThat(json.path("model").isNull()).isTrue();
    assertThat(json.path("data").isNull()).isTrue();
    assertThat(json.path("error").path("exitCode").asInt()).isEqualTo(2);
    assertThat(json.path("error").path("message").asText()).contains("지원하지 않는 옵션");
  }
}
