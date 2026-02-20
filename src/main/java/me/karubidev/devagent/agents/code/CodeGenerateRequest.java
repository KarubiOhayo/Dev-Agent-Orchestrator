package me.karubidev.devagent.agents.code;

import me.karubidev.devagent.orchestration.routing.RiskLevel;
import me.karubidev.devagent.orchestration.routing.RoutingMode;

public class CodeGenerateRequest {

  public enum ChainFailurePolicy {
    FAIL_FAST,
    PARTIAL_SUCCESS
  }

  private String projectId = "default";
  private String targetProjectRoot = ".";
  private String userRequest;
  private RoutingMode mode = RoutingMode.BALANCED;
  private RiskLevel riskLevel = RiskLevel.MEDIUM;
  private boolean largeContext;
  private boolean strictJsonRequired;
  private boolean apply;
  private boolean overwriteExisting;
  private String specInputPath;
  private boolean chainToDoc;
  private String docUserRequest;
  private boolean chainToReview;
  private String reviewUserRequest;
  private ChainFailurePolicy chainFailurePolicy = ChainFailurePolicy.FAIL_FAST;

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

  public boolean isApply() {
    return apply;
  }

  public void setApply(boolean apply) {
    this.apply = apply;
  }

  public boolean isOverwriteExisting() {
    return overwriteExisting;
  }

  public void setOverwriteExisting(boolean overwriteExisting) {
    this.overwriteExisting = overwriteExisting;
  }

  public String getSpecInputPath() {
    return specInputPath;
  }

  public void setSpecInputPath(String specInputPath) {
    this.specInputPath = specInputPath;
  }

  public boolean isChainToDoc() {
    return chainToDoc;
  }

  public void setChainToDoc(boolean chainToDoc) {
    this.chainToDoc = chainToDoc;
  }

  public String getDocUserRequest() {
    return docUserRequest;
  }

  public void setDocUserRequest(String docUserRequest) {
    this.docUserRequest = docUserRequest;
  }

  public boolean isChainToReview() {
    return chainToReview;
  }

  public void setChainToReview(boolean chainToReview) {
    this.chainToReview = chainToReview;
  }

  public String getReviewUserRequest() {
    return reviewUserRequest;
  }

  public void setReviewUserRequest(String reviewUserRequest) {
    this.reviewUserRequest = reviewUserRequest;
  }

  public ChainFailurePolicy getChainFailurePolicy() {
    return chainFailurePolicy;
  }

  public void setChainFailurePolicy(ChainFailurePolicy chainFailurePolicy) {
    this.chainFailurePolicy = chainFailurePolicy;
  }
}
