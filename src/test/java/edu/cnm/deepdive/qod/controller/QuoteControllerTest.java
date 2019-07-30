package edu.cnm.deepdive.qod.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.qod.QodApplicationTest;
import edu.cnm.deepdive.qod.model.entity.Quote;
import edu.cnm.deepdive.qod.model.entity.Source;
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
class QuoteControllerTest extends BaseControllerTest {

  @Autowired
  QuoteControllerTest(ObjectMapper mapper, WebApplicationContext context, Random rng) {
    super(mapper, context, rng);
  }

  @Test
  void getRandomNotFound() throws Exception {
    getMockMvc().perform(get("/quotes/random").with(oauthTokenRequired()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void getRandom() throws Exception {
    addAndAttribute("Be excellent to each other", "Bill and Ted's Excellent Adventure");
    getMockMvc().perform(get("/quotes/random").with(oauthTokenRequired()))
        .andExpect(status().isOk());
  }

  @Test
  void getQodNotFound() throws Exception {
    getMockMvc().perform(get("/quotes/qod").with(oauthTokenRequired()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void getQod() throws Exception {
    addAndAttribute("I'm your huckleberry.", "Tombstone");
    getMockMvc().perform(get("/quotes/qod").with(oauthTokenRequired()))
        .andExpect(status().isOk());
  }

  @Test
  void getQuotesNone() throws Exception {
    getMockMvc().perform(get("/quotes/").with(oauthTokenRequired()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void getQuotes() throws Exception {
    addAndAttribute("Be excellent to each other", "Bill and Ted's Excellent Adventure");
    addQuote("We begin where we are.");
    getMockMvc().perform(get("/quotes/").with(oauthTokenRequired()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void searchQuotesNoParameter() throws Exception {
    getMockMvc().perform(get("/quotes/search").with(oauthTokenRequired()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void searchQuotesNoMatch() throws Exception {
    getMockMvc().perform(get("/quotes/search?q=excel").with(oauthTokenRequired()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void searchQuotes() throws Exception {
    addQuote("We begin where we are");
    addAndAttribute("Be excellent to each other", "Bill and Ted's Excellent Adventure");
    getMockMvc().perform(get("/quotes/search?q=be").with(oauthTokenRequired()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void postDuplicateQuote() throws Exception {
    addQuote("We begin where we are.");
    addQuote("We begin where we are.")
        .andExpect(status().isBadRequest());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void postQuote() throws Exception {
    addQuote("We begin where we are.")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void putQuoteNoText() throws Exception {
    StringBuilder builder = new StringBuilder();
    String content = "{}";
    addAndAttribute("Ignored", "George Box", builder);
    String url = builder.substring(0, builder.indexOf("/sources"));
    getMockMvc().perform(
        put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
            .content(content))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void putQuoteBlankText() throws Exception {
    StringBuilder builder = new StringBuilder();
    String content = "{\"text\": \"\"}";
    addAndAttribute("Ignored", "George Box", builder);
    String url = builder.substring(0, builder.indexOf("/sources"));
    getMockMvc().perform(
        put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
            .content(content))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void putQuote() throws Exception {
    StringBuilder builder = new StringBuilder();
    String content = "{\"text\": \"Essentially, all models are wrong, but some are useful.\"}";
    addAndAttribute("Ignored", "George Box", builder);
    String url = builder.substring(0, builder.indexOf("/sources"));
    getMockMvc().perform(
        put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
            .content(content))
        .andExpect(status().isOk());
  }

  @Test
  void getQuoteBadId() throws Exception {
    getMockMvc().perform(get("/quotes/XXXXXXXX").with(oauthTokenRequired()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getQuoteNotFound() throws Exception {
    getMockMvc().perform(get("/quotes/01234567-89AB-CDEF-0123-456789ABCDEF").with(oauthTokenRequired()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void getQuote() throws Exception {
    StringBuilder builder = new StringBuilder();
    addAndAttribute("Be excellent to each other", "Bill and Ted's Excellent Adventure", builder);
    getMockMvc().perform(get(builder.substring(0, builder.indexOf("/sources")))
        .with(oauthTokenRequired()))
        .andExpect(status().isOk());
  }

  @Test
  void deleteQuoteBadId() throws Exception {
    getMockMvc().perform(delete("/quotes/XXXXXXXX").with(oauthTokenRequired()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void deleteQuoteNotFound() throws Exception {
    getMockMvc().perform(delete("/quotes/01234567-89AB-CDEF-0123-456789ABCDEF").with(oauthTokenRequired()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void deleteQuote() throws Exception {
    StringBuilder builder = new StringBuilder("/quotes/");
    addQuote("Ignored")
        .andDo(mvcResult -> {
          Quote quote = getMapper().readValue(mvcResult.getResponse().getContentAsString(), Quote.class);
          builder.append(quote.getId().toString());
        });
    getMockMvc().perform(delete(builder.toString()).with(oauthTokenRequired())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void attributeQuote() throws Exception {
    addAndAttribute("I'm your huckleberry.", "Tombstone")
        .andExpect(status().isOk());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void getQuoteAttribution() throws Exception {
    StringBuilder builder = new StringBuilder();
    addAndAttribute("I'm your huckleberry.", "Tombstone", builder);
    getMockMvc().perform(get(builder.toString()).with(oauthTokenRequired()))
        .andExpect(status().isOk());
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void unattributeQuote() throws Exception {
    StringBuilder builder = new StringBuilder();
    addAndAttribute("I'm your huckleberry.", "Tombstone", builder);
    getMockMvc().perform(delete(builder.toString()).with(oauthTokenRequired()))
        .andExpect(status().isNoContent());
  }

  ResultActions addQuote(String text) throws Exception {
    return getMockMvc().perform(
        post("/quotes")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
            .content(String.format("{\"text\": \"%s\"}", text))
    );
  }

  ResultActions addSource(String name) throws Exception {
    return getMockMvc().perform(
        post("/sources")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(oauthTokenRequired())
            .content(String.format("{\"name\": \"%s\"}", name))
    );
  }

  ResultActions addAndAttribute(String text, String name, StringBuilder builder) throws Exception {
    builder.append("/quotes/");
    addQuote(text).andDo(mvcResult -> {
      Quote quote = getMapper().readValue(mvcResult.getResponse().getContentAsString(), Quote.class);
      builder.append(quote.getId().toString()).append("/sources/");
    });
    addSource(name).andDo(mvcResult -> {
      Source source = getMapper().readValue(mvcResult.getResponse().getContentAsString(), Source.class);
      builder.append(source.getId().toString());
    });
    return getMockMvc().perform(put(builder.toString()).with(oauthTokenRequired()));
  }

  ResultActions addAndAttribute(String text, String name) throws Exception {
    return addAndAttribute(text, name, new StringBuilder());
  }

}