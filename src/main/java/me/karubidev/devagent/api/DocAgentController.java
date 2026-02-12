package me.karubidev.devagent.api;

import me.karubidev.devagent.agents.doc.DocAgentService;
import me.karubidev.devagent.agents.doc.DocGenerateRequest;
import me.karubidev.devagent.agents.doc.DocGenerateResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents/doc")
public class DocAgentController {

  private final DocAgentService docAgentService;

  public DocAgentController(DocAgentService docAgentService) {
    this.docAgentService = docAgentService;
  }

  @PostMapping("/generate")
  public DocGenerateResponse generate(@RequestBody DocGenerateRequest request) {
    return docAgentService.generate(request);
  }
}
