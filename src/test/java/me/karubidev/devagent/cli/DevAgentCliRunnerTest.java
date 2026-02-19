package me.karubidev.devagent.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.agents.spec.SpecGenerateRequest;
import me.karubidev.devagent.agents.spec.SpecGenerateResponse;
import me.karubidev.devagent.agents.spec.SpecAgentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class DevAgentCliRunnerTest {

  @Test
  void runPrintsFriendlyMessageAndKeepsExitCodeForUnknownOption() {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        Mockito.mock(SpecAgentService.class),
        Mockito.mock(CliResultFormatter.class),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("generate", "--unknown-option=true"));

    String err = errBytes.toString();
    assertThat(runner.getExitCode()).isEqualTo(2);
    assertThat(err).contains("[devagent-cli] 지원하지 않는 옵션입니다: --unknown-option");
    assertThat(err).doesNotContain("Exception");
    assertThat(outBytes.toString()).isBlank();
  }

  @Test
  void runPrintsJsonErrorWhenJsonModeIsRequested() throws Exception {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        Mockito.mock(SpecAgentService.class),
        new CliResultFormatter(new ObjectMapper()),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("generate", "--unknown-option=true", "--json"));

    assertThat(runner.getExitCode()).isEqualTo(2);
    assertThat(errBytes.toString()).isBlank();
    var json = new ObjectMapper().readTree(outBytes.toString());
    assertThat(json.path("ok").asBoolean()).isFalse();
    assertThat(json.path("command").asText()).isEqualTo("generate");
    assertThat(json.path("error").path("exitCode").asInt()).isEqualTo(2);
  }

  @Test
  void runGeneratePrintsJsonOnSuccess() throws Exception {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    Mockito.when(codeAgentService.generate(any())).thenReturn(sampleGenerateResponse());

    DevAgentCliRunner runner = new DevAgentCliRunner(
        codeAgentService,
        Mockito.mock(SpecAgentService.class),
        new CliResultFormatter(new ObjectMapper()),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("generate", "--user-request=test", "--json"));

    assertThat(runner.getExitCode()).isZero();
    assertThat(errBytes.toString()).isBlank();
    var json = new ObjectMapper().readTree(outBytes.toString());
    assertThat(json.path("ok").asBoolean()).isTrue();
    assertThat(json.path("command").asText()).isEqualTo("generate");
    assertThat(json.path("data").path("summary").path("parsedFiles").asInt()).isEqualTo(1);
  }

  @Test
  void runSpecPrintsJsonOnSuccess() throws Exception {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    SpecAgentService specAgentService = Mockito.mock(SpecAgentService.class);
    Mockito.when(specAgentService.generate(any())).thenReturn(sampleSpecResponse());

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        specAgentService,
        new CliResultFormatter(new ObjectMapper()),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("spec", "--user-request=test", "--json"));

    assertThat(runner.getExitCode()).isZero();
    assertThat(errBytes.toString()).isBlank();
    var json = new ObjectMapper().readTree(outBytes.toString());
    assertThat(json.path("ok").asBoolean()).isTrue();
    assertThat(json.path("command").asText()).isEqualTo("spec");
    assertThat(json.path("data").path("summary").path("specKeys").asInt()).isEqualTo(1);
  }

  @Test
  void runHelpPrintsUsageWithoutError() {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        Mockito.mock(SpecAgentService.class),
        Mockito.mock(CliResultFormatter.class),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments("help"));

    assertThat(runner.getExitCode()).isZero();
    assertThat(outBytes.toString()).contains("devagent CLI (draft)");
    assertThat(errBytes.toString()).isBlank();
  }

  @Test
  void runGenerateMapsDocReviewChainOptionsToRequest() {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    CodeAgentService codeAgentService = Mockito.mock(CodeAgentService.class);
    Mockito.when(codeAgentService.generate(any())).thenReturn(sampleGenerateResponse());

    DevAgentCliRunner runner = new DevAgentCliRunner(
        codeAgentService,
        Mockito.mock(SpecAgentService.class),
        new CliResultFormatter(new ObjectMapper()),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments(
        "generate",
        "--user-request=test",
        "--chain-to-doc=true",
        "--doc-user-request=문서를 생성해줘",
        "--chain-to-review",
        "--review-user-request=리뷰를 생성해줘",
        "--chain-failure-policy=PARTIAL_SUCCESS"
    ));

    ArgumentCaptor<CodeGenerateRequest> captor = ArgumentCaptor.forClass(CodeGenerateRequest.class);
    Mockito.verify(codeAgentService).generate(captor.capture());
    CodeGenerateRequest request = captor.getValue();
    assertThat(request.isChainToDoc()).isTrue();
    assertThat(request.getDocUserRequest()).isEqualTo("문서를 생성해줘");
    assertThat(request.isChainToReview()).isTrue();
    assertThat(request.getReviewUserRequest()).isEqualTo("리뷰를 생성해줘");
    assertThat(request.getChainFailurePolicy()).isEqualTo(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);
    assertThat(runner.getExitCode()).isZero();
  }

  @Test
  void runSpecMapsCodePrefixedChainOptionsToRequest() {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    SpecAgentService specAgentService = Mockito.mock(SpecAgentService.class);
    Mockito.when(specAgentService.generate(any())).thenReturn(sampleSpecResponse());

    DevAgentCliRunner runner = new DevAgentCliRunner(
        Mockito.mock(CodeAgentService.class),
        specAgentService,
        new CliResultFormatter(new ObjectMapper()),
        new PrintStream(outBytes),
        new PrintStream(errBytes)
    );

    runner.run(new DefaultApplicationArguments(
        "spec",
        "--user-request=test",
        "--chain-to-code=true",
        "--code-chain-to-doc=true",
        "--code-doc-user-request=코드 문서를 생성해줘",
        "--code-chain-to-review=true",
        "--code-review-user-request=코드 리뷰를 생성해줘",
        "--code-chain-failure-policy=PARTIAL_SUCCESS"
    ));

    ArgumentCaptor<SpecGenerateRequest> captor = ArgumentCaptor.forClass(SpecGenerateRequest.class);
    Mockito.verify(specAgentService).generate(captor.capture());
    SpecGenerateRequest request = captor.getValue();
    assertThat(request.isCodeChainToDoc()).isTrue();
    assertThat(request.getCodeDocUserRequest()).isEqualTo("코드 문서를 생성해줘");
    assertThat(request.isCodeChainToReview()).isTrue();
    assertThat(request.getCodeReviewUserRequest()).isEqualTo("코드 리뷰를 생성해줘");
    assertThat(request.getCodeChainFailurePolicy()).isEqualTo(CodeGenerateRequest.ChainFailurePolicy.PARTIAL_SUCCESS);
    assertThat(runner.getExitCode()).isZero();
  }

  private CodeGenerateResponse sampleGenerateResponse() {
    return new CodeGenerateResponse(
        "run-1",
        "default",
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
  }

  private SpecGenerateResponse sampleSpecResponse() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode spec = mapper.createObjectNode();
    spec.put("title", "Spec title");
    return new SpecGenerateResponse(
        "spec-run-1",
        "default",
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
  }
}
