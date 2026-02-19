package me.karubidev.devagent.agents.spec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.state.RunStateStore;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SpecCodeChainService {

  private final CodeAgentService codeAgentService;
  private final RunStateStore runStateStore;
  private final ObjectMapper objectMapper;

  public SpecCodeChainService(CodeAgentService codeAgentService, RunStateStore runStateStore,
      ObjectMapper objectMapper) {
    this.codeAgentService = codeAgentService;
    this.runStateStore = runStateStore;
    this.objectMapper = objectMapper;
  }

  public CodeGenerateResponse runChain(String specRunId, SpecGenerateRequest request, JsonNode spec, Path targetRoot) {
    if (!request.isChainToCode()) {
      return null;
    }

    try {
      Path safeTargetRoot = targetRoot.toAbsolutePath().normalize();
      Path specFilePath = resolveSpecFilePath(request.getSpecOutputPath(), safeTargetRoot, specRunId);
      writeSpecFile(specFilePath, spec);
      runStateStore.appendEvent(specRunId, "CHAIN_SPEC_WRITTEN", specFilePath.toString());

      CodeGenerateRequest codeRequest = new CodeGenerateRequest();
      codeRequest.setProjectId(request.getProjectId());
      codeRequest.setTargetProjectRoot(safeTargetRoot.toString());
      codeRequest.setUserRequest(resolveCodeRequest(request));
      codeRequest.setMode(request.getMode());
      codeRequest.setRiskLevel(request.getRiskLevel());
      codeRequest.setLargeContext(request.isLargeContext());
      codeRequest.setStrictJsonRequired(request.isStrictJsonRequired());
      codeRequest.setApply(request.isCodeApply());
      codeRequest.setOverwriteExisting(request.isCodeOverwriteExisting());
      codeRequest.setChainToDoc(request.isCodeChainToDoc());
      codeRequest.setDocUserRequest(request.getCodeDocUserRequest());
      codeRequest.setChainToReview(request.isCodeChainToReview());
      codeRequest.setReviewUserRequest(request.getCodeReviewUserRequest());
      codeRequest.setChainFailurePolicy(request.getCodeChainFailurePolicy());
      codeRequest.setSpecInputPath(safeTargetRoot.relativize(specFilePath).toString());

      runStateStore.appendEvent(specRunId, "CHAIN_CODE_TRIGGERED", specFilePath.toString());
      CodeGenerateResponse response = codeAgentService.generate(codeRequest);
      runStateStore.appendEvent(specRunId, "CHAIN_CODE_DONE", "codeRunId=" + response.runId());
      return response;
    } catch (Exception e) {
      runStateStore.appendEvent(specRunId, "CHAIN_CODE_FAILED", "reason=" + errorMessage(e));
      throw e;
    }
  }

  private Path resolveSpecFilePath(String requestedPath, Path targetRoot, String runId) {
    if (requestedPath == null || requestedPath.isBlank()) {
      return targetRoot.resolve(".devagent/specs").resolve(runId + ".json").normalize();
    }

    Path candidate;
    try {
      candidate = Path.of(requestedPath);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException("specOutputPath is invalid: " + requestedPath, e);
    }

    if (candidate.isAbsolute()) {
      throw new IllegalArgumentException("specOutputPath must be a relative path inside targetProjectRoot");
    }
    for (Path part : candidate) {
      if ("..".equals(part.toString())) {
        throw new IllegalArgumentException("specOutputPath must not contain '..'");
      }
    }

    Path normalized = targetRoot.resolve(candidate).normalize();
    if (!normalized.startsWith(targetRoot)) {
      throw new IllegalArgumentException("specOutputPath escapes targetProjectRoot");
    }
    return normalized;
  }

  private void writeSpecFile(Path specFilePath, JsonNode spec) {
    if (Files.exists(specFilePath)) {
      throw new IllegalArgumentException("spec output already exists (overwrite disabled): " + specFilePath);
    }

    try {
      if (specFilePath.getParent() != null) {
        Files.createDirectories(specFilePath.getParent());
      }
      String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
      Files.writeString(specFilePath, payload, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("failed to write spec file: " + specFilePath, e);
    }
  }

  private String resolveCodeRequest(SpecGenerateRequest request) {
    if (request.getCodeUserRequest() != null && !request.getCodeUserRequest().isBlank()) {
      return request.getCodeUserRequest();
    }
    return "Implement the code based on the provided specification JSON.";
  }

  private String errorMessage(Exception e) {
    if (e.getMessage() == null || e.getMessage().isBlank()) {
      return e.getClass().getSimpleName();
    }
    return e.getMessage();
  }
}
