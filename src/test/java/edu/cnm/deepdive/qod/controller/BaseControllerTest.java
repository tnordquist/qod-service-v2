package edu.cnm.deepdive.qod.controller;

import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class BaseControllerTest {

  private static final String OAUTH_NOTICE =
      "OAuth2.0 bearer token required in `Authorization` header.";

  private MockMvc mockMvc;
  private final ObjectMapper mapper;
  private final WebApplicationContext context;
  private final Random rng;

  protected BaseControllerTest(ObjectMapper mapper, WebApplicationContext context, Random rng) {
    this.mapper = mapper;
    this.context = context;
    this.rng = rng;
  }

  @BeforeEach
  protected void setUp(RestDocumentationContextProvider provider) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .alwaysDo(JacksonResultHandlers.prepareJackson(mapper))
        .apply(
            documentationConfiguration(provider)
                .uris()
                .withScheme("https")
                .withHost("java-bootcamp.cnm.edu/rest/qod")
                .withPort(443)
                .and()
                .snippets()
                .withDefaults(
                    HttpDocumentation.httpRequest(),
                    HttpDocumentation.httpResponse(),
                    AutoDocumentation.requestHeaders(),
                    AutoDocumentation.authorization(OAUTH_NOTICE),
                    AutoDocumentation.pathParameters(),
                    AutoDocumentation.requestParameters(),
                    AutoDocumentation.requestFields(),
                    AutoDocumentation.responseFields(),
                    AutoDocumentation.description(),
                    AutoDocumentation.methodAndPath(),
                    AutoDocumentation.section()
                )
        )
        .alwaysDo(document("{class-name}/{method-name}",
            preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
        .build();
  }

  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  protected ObjectMapper getMapper() {
    return mapper;
  }

  protected WebApplicationContext getContext() {
    return context;
  }

  protected RequestPostProcessor oauthTokenRequired() {
    return (request) -> {
      byte[] bytes = new byte[48];
      rng.nextBytes(bytes);
      request.addHeader("Authorization", String.format("Bearer %s",
          Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)));
      return documentAuthorization(request, OAUTH_NOTICE);
    };
  }

}
