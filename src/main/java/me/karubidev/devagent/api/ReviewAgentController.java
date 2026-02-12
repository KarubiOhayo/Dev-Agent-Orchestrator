package me.karubidev.devagent.api;

import me.karubidev.devagent.agents.review.ReviewAgentService;
import me.karubidev.devagent.agents.review.ReviewGenerateRequest;
import me.karubidev.devagent.agents.review.ReviewGenerateResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents/review")
public class ReviewAgentController {

  private final ReviewAgentService reviewAgentService;

  public ReviewAgentController(ReviewAgentService reviewAgentService) {
    this.reviewAgentService = reviewAgentService;
  }

  @PostMapping("/generate")
  public ReviewGenerateResponse generate(@RequestBody ReviewGenerateRequest request) {
    return reviewAgentService.generate(request);
  }
}
