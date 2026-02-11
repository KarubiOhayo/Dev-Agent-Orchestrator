package me.karubidev.devagent.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.karubidev.devagent.orchestration.routing.ModelRouter;
import me.karubidev.devagent.orchestration.routing.RouteDecision;
import me.karubidev.devagent.orchestration.routing.RouteRequest;

@RestController
@RequestMapping("/api/routing")
public class RoutingController {

	private final ModelRouter modelRouter;

	public RoutingController(ModelRouter modelRouter) {
		this.modelRouter = modelRouter;
	}

	@PostMapping("/resolve")
	public RouteDecision resolve(@RequestBody RouteRequest request) {
		return modelRouter.resolve(request);
	}
}
