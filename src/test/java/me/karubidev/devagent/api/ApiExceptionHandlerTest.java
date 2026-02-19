package me.karubidev.devagent.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.karubidev.devagent.agents.code.CodeAgentService;
import me.karubidev.devagent.agents.code.CodeGenerateRequest;
import me.karubidev.devagent.agents.doc.DocAgentService;
import me.karubidev.devagent.agents.review.ReviewAgentService;
import me.karubidev.devagent.agents.spec.SpecAgentService;
import me.karubidev.devagent.agents.spec.SpecGenerateRequest;
import me.karubidev.devagent.api.error.ApiExceptionHandler;
import me.karubidev.devagent.orchestration.routing.ModelRouter;
import me.karubidev.devagent.orchestration.routing.RouteRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiExceptionHandlerTest {

  private MockMvc mockMvc;
  private CodeAgentService codeAgentService;
  private SpecAgentService specAgentService;
  private ReviewAgentService reviewAgentService;
  private ModelRouter modelRouter;

  @BeforeEach
  void setUp() {
    codeAgentService = Mockito.mock(CodeAgentService.class);
    specAgentService = Mockito.mock(SpecAgentService.class);
    DocAgentService docAgentService = Mockito.mock(DocAgentService.class);
    reviewAgentService = Mockito.mock(ReviewAgentService.class);
    modelRouter = Mockito.mock(ModelRouter.class);

    when(codeAgentService.generate(any())).thenAnswer(invocation -> {
      CodeGenerateRequest request = invocation.getArgument(0);
      boolean hasUserRequest = request != null
          && request.getUserRequest() != null
          && !request.getUserRequest().isBlank();
      boolean hasSpecInputPath = request != null
          && request.getSpecInputPath() != null
          && !request.getSpecInputPath().isBlank();
      if (!hasUserRequest && !hasSpecInputPath) {
        throw new IllegalArgumentException("userRequest or specInputPath is required");
      }
      if (!hasUserRequest && "missing-spec.json".equals(request.getSpecInputPath())) {
        throw new IllegalArgumentException("userRequest or readable specInputPath is required");
      }
      return null;
    });

    when(specAgentService.generate(any())).thenAnswer(invocation -> {
      SpecGenerateRequest request = invocation.getArgument(0);
      if (request == null || request.getUserRequest() == null || request.getUserRequest().isBlank()) {
        throw new IllegalArgumentException("userRequest is required");
      }
      return null;
    });

    when(modelRouter.resolve(any())).thenAnswer(invocation -> {
      RouteRequest request = invocation.getArgument(0);
      if (request == null || request.getAgentType() == null) {
        throw new IllegalArgumentException("agentType is required");
      }
      return null;
    });

    mockMvc = MockMvcBuilders.standaloneSetup(
            new CodeAgentController(codeAgentService),
            new SpecAgentController(specAgentService),
            new DocAgentController(docAgentService),
            new ReviewAgentController(reviewAgentService),
            new RoutingController(modelRouter)
        )
        .setControllerAdvice(new ApiExceptionHandler())
        .build();
  }

  @Test
  void returns400EnvelopeForMissingRequiredField() throws Exception {
    mockMvc.perform(post("/api/agents/spec/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_FIELD"))
        .andExpect(jsonPath("$.message").value("userRequest is required"))
        .andExpect(jsonPath("$.path").value("/api/agents/spec/generate"))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.details[0].field").value("userRequest"))
        .andExpect(jsonPath("$.details[0].reason").value("required"));
  }

  @Test
  void returns400EnvelopeForMissingAnyOfRequiredFields() throws Exception {
    mockMvc.perform(post("/api/agents/code/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_ANY_OF"))
        .andExpect(jsonPath("$.message").value("userRequest or specInputPath is required"))
        .andExpect(jsonPath("$.path").value("/api/agents/code/generate"))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.details[0].field").value("userRequest"))
        .andExpect(jsonPath("$.details[0].reason").value("any_of_required"))
        .andExpect(jsonPath("$.details[1].field").value("specInputPath"))
        .andExpect(jsonPath("$.details[1].reason").value("any_of_required"));
  }

  @Test
  void returns400EnvelopeForMissingAnyOfRequiredFieldsWithReadableQualifier() throws Exception {
    mockMvc.perform(post("/api/agents/code/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "specInputPath": "missing-spec.json"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_ANY_OF"))
        .andExpect(jsonPath("$.message").value("userRequest or readable specInputPath is required"))
        .andExpect(jsonPath("$.path").value("/api/agents/code/generate"))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.details[0].field").value("userRequest"))
        .andExpect(jsonPath("$.details[0].reason").value("any_of_required"))
        .andExpect(jsonPath("$.details[1].field").value("specInputPath"))
        .andExpect(jsonPath("$.details[1].reason").value("any_of_required"));
  }

  @Test
  void returns400EnvelopeForInvalidEnum() throws Exception {
    mockMvc.perform(post("/api/agents/code/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userRequest": "로그인 API를 생성해줘",
                  "mode": "NOT_A_MODE"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("INVALID_ENUM_VALUE"))
        .andExpect(jsonPath("$.message").value("Invalid enum value"))
        .andExpect(jsonPath("$.path").value("/api/agents/code/generate"))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.details[0].field").value("mode"))
        .andExpect(jsonPath("$.details[0].reason").value(containsString("BALANCED")))
        .andExpect(jsonPath("$.details[0].rejectedValue").value("NOT_A_MODE"));
  }

  @Test
  void returns400EnvelopeForMalformedJson() throws Exception {
    mockMvc.perform(post("/api/agents/review/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userRequest": "리뷰해줘"
                """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
        .andExpect(jsonPath("$.message").value("Malformed JSON request body"))
        .andExpect(jsonPath("$.path").value("/api/agents/review/generate"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void returns400EnvelopeWhenRoutingRequestMissesAgentType() throws Exception {
    mockMvc.perform(post("/api/routing/resolve")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_FIELD"))
        .andExpect(jsonPath("$.message").value("agentType is required"))
        .andExpect(jsonPath("$.path").value("/api/routing/resolve"))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.details[0].field").value("agentType"))
        .andExpect(jsonPath("$.details[0].reason").value("required"));
  }

  @Test
  void returns400EnvelopeWhenRequestBodyIsMissing() throws Exception {
    mockMvc.perform(post("/api/agents/doc/generate")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("REQUEST_BODY_REQUIRED"))
        .andExpect(jsonPath("$.message").value("Request body is required"))
        .andExpect(jsonPath("$.path").value("/api/agents/doc/generate"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void returns500EnvelopeForUnexpectedServerError() throws Exception {
    when(reviewAgentService.generate(any())).thenThrow(new RuntimeException("unexpected failure"));

    mockMvc.perform(post("/api/agents/review/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userRequest": "리뷰를 생성해줘"
                }
                """))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.message").value("Internal server error"))
        .andExpect(jsonPath("$.path").value("/api/agents/review/generate"))
        .andExpect(jsonPath("$.timestamp").isString());
  }
}
