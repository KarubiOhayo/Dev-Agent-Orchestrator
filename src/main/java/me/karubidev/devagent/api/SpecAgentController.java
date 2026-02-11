package me.karubidev.devagent.api;

import me.karubidev.devagent.agents.spec.SpecAgentService;
import me.karubidev.devagent.agents.spec.SpecGenerateRequest;
import me.karubidev.devagent.agents.spec.SpecGenerateResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents/spec")
public class SpecAgentController {

  private final SpecAgentService specAgentService;

  public SpecAgentController(SpecAgentService specAgentService) {
    this.specAgentService = specAgentService;
  }

  @PostMapping("/generate")
  public SpecGenerateResponse generate(@RequestBody SpecGenerateRequest request) {
    return specAgentService.generate(request);
  }
}
