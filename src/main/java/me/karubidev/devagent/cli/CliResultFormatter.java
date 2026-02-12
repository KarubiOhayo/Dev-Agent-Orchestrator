package me.karubidev.devagent.cli;

import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.spec.SpecGenerateResponse;
import org.springframework.stereotype.Component;

@Component
public class CliResultFormatter {

  public String formatGenerate(CodeGenerateResponse response) {
    FileApplyResult applyResult = response.applyResult();
    int parsedFiles = applyResult != null ? applyResult.parsedFiles() : safeSize(response.files());
    int writtenFiles = applyResult != null ? applyResult.writtenFiles() : 0;
    int skippedFiles = applyResult != null ? applyResult.skippedFiles() : 0;
    String applyOutcome;
    if (applyResult == null) {
      applyOutcome = "UNKNOWN";
    } else {
      applyOutcome = applyResult.dryRun() ? "DRY_RUN" : "APPLY";
    }

    List<Row> rows = List.of(
        new Row("runId", safe(response.runId())),
        new Row("model", modelOf(response.usedProvider(), response.usedModel())),
        new Row("parsedFiles", String.valueOf(parsedFiles)),
        new Row("applyOutcome", applyOutcome),
        new Row("writtenFiles", String.valueOf(writtenFiles)),
        new Row("skippedFiles", String.valueOf(skippedFiles))
    );

    StringBuilder sb = new StringBuilder();
    sb.append("== generate summary ==\n");
    sb.append(renderTable(rows));
    appendFileItems(sb, applyResult);
    return sb.toString();
  }

  public String formatSpec(SpecGenerateResponse response) {
    List<Row> rows = List.of(
        new Row("runId", safe(response.runId())),
        new Row("model", modelOf(response.usedProvider(), response.usedModel())),
        new Row("specKeys", String.valueOf(response.spec() == null ? 0 : response.spec().size())),
        new Row("chainedCode", response.chainedCodeResult() == null ? "false" : "true")
    );

    StringBuilder sb = new StringBuilder();
    sb.append("== spec summary ==\n");
    sb.append(renderTable(rows));
    if (response.chainedCodeResult() != null) {
      sb.append("\n");
      sb.append(formatGenerate(response.chainedCodeResult()));
    }
    return sb.toString();
  }

  private void appendFileItems(StringBuilder sb, FileApplyResult applyResult) {
    if (applyResult == null || applyResult.files() == null || applyResult.files().isEmpty()) {
      return;
    }

    sb.append("\nfile results\n");
    for (FileApplyItem item : applyResult.files()) {
      sb.append("- ")
          .append(safe(item.status()))
          .append(" ")
          .append(pathOf(item.path()));
      String message = safe(item.message());
      if (!message.isBlank() && !"-".equals(message)) {
        sb.append(" (").append(message).append(")");
      }
      sb.append("\n");
    }
  }

  private String renderTable(List<Row> rows) {
    int keyWidth = "field".length();
    int valueWidth = "value".length();
    for (Row row : rows) {
      keyWidth = Math.max(keyWidth, row.key().length());
      valueWidth = Math.max(valueWidth, row.value().length());
    }

    String border = "+" + "-".repeat(keyWidth + 2) + "+" + "-".repeat(valueWidth + 2) + "+\n";
    StringBuilder sb = new StringBuilder();
    sb.append(border);
    sb.append(row("field", "value", keyWidth, valueWidth));
    sb.append(border);
    for (Row row : rows) {
      sb.append(row(row.key(), row.value(), keyWidth, valueWidth));
    }
    sb.append(border);
    return sb.toString();
  }

  private String row(String key, String value, int keyWidth, int valueWidth) {
    return "| " + padRight(key, keyWidth) + " | " + padRight(value, valueWidth) + " |\n";
  }

  private String padRight(String value, int width) {
    if (value.length() >= width) {
      return value;
    }
    return value + " ".repeat(width - value.length());
  }

  private int safeSize(List<?> values) {
    return values == null ? 0 : values.size();
  }

  private String safe(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private String pathOf(String path) {
    return path == null || path.isBlank() ? "(no-path)" : path;
  }

  private String modelOf(String provider, String model) {
    return safe(provider) + ":" + safe(model);
  }

  private record Row(String key, String value) {
  }
}
