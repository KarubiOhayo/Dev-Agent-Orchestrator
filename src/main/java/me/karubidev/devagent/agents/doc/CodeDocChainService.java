package me.karubidev.devagent.agents.doc;

import java.nio.file.Path;
import java.util.List;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import me.karubidev.devagent.agents.code.apply.GeneratedFile;
import me.karubidev.devagent.state.RunStateStore;
import org.springframework.stereotype.Service;

@Service
public class CodeDocChainService {

  private final DocAgentService docAgentService;
  private final RunStateStore runStateStore;

  public CodeDocChainService(DocAgentService docAgentService, RunStateStore runStateStore) {
    this.docAgentService = docAgentService;
    this.runStateStore = runStateStore;
  }

  public DocGenerateResponse runChain(
      String codeRunId,
      CodeGenerateRequest request,
      CodeGenerateResponse codeResponse,
      Path targetRoot
  ) {
    if (!request.isChainToDoc()) {
      return null;
    }

    try {
      Path safeTargetRoot = targetRoot.toAbsolutePath().normalize();
      DocGenerateRequest docRequest = new DocGenerateRequest();
      docRequest.setProjectId(request.getProjectId());
      docRequest.setTargetProjectRoot(safeTargetRoot.toString());
      docRequest.setUserRequest(resolveDocRequest(request));
      docRequest.setMode(request.getMode());
      docRequest.setRiskLevel(request.getRiskLevel());
      docRequest.setLargeContext(request.isLargeContext());
      docRequest.setStrictJsonRequired(request.isStrictJsonRequired());
      docRequest.setCodeRunId(codeRunId);
      docRequest.setCodeOutput(codeResponse.output());

      List<GeneratedFile> files = codeResponse.files() == null ? List.of() : codeResponse.files();
      docRequest.setCodeFiles(files);

      runStateStore.appendEvent(codeRunId, "CHAIN_DOC_TRIGGERED", "files=" + files.size());
      DocGenerateResponse response = docAgentService.generate(docRequest);
      runStateStore.appendEvent(codeRunId, "CHAIN_DOC_DONE", "docRunId=" + response.runId());
      return response;
    } catch (Exception e) {
      runStateStore.appendEvent(codeRunId, "CHAIN_DOC_FAILED", "reason=" + errorMessage(e));
      throw e;
    }
  }

  private String resolveDocRequest(CodeGenerateRequest request) {
    if (request.getDocUserRequest() != null && !request.getDocUserRequest().isBlank()) {
      return request.getDocUserRequest();
    }
    return "Generate developer-facing documentation based on the generated code result.";
  }

  private String errorMessage(Exception e) {
    if (e.getMessage() == null || e.getMessage().isBlank()) {
      return e.getClass().getSimpleName();
    }
    return e.getMessage();
  }
}
