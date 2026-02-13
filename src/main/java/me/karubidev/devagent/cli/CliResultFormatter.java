package me.karubidev.devagent.cli;

import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.FileApplyItem;
import me.karubidev.devagent.agents.code.apply.FileApplyResult;
import me.karubidev.devagent.agents.spec.SpecGenerateResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class CliResultFormatter {

  private final ObjectMapper objectMapper;

  public CliResultFormatter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

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

  public String formatGenerateJson(CodeGenerateResponse response) {
    ObjectNode root = baseSuccess("generate", response.runId(), response.usedProvider(), response.usedModel());
    root.set("data", buildGenerateDataNode(response));
    root.set("error", objectMapper.nullNode());
    return toJson(root);
  }

  public String formatSpecJson(SpecGenerateResponse response) {
    ObjectNode root = baseSuccess("spec", response.runId(), response.usedProvider(), response.usedModel());
    ObjectNode data = objectMapper.createObjectNode();

    ObjectNode summary = objectMapper.createObjectNode();
    summary.put("specKeys", response.spec() == null ? 0 : response.spec().size());
    summary.put("chainedCode", response.chainedCodeResult() != null);
    data.set("summary", summary);
    if (response.spec() != null) {
      data.set("spec", response.spec());
    } else {
      data.set("spec", objectMapper.nullNode());
    }
    if (response.chainedCodeResult() != null) {
      data.set("chainedCode", buildGenerateDataNode(response.chainedCodeResult()));
    } else {
      data.set("chainedCode", objectMapper.nullNode());
    }

    root.set("data", data);
    root.set("error", objectMapper.nullNode());
    return toJson(root);
  }

  public String formatErrorJson(String command, int exitCode, String message) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("ok", false);
    if (command == null || command.isBlank()) {
      root.set("command", objectMapper.nullNode());
    } else {
      root.put("command", command);
    }
    root.set("runId", objectMapper.nullNode());
    root.set("model", objectMapper.nullNode());
    root.set("data", objectMapper.nullNode());

    ObjectNode error = objectMapper.createObjectNode();
    error.put("exitCode", exitCode);
    error.put("message", safeJsonMessage(message));
    root.set("error", error);
    return toJson(root);
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

  private ObjectNode baseSuccess(String command, String runId, String provider, String model) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("ok", true);
    root.put("command", command);
    if (runId == null || runId.isBlank()) {
      root.set("runId", objectMapper.nullNode());
    } else {
      root.put("runId", runId);
    }
    root.set("model", modelNode(provider, model));
    return root;
  }

  private ObjectNode modelNode(String provider, String model) {
    ObjectNode node = objectMapper.createObjectNode();
    if (provider == null || provider.isBlank()) {
      node.set("provider", objectMapper.nullNode());
    } else {
      node.put("provider", provider);
    }
    if (model == null || model.isBlank()) {
      node.set("name", objectMapper.nullNode());
    } else {
      node.put("name", model);
    }
    if ((provider == null || provider.isBlank()) && (model == null || model.isBlank())) {
      node.set("id", objectMapper.nullNode());
    } else {
      node.put("id", modelOf(provider, model));
    }
    return node;
  }

  private ObjectNode buildGenerateDataNode(CodeGenerateResponse response) {
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

    ObjectNode data = objectMapper.createObjectNode();
    ObjectNode summary = objectMapper.createObjectNode();
    summary.put("parsedFiles", parsedFiles);
    summary.put("applyOutcome", applyOutcome);
    summary.put("writtenFiles", writtenFiles);
    summary.put("skippedFiles", skippedFiles);
    data.set("summary", summary);

    ArrayNode files = objectMapper.createArrayNode();
    if (applyResult != null && applyResult.files() != null) {
      for (FileApplyItem item : applyResult.files()) {
        ObjectNode node = objectMapper.createObjectNode();
        if (item.path() == null || item.path().isBlank()) {
          node.set("path", objectMapper.nullNode());
        } else {
          node.put("path", item.path());
        }
        if (item.status() == null || item.status().isBlank()) {
          node.set("status", objectMapper.nullNode());
        } else {
          node.put("status", item.status());
        }
        if (item.message() == null || item.message().isBlank()) {
          node.set("message", objectMapper.nullNode());
        } else {
          node.put("message", item.message());
        }
        files.add(node);
      }
    }
    data.set("fileResults", files);
    return data;
  }

  private String toJson(ObjectNode node) {
    try {
      return objectMapper.writeValueAsString(node) + System.lineSeparator();
    } catch (Exception e) {
      throw new IllegalStateException("CLI JSON 직렬화에 실패했습니다.", e);
    }
  }

  private String safeJsonMessage(String message) {
    if (message == null || message.isBlank()) {
      return "알 수 없는 오류";
    }
    return message;
  }

  private record Row(String key, String value) {
  }
}
