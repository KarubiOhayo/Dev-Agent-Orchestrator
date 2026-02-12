package me.karubidev.devagent.cli;

import java.io.PrintStream;
import java.util.Set;
import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
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
      "spec-input-path"
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
      "spec-output-path"
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
    try {
      DevAgentCliArguments cli = DevAgentCliArguments.parse(arguments.getSourceArgs());
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
      err.println("[devagent-cli] " + e.getMessage());
    } catch (Exception e) {
      String message = e.getMessage() == null || e.getMessage().isBlank()
          ? e.getClass().getSimpleName()
          : e.getMessage();
      exitCode = 1;
      err.println("[devagent-cli] 실행 실패: " + message);
    }
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  private void runGenerate(DevAgentCliArguments cli) {
    cli.assertOnly(GENERATE_OPTIONS);

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

    var response = codeAgentService.generate(request);
    out.print(formatter.formatGenerate(response));
  }

  private void runSpec(DevAgentCliArguments cli) {
    cli.assertOnly(SPEC_OPTIONS);

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
    if (cli.hasOption("spec-output-path")) {
      request.setSpecOutputPath(cli.option("spec-output-path"));
    }

    var response = specAgentService.generate(request);
    out.print(formatter.formatSpec(response));
  }

  private void printUsage() {
    out.print("""
        devagent CLI (draft)

        usage:
          devagent generate --user-request "<요청>" [options]
          devagent spec --user-request "<요청>" [options]

        common options:
          --project-id=<id>                 (default: default)
          --target-root=<path>              (default: .)
          --mode=<COST_SAVER|BALANCED|QUALITY|GEMINI3_CANARY>
          --risk-level=<LOW|MEDIUM|HIGH>
          --large-context=<true|false>
          --strict-json-required=<true|false>

        generate options:
          --user-request="<요청>"
          --spec-input-path=<relative/path>
          --apply=<true|false>              (default: false)
          --overwrite-existing=<true|false> (default: false)

        spec options:
          --user-request="<요청>"
          --chain-to-code=<true|false>      (default: false)
          --code-user-request="<요청>"
          --code-apply=<true|false>         (default: false)
          --code-overwrite-existing=<true|false>
          --spec-output-path=<relative/path>
        """);
  }
}
