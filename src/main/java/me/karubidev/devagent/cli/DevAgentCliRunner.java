package me.karubidev.devagent.cli;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Set;
import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.spec.SpecAgentService;
import me.karubidev.devagent.agents.spec.SpecGenerateRequest;
import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RoutingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class DevAgentCliRunner implements ApplicationRunner, ExitCodeGenerator {

  private static final int EXIT_CODE_CHAIN_FAILURE_GUARDRAIL = 3;

  private static final Set<String> GENERATE_OPTIONS = Set.of(
      "project-id",
      "target-root",
      "user-request",
      "mode",
      "risk-level",
      "large-context",
      "strict-json-required",
      "apply",
      "overwrite-existing",
      "spec-input-path",
      "chain-to-doc",
      "doc-user-request",
      "chain-to-review",
      "review-user-request",
      "chain-failure-policy",
      "fail-on-chain-failures",
      "json"
  );

  private static final Set<String> SPEC_OPTIONS = Set.of(
      "project-id",
      "target-root",
      "user-request",
      "mode",
      "risk-level",
      "large-context",
      "strict-json-required",
      "chain-to-code",
      "code-user-request",
      "code-apply",
      "code-overwrite-existing",
      "code-chain-to-doc",
      "code-doc-user-request",
      "code-chain-to-review",
      "code-review-user-request",
      "code-chain-failure-policy",
      "fail-on-chain-failures",
      "spec-output-path",
      "json"
  );

  private final CodeAgentService codeAgentService;
  private final SpecAgentService specAgentService;
  private final CliResultFormatter formatter;
  private final PrintStream out;
  private final PrintStream err;
  private int exitCode = 0;

  @Autowired
  public DevAgentCliRunner(
      CodeAgentService codeAgentService,
      SpecAgentService specAgentService,
      CliResultFormatter formatter
  ) {
    this(codeAgentService, specAgentService, formatter, System.out, System.err);
  }

  DevAgentCliRunner(
      CodeAgentService codeAgentService,
      SpecAgentService specAgentService,
      CliResultFormatter formatter,
      PrintStream out,
      PrintStream err
  ) {
    this.codeAgentService = codeAgentService;
    this.specAgentService = specAgentService;
    this.formatter = formatter;
    this.out = out;
    this.err = err;
  }

  @Override
  public void run(ApplicationArguments arguments) {
    exitCode = 0;
    String[] sourceArgs = arguments.getSourceArgs();
    boolean jsonErrorMode = shouldEmitJsonError(sourceArgs);
    String command = detectCommand(sourceArgs);

    try {
      DevAgentCliArguments cli = DevAgentCliArguments.parse(sourceArgs);
      if (!cli.cliMode()) {
        return;
      }
      switch (cli.command()) {
        case "generate" -> runGenerate(cli);
        case "spec" -> runSpec(cli);
        case "help" -> printUsage();
        default -> throw new CliCommandException("지원하지 않는 명령어입니다: " + cli.command(), 2);
      }
    } catch (CliCommandException e) {
      exitCode = e.getExitCode();
      emitError(jsonErrorMode, command, exitCode, e.getMessage());
    } catch (Exception e) {
      String message = e.getMessage() == null || e.getMessage().isBlank()
          ? e.getClass().getSimpleName()
          : e.getMessage();
      exitCode = 1;
      emitError(jsonErrorMode, command, exitCode, "실행 실패: " + message);
    }
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  private void runGenerate(DevAgentCliArguments cli) {
    cli.assertOnly(GENERATE_OPTIONS);
    boolean jsonMode = cli.optionAsBoolean("json", false);
    boolean failOnChainFailures = cli.optionAsBoolean("fail-on-chain-failures", false);

    CodeGenerateRequest request = new CodeGenerateRequest();
    request.setProjectId(cli.optionOrDefault("project-id", request.getProjectId()));
    request.setTargetProjectRoot(cli.optionOrDefault("target-root", request.getTargetProjectRoot()));
    if (cli.hasOption("user-request")) {
      request.setUserRequest(cli.option("user-request"));
    }
    request.setMode(cli.optionAsEnum("mode", RoutingMode.class, request.getMode()));
    request.setRiskLevel(cli.optionAsEnum("risk-level", RiskLevel.class, request.getRiskLevel()));
    request.setLargeContext(cli.optionAsBoolean("large-context", request.isLargeContext()));
    request.setStrictJsonRequired(cli.optionAsBoolean("strict-json-required", request.isStrictJsonRequired()));
    request.setApply(cli.optionAsBoolean("apply", request.isApply()));
    request.setOverwriteExisting(cli.optionAsBoolean("overwrite-existing", request.isOverwriteExisting()));
    if (cli.hasOption("spec-input-path")) {
      request.setSpecInputPath(cli.option("spec-input-path"));
    }
    request.setChainToDoc(cli.optionAsBoolean("chain-to-doc", request.isChainToDoc()));
    if (cli.hasOption("doc-user-request")) {
      request.setDocUserRequest(cli.option("doc-user-request"));
    }
    request.setChainToReview(cli.optionAsBoolean("chain-to-review", request.isChainToReview()));
    if (cli.hasOption("review-user-request")) {
      request.setReviewUserRequest(cli.option("review-user-request"));
    }
    request.setChainFailurePolicy(
        cli.optionAsEnum(
            "chain-failure-policy",
            CodeGenerateRequest.ChainFailurePolicy.class,
            request.getChainFailurePolicy()
        )
    );

    var response = codeAgentService.generate(request);
    if (jsonMode) {
      out.print(formatter.formatGenerateJson(response));
    } else {
      out.print(formatter.formatGenerate(response));
    }
    applyChainFailureGuardrail(failOnChainFailures, response);
  }

  private void runSpec(DevAgentCliArguments cli) {
    cli.assertOnly(SPEC_OPTIONS);
    boolean jsonMode = cli.optionAsBoolean("json", false);
    boolean failOnChainFailures = cli.optionAsBoolean("fail-on-chain-failures", false);

    SpecGenerateRequest request = new SpecGenerateRequest();
    request.setProjectId(cli.optionOrDefault("project-id", request.getProjectId()));
    request.setTargetProjectRoot(cli.optionOrDefault("target-root", request.getTargetProjectRoot()));
    if (cli.hasOption("user-request")) {
      request.setUserRequest(cli.option("user-request"));
    }
    request.setMode(cli.optionAsEnum("mode", RoutingMode.class, request.getMode()));
    request.setRiskLevel(cli.optionAsEnum("risk-level", RiskLevel.class, request.getRiskLevel()));
    request.setLargeContext(cli.optionAsBoolean("large-context", request.isLargeContext()));
    request.setStrictJsonRequired(cli.optionAsBoolean("strict-json-required", request.isStrictJsonRequired()));
    request.setChainToCode(cli.optionAsBoolean("chain-to-code", request.isChainToCode()));
    if (cli.hasOption("code-user-request")) {
      request.setCodeUserRequest(cli.option("code-user-request"));
    }
    request.setCodeApply(cli.optionAsBoolean("code-apply", request.isCodeApply()));
    request.setCodeOverwriteExisting(cli.optionAsBoolean("code-overwrite-existing", request.isCodeOverwriteExisting()));
    request.setCodeChainToDoc(cli.optionAsBoolean("code-chain-to-doc", request.isCodeChainToDoc()));
    if (cli.hasOption("code-doc-user-request")) {
      request.setCodeDocUserRequest(cli.option("code-doc-user-request"));
    }
    request.setCodeChainToReview(cli.optionAsBoolean("code-chain-to-review", request.isCodeChainToReview()));
    if (cli.hasOption("code-review-user-request")) {
      request.setCodeReviewUserRequest(cli.option("code-review-user-request"));
    }
    request.setCodeChainFailurePolicy(
        cli.optionAsEnum(
            "code-chain-failure-policy",
            CodeGenerateRequest.ChainFailurePolicy.class,
            request.getCodeChainFailurePolicy()
        )
    );
    if (cli.hasOption("spec-output-path")) {
      request.setSpecOutputPath(cli.option("spec-output-path"));
    }

    var response = specAgentService.generate(request);
    if (jsonMode) {
      out.print(formatter.formatSpecJson(response));
    } else {
      out.print(formatter.formatSpec(response));
    }
    applyChainFailureGuardrail(failOnChainFailures, response.chainedCodeResult());
  }

  private void printUsage() {
    out.print("""
        devagent CLI (draft)

        usage:
          devagent generate --user-request "<요청>" [options]
          devagent spec --user-request "<요청>" [options]

        common options:
          --project-id=<id>, --project, -p  (default: default)
          --target-root=<path>, --root, -r  (default: .)
          --user-request="<요청>", --request, -u
          --mode=<COST_SAVER|BALANCED|QUALITY|GEMINI3_CANARY>, -m
          --risk-level=<LOW|MEDIUM|HIGH>, --risk, -k
          --large-context=<true|false>
          --strict-json-required=<true|false>
          --json=<true|false>, -j           (default: false)

        generate options:
          --spec-input-path=<relative/path>
          --apply=<true|false>, -a          (default: false)
          --overwrite-existing=<true|false> (default: false)
          --chain-to-doc=<true|false>       (default: false)
          --doc-user-request="<요청>"
          --chain-to-review=<true|false>    (default: false)
          --review-user-request="<요청>"
          --chain-failure-policy=<FAIL_FAST|PARTIAL_SUCCESS> (default: FAIL_FAST)
          --fail-on-chain-failures=<true|false> (default: false, chainFailures>0 => exit 3)

        spec options:
          --chain-to-code=<true|false>, -c  (default: false)
          --code-user-request="<요청>"
          --code-apply=<true|false>         (default: false)
          --code-overwrite-existing=<true|false>
          --code-chain-to-doc=<true|false>  (default: false)
          --code-doc-user-request="<요청>"
          --code-chain-to-review=<true|false> (default: false)
          --code-review-user-request="<요청>"
          --code-chain-failure-policy=<FAIL_FAST|PARTIAL_SUCCESS> (default: FAIL_FAST)
          --fail-on-chain-failures=<true|false> (default: false, chainFailures>0 => exit 3)
          --spec-output-path=<relative/path>
        """);
  }

  private void emitError(boolean jsonMode, String command, int exitCode, String message) {
    if (jsonMode) {
      out.print(formatter.formatErrorJson(command, exitCode, message));
      return;
    }
    err.println("[devagent-cli] " + message);
  }

  private boolean shouldEmitJsonError(String[] rawArgs) {
    if (rawArgs == null || rawArgs.length == 0) {
      return false;
    }
    for (int i = 0; i < rawArgs.length; i++) {
      String token = normalize(rawArgs[i]);
      if ("--json".equals(token) || "-j".equals(token)) {
        if (i + 1 < rawArgs.length && !isOptionToken(rawArgs[i + 1])) {
          Boolean parsed = parseBoolean(rawArgs[i + 1]);
          return parsed == null ? true : parsed;
        }
        return true;
      }
      if (token.startsWith("--json=")) {
        String value = token.substring("--json=".length());
        Boolean parsed = parseBoolean(value);
        return parsed == null ? true : parsed;
      }
      if (token.startsWith("-j=")) {
        String value = token.substring("-j=".length());
        Boolean parsed = parseBoolean(value);
        return parsed == null ? true : parsed;
      }
    }
    return false;
  }

  private String detectCommand(String[] rawArgs) {
    if (rawArgs == null || rawArgs.length == 0) {
      return null;
    }

    int index = 0;
    String first = normalize(rawArgs[0]);
    if ("devagent".equals(first)) {
      index++;
    }
    if (index >= rawArgs.length) {
      return null;
    }

    String command = normalize(rawArgs[index]);
    if (command.startsWith("-")) {
      return null;
    }
    return command;
  }

  private boolean isOptionToken(String token) {
    return token != null && token.startsWith("-");
  }

  private Boolean parseBoolean(String raw) {
    String normalized = normalize(raw);
    if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
      return true;
    }
    if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
      return false;
    }
    return null;
  }

  private void applyChainFailureGuardrail(boolean failOnChainFailures, CodeGenerateResponse response) {
    if (failOnChainFailures && hasChainFailures(response)) {
      exitCode = EXIT_CODE_CHAIN_FAILURE_GUARDRAIL;
    }
  }

  private boolean hasChainFailures(CodeGenerateResponse response) {
    return response != null && response.chainFailures() != null && !response.chainFailures().isEmpty();
  }

  private String normalize(String raw) {
    return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
  }
}
