package me.karubidev.devagent.state;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RunStateStore {

  private final RunStateProperties properties;
  private final Object sqliteLock = new Object();
  private volatile boolean sqliteAvailable;

  public RunStateStore(RunStateProperties properties) {
    this.properties = properties;
    this.sqliteAvailable = false;
    initialize();
  }

  public String startRun(String projectId, String agent, String mode, String requestText) {
    String runId = UUID.randomUUID().toString();
    if (!properties.isEnabled()) {
      return runId;
    }

    String now = Instant.now().toString();
    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 """
                     INSERT INTO runs(run_id, project_id, agent, mode, request_text, status, created_at, updated_at)
                     VALUES(?, ?, ?, ?, ?, 'RUNNING', ?, ?)
                     """
             )) {
          ps.setString(1, runId);
          ps.setString(2, projectId);
          ps.setString(3, agent);
          ps.setString(4, mode);
          ps.setString(5, truncate(requestText, 4000));
          ps.setString(6, now);
          ps.setString(7, now);
          ps.executeUpdate();
          return runId;
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    appendFallbackLog("RUN_START\t" + now + "\t" + runId + "\t" + projectId + "\t" + agent + "\t" + mode);
    return runId;
  }

  public void completeSuccess(String runId, String projectId, String provider, String model, String outputPreview) {
    if (!properties.isEnabled()) {
      return;
    }

    String now = Instant.now().toString();
    String preview = truncate(outputPreview, properties.getOutputPreviewChars());
    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 """
                     UPDATE runs
                     SET status='SUCCESS', used_provider=?, used_model=?, output_preview=?, updated_at=?
                     WHERE run_id=?
                     """
             )) {
          ps.setString(1, provider);
          ps.setString(2, model);
          ps.setString(3, preview);
          ps.setString(4, now);
          ps.setString(5, runId);
          ps.executeUpdate();
          return;
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    appendFallbackLog("RUN_SUCCESS\t" + now + "\t" + runId + "\t" + projectId + "\t" + provider + "\t" + model);
  }

  public void completeFailure(String runId, String projectId, String errorMessage) {
    if (!properties.isEnabled()) {
      return;
    }

    String now = Instant.now().toString();
    String err = truncate(errorMessage, 3000);
    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 """
                     UPDATE runs
                     SET status='FAILED', error_message=?, updated_at=?
                     WHERE run_id=?
                     """
             )) {
          ps.setString(1, err);
          ps.setString(2, now);
          ps.setString(3, runId);
          ps.executeUpdate();
          return;
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    appendFallbackLog("RUN_FAILED\t" + now + "\t" + runId + "\t" + projectId + "\t" + err);
  }

  public void appendEvent(String runId, String eventType, String payload) {
    if (!properties.isEnabled()) {
      return;
    }

    String now = Instant.now().toString();
    String shortPayload = truncate(payload, 3000);
    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 """
                     INSERT INTO run_events(run_id, event_type, payload, created_at)
                     VALUES(?, ?, ?, ?)
                     """
             )) {
          ps.setString(1, runId);
          ps.setString(2, eventType);
          ps.setString(3, shortPayload);
          ps.setString(4, now);
          ps.executeUpdate();
          return;
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    appendFallbackLog("EVENT\t" + now + "\t" + runId + "\t" + eventType + "\t" + shortPayload);
  }

  public String getProjectSummary(String projectId) {
    if (!properties.isEnabled()) {
      return "";
    }

    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT summary FROM project_memory WHERE project_id=?"
             )) {
          ps.setString(1, projectId);
          try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
              return safe(rs.getString(1));
            }
          }
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    return fallbackSummary(projectId);
  }

  public void updateProjectSummary(String projectId, String summary) {
    if (!properties.isEnabled()) {
      return;
    }

    String now = Instant.now().toString();
    String shortSummary = truncate(summary, 6000);
    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 """
                     INSERT INTO project_memory(project_id, summary, updated_at)
                     VALUES(?, ?, ?)
                     ON CONFLICT(project_id)
                     DO UPDATE SET summary=excluded.summary, updated_at=excluded.updated_at
                     """
             )) {
          ps.setString(1, projectId);
          ps.setString(2, shortSummary);
          ps.setString(3, now);
          ps.executeUpdate();
          return;
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    saveFallbackSummary(projectId, shortSummary);
  }

  public List<String> getRecentOutputs(String projectId, int limit) {
    int max = Math.max(1, limit);
    List<String> outputs = new ArrayList<>();
    if (!properties.isEnabled()) {
      return outputs;
    }

    if (sqliteAvailable) {
      synchronized (sqliteLock) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                 """
                     SELECT output_preview
                     FROM runs
                     WHERE project_id=? AND status='SUCCESS' AND output_preview IS NOT NULL
                     ORDER BY updated_at DESC
                     LIMIT ?
                     """
             )) {
          ps.setString(1, projectId);
          ps.setInt(2, max);
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              String output = rs.getString(1);
              if (output != null && !output.isBlank()) {
                outputs.add(output);
              }
            }
          }
          return outputs;
        } catch (Exception e) {
          sqliteAvailable = false;
        }
      }
    }

    return outputs;
  }

  private void initialize() {
    if (!properties.isEnabled()) {
      return;
    }

    prepareFallbackFiles();
    try {
      ensureJdbcParentDirectory();
      try (Connection conn = connection(); Statement st = conn.createStatement()) {
        st.executeUpdate(
            """
                CREATE TABLE IF NOT EXISTS runs (
                  run_id TEXT PRIMARY KEY,
                  project_id TEXT NOT NULL,
                  agent TEXT NOT NULL,
                  mode TEXT,
                  request_text TEXT,
                  status TEXT NOT NULL,
                  used_provider TEXT,
                  used_model TEXT,
                  output_preview TEXT,
                  error_message TEXT,
                  created_at TEXT NOT NULL,
                  updated_at TEXT NOT NULL
                )
                """
        );
        st.executeUpdate(
            """
                CREATE TABLE IF NOT EXISTS run_events (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  run_id TEXT NOT NULL,
                  event_type TEXT NOT NULL,
                  payload TEXT,
                  created_at TEXT NOT NULL
                )
                """
        );
        st.executeUpdate(
            """
                CREATE TABLE IF NOT EXISTS project_memory (
                  project_id TEXT PRIMARY KEY,
                  summary TEXT,
                  updated_at TEXT NOT NULL
                )
                """
        );
        st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_runs_project_updated ON runs(project_id, updated_at DESC)");
      }
      sqliteAvailable = true;
    } catch (Exception e) {
      sqliteAvailable = false;
      appendFallbackLog("INIT\t" + Instant.now() + "\tSQLite unavailable: " + e.getMessage());
    }
  }

  private Connection connection() throws SQLException {
    return DriverManager.getConnection(properties.getJdbcUrl());
  }

  private void ensureJdbcParentDirectory() throws IOException {
    String jdbcUrl = properties.getJdbcUrl();
    if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:sqlite:")) {
      return;
    }

    String rawPath = jdbcUrl.substring("jdbc:sqlite:".length());
    if (rawPath.isBlank() || ":memory:".equals(rawPath)) {
      return;
    }

    Path dbPath = Path.of(rawPath).toAbsolutePath().normalize();
    Path parent = dbPath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
  }

  private void prepareFallbackFiles() {
    try {
      Path log = fallbackLogPath();
      Path memory = fallbackMemoryPath();
      if (log.getParent() != null) {
        Files.createDirectories(log.getParent());
      }
      if (memory.getParent() != null) {
        Files.createDirectories(memory.getParent());
      }
      if (!Files.exists(log)) {
        Files.createFile(log);
      }
      if (!Files.exists(memory)) {
        Files.createFile(memory);
      }
    } catch (IOException ignored) {
      // fallback 준비 실패해도 런타임을 막지 않는다.
    }
  }

  private String fallbackSummary(String projectId) {
    try {
      Properties props = new Properties();
      try (var in = Files.newInputStream(fallbackMemoryPath())) {
        props.load(in);
      }
      return safe(props.getProperty(projectId));
    } catch (Exception e) {
      return "";
    }
  }

  private void saveFallbackSummary(String projectId, String summary) {
    try {
      Properties props = new Properties();
      Path path = fallbackMemoryPath();
      try (var in = Files.newInputStream(path)) {
        props.load(in);
      }
      props.setProperty(projectId, summary);
      try (var out = Files.newOutputStream(path)) {
        props.store(out, "DevAgent project memory");
      }
    } catch (Exception ignored) {
      // ignore
    }
  }

  private void appendFallbackLog(String line) {
    try {
      Files.writeString(
          fallbackLogPath(),
          sanitizeLine(line) + System.lineSeparator(),
          StandardCharsets.UTF_8,
          java.nio.file.StandardOpenOption.CREATE,
          java.nio.file.StandardOpenOption.APPEND
      );
    } catch (Exception ignored) {
      // ignore
    }
  }

  private Path fallbackLogPath() {
    return Path.of(properties.getFallbackLogFile()).toAbsolutePath().normalize();
  }

  private Path fallbackMemoryPath() {
    return Path.of(properties.getFallbackMemoryFile()).toAbsolutePath().normalize();
  }

  private String sanitizeLine(String value) {
    if (value == null) {
      return "";
    }
    return value.replace('\n', ' ').replace('\r', ' ');
  }

  private String truncate(String value, int maxChars) {
    if (value == null) {
      return null;
    }
    if (value.length() <= maxChars) {
      return value;
    }
    return value.substring(0, maxChars) + "...";
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
