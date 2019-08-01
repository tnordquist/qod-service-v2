package edu.cnm.deepdive.qod.controller;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.qod.QodApplicationTest;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(classes = QodApplicationTest.class)
public class SourceControllerTest extends BaseControllerTest {

  @Autowired
  SourceControllerTest(ObjectMapper mapper,
      WebApplicationContext context, Random rng) {
    super(mapper, context, rng);
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void postSource() throws Exception {
    addSource("George Box")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
    //TEST THIS
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void getSources() throws Exception {
    addSource("George Box");
    addSource("Bill and Ted's Excellent Adventure");
    getMockMvc().perform(
        get("/sources")
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  private ResultActions addSource(String name) throws Exception {
    return getMockMvc().perform(
        post("/sources")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
            .content(String.format("{\"name\": \"%s\"}", name))
    );
  }

}
  