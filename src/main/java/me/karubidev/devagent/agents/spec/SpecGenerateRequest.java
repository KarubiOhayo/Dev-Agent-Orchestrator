package me.karubidev.devagent.agents.spec;

import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RoutingMode;

public class SpecGenerateRequest {

  private String projectId = "default";
  private String targetProjectRoot = ".";
  private String userRequest;
  private RoutingMode mode = RoutingMode.BALANCED;
  private RiskLevel riskLevel = RiskLevel.MEDIUM;
  private boolean largeContext;
  private boolean strictJsonRequired = true;

  private boolean chainToCode;
  private String codeUserRequest;
  private boolean codeApply;
  private boolean codeOverwriteExisting;
  private boolean codeChainToDoc;
  private String codeDocUserRequest;
  private boolean codeChainToReview;
  private String codeReviewUserRequest;
  private CodeGenerateRequest.ChainFailurePolicy codeChainFailurePolicy =
      CodeGenerateRequest.ChainFailurePolicy.FAIL_FAST;
  private String specOutputPath;

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

  public boolean isChainToCode() {
    return chainToCode;
  }

  public void setChainToCode(boolean chainToCode) {
    this.chainToCode = chainToCode;
  }

  public String getCodeUserRequest() {
    return codeUserRequest;
  }

  public void setCodeUserRequest(String codeUserRequest) {
    this.codeUserRequest = codeUserRequest;
  }

  public boolean isCodeApply() {
    return codeApply;
  }

  public void setCodeApply(boolean codeApply) {
    this.codeApply = codeApply;
  }

  public boolean isCodeOverwriteExisting() {
    return codeOverwriteExisting;
  }

  public void setCodeOverwriteExisting(boolean codeOverwriteExisting) {
    this.codeOverwriteExisting = codeOverwriteExisting;
  }

  public boolean isCodeChainToDoc() {
    return codeChainToDoc;
  }

  public void setCodeChainToDoc(boolean codeChainToDoc) {
    this.codeChainToDoc = codeChainToDoc;
  }

  public String getCodeDocUserRequest() {
    return codeDocUserRequest;
  }

  public void setCodeDocUserRequest(String codeDocUserRequest) {
    this.codeDocUserRequest = codeDocUserRequest;
  }

  public boolean isCodeChainToReview() {
    return codeChainToReview;
  }

  public void setCodeChainToReview(boolean codeChainToReview) {
    this.codeChainToReview = codeChainToReview;
  }

  public String getCodeReviewUserRequest() {
    return codeReviewUserRequest;
  }

  public void setCodeReviewUserRequest(String codeReviewUserRequest) {
    this.codeReviewUserRequest = codeReviewUserRequest;
  }

  public CodeGenerateRequest.ChainFailurePolicy getCodeChainFailurePolicy() {
    return codeChainFailurePolicy;
  }

  public void setCodeChainFailurePolicy(CodeGenerateRequest.ChainFailurePolicy codeChainFailurePolicy) {
    if (codeChainFailurePolicy == null) {
      this.codeChainFailurePolicy = CodeGenerateRequest.ChainFailurePolicy.FAIL_FAST;
      return;
    }
    this.codeChainFailurePolicy = codeChainFailurePolicy;
  }

  public String getSpecOutputPath() {
    return specOutputPath;
  }

  public void setSpecOutputPath(String specOutputPath) {
    this.specOutputPath = specOutputPath;
  }
}
