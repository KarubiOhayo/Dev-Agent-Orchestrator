package me.karubidev.devagent.agents.review;

import java.util.List;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RoutingMode;

public class ReviewGenerateRequest {

  private String projectId = "default";
  private String targetProjectRoot = ".";
  private String userRequest;
  private RoutingMode mode = RoutingMode.BALANCED;
  private RiskLevel riskLevel = RiskLevel.MEDIUM;
  private boolean largeContext;
  private boolean strictJsonRequired = true;

  private String codeRunId;
  private String codeOutput;
  private List<GeneratedFile> codeFiles = List.of();

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getTargetProjectRoot() {
    return targetProjectRoot;
  }

  public void setTargetProjectRoot(String targetProjectRoot) {
    this.targetProjectRoot = targetProjectRoot;
  }

  public String getUserRequest() {
    return userRequest;
  }

  public void setUserRequest(String userRequest) {
    this.userRequest = userRequest;
  }

  public RoutingMode getMode() {
    return mode;
  }

  public void setMode(RoutingMode mode) {
    this.mode = mode;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(RiskLevel riskLevel) {
    this.riskLevel = riskLevel;
  }

  public boolean isLargeContext() {
    return largeContext;
  }

  public void setLargeContext(boolean largeContext) {
    this.largeContext = largeContext;
  }

  public boolean isStrictJsonRequired() {
    return strictJsonRequired;
  }

  public void setStrictJsonRequired(boolean strictJsonRequired) {
    this.strictJsonRequired = strictJsonRequired;
  }

  public String getCodeRunId() {
    return codeRunId;
  }

  public void setCodeRunId(String codeRunId) {
    this.codeRunId = codeRunId;
  }

  public String getCodeOutput() {
    return codeOutput;
  }

  public void setCodeOutput(String codeOutput) {
    this.codeOutput = codeOutput;
  }

  public List<GeneratedFile> getCodeFiles() {
    return codeFiles;
  }

  public void setCodeFiles(List<GeneratedFile> codeFiles) {
    this.codeFiles = codeFiles == null ? List.of() : codeFiles;
  }
}
