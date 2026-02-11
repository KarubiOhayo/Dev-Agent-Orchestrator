package me.karubidev.devagent.api;

import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.code.CodeGenerateResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents/code")
public class CodeAgentController {

  private final CodeAgentService codeAgentService;

  public CodeAgentController(CodeAgentService codeAgentService) {
    this.codeAgentService = codeAgentService;
  }

  @PostMapping("/generate")
  public CodeGenerateResponse generate(@RequestBody CodeGenerateRequest request) {
    return codeAgentService.generate(request);
  }
}
