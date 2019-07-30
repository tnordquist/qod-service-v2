/*
 *  Copyright 2019 Nicholas Bennett & Deep Dive Coding
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.qod.controller;

import edu.cnm.deepdive.qod.model.dao.QuoteRepository;
import edu.cnm.deepdive.qod.model.dao.SourceRepository;
import edu.cnm.deepdive.qod.model.entity.Quote;
import edu.cnm.deepdive.qod.model.entity.Source;
import edu.cnm.deepdive.qod.service.RandomReplacementStage;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.StreamSupport;
import javax.persistence.RollbackException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Defines REST endpoints for servicing requests on {@link Quote} resources, invoking {@link
 * QuoteRepository} methods to perform the required operations.
 */
@RestController
@ExposesResourceFor(Quote.class)
@RequestMapping("/quotes")
public class QuoteController {

  private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L;

  private final QuoteRepository quoteRepository;
  private final SourceRepository sourceRepository;
  private final RandomReplacementStage<Quote> stage;
  private int daysSinceEpoch = Integer.MIN_VALUE;
  private Quote lastQod = null;

  /**
   * Initializes this instance, injecting an instance of {@link QuoteRepository} and an instance of
   * {@link SourceRepository}.
   *
   * @param quoteRepository repository used for operations on {@link Quote} entity instances.
   * @param sourceRepository repository used for operations on {@link Source} entity instances.
   * @param stage container used to select the quote of the day at random from a stream.
   */
  @Autowired
  public QuoteController(QuoteRepository quoteRepository, SourceRepository sourceRepository,
      RandomReplacementStage<Quote> stage) {
    this.quoteRepository = quoteRepository;
    this.sourceRepository = sourceRepository;
    this.stage = stage;
  }

  /**
   * Returns a randomly selected {@link Quote} resource. Unlike {@link #qod()}, this method will
   * usually not give the same result on successive invocations, unless the number of quotes in the
   * database is small.
   *
   * @return random {@link Quote}.
   */
  @GetMapping(value = "random", produces = MediaType.APPLICATION_JSON_VALUE)
  public Quote random() {
    return quoteRepository.findRandom().get();
  }

  /**
   * Returns a randomly selected {@link Quote} resource, presumably for use in "Quote of the
   * Day"-type applications. The most recent selection is retained until that quote is removed, or
   * until the next day (according to the server), so while the quote returned will generally not be
   * the same from one day to the next, invocations during the same day will generally return the
   * same quote.
   *
   * @return random {@link Quote}, regenerated daily.
   */
  @GetMapping(value = "qod", produces = MediaType.APPLICATION_JSON_VALUE)
  public Quote qod() {
    int daysSinceEpoch = (int) (System.currentTimeMillis() / MILLISECONDS_PER_DAY);
    if (lastQod == null
        || !quoteRepository.existsById(lastQod.getId())
        || daysSinceEpoch != this.daysSinceEpoch) {
      lastQod = StreamSupport.stream(quoteRepository.findAll().spliterator(), false)
          .reduce((a, b) -> stage.offer(b))
          .get();
      this.daysSinceEpoch = daysSinceEpoch;
    }
    return lastQod;
  }

  /**
   * Returns a sequence of {@link Quote} resources, containing the specified text.
   *
   * @param fragment text to match (case-insensitive).
   * @return sequence of {@link Quote} resources.
   */
  @GetMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Quote> search(@RequestParam("q") String fragment) {
    return quoteRepository.findAllByTextContainingOrderByTextAsc(fragment);
  }

  /**
   * Returns a sequence of all the {@link Quote} resources in the database.
   *
   * @return sequence of {@link Quote} resources.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Quote> list() {
    return quoteRepository.findAllByOrderByTextAsc();
  }

  /**
   * Adds the provided {@link Quote} resource to the database and returns the completed resource,
   * including timestamp &amp; ID. The provided resource is only required to contain a {@code text}
   * property, with a non-{@code null} value.
   *
   * @param quote partial {@link Quote} resource.
   * @return completed {@link Quote} resource.
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Quote> post(@RequestBody Quote quote) {
    quoteRepository.save(quote);
    return ResponseEntity.created(quote.getHref()).body(quote);
  }

  /**
   * Retrieves and returns the {@link Quote} resource with the specified ID.
   *
   * @param quoteId quote {@link UUID}.
   * @return retrieved {@link Quote} resource.
   */
  @GetMapping(value = "{quoteId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Quote get(@PathVariable("quoteId") UUID quoteId) {
    return quoteRepository.findById(quoteId).get();
  }

  /**
   * Replaces the text (which is the only consumer-specifiable property) of the referenced existing
   * {@link Quote} resource with the text from the provided resource.
   *
   * @param quoteId source {@link UUID}.
   * @param update {@link Quote} resource to use to replace contents of existing quote.
   * @return complete {@link Quote} resource, including new {@code text}.
   */
  @PutMapping(value = "{quoteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Quote put(@PathVariable("quoteId") UUID quoteId, @RequestBody Quote update) {
    Quote quote = quoteRepository.findById(quoteId).get();
    quote.setText(update.getText());
    return quoteRepository.save(quote);
  }

  /**
   * Deletes the specified {@link Quote} resource from the database.
   *
   * @param quoteId quote {@link UUID}.
   */
  @DeleteMapping(value = "{quoteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("quoteId") UUID quoteId) {
    quoteRepository.delete(get(quoteId));
  }

  /**
   * Associates the {@link Source} referenced in the path with the {@link Quote}, also referenced by
   * a path parameter.
   *
   * @param quoteId {@link UUID} of {@link Quote} resource.
   * @param sourceId {@link UUID} of {@link Source} to be associated with referenced {@link Quote}.
   * @return updated {@code Source} resource.
   */
  @PutMapping(value = "{quoteId}/sources/{sourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Source attach(@PathVariable("quoteId") UUID quoteId, @PathVariable UUID sourceId) {
    Source source = sourceRepository.findById(sourceId).get();
    Quote quote = get(quoteId);
    quote.getSources().add(source);
    quoteRepository.save(quote);
    source.getQuotes();
    return source;
  }

  /**
   * Retrieves and returns the referenced {@link Source} resource associated with the referenced
   * {@link Quote} resource. If either does not exist, or if the referenced source is not associated
   * with the quote, this method will fail.
   *
   * @param quoteId {@link UUID} of {@link Quote} resource.
   * @param sourceId {@link UUID} of {@link Source} associated with referenced {@link Quote}.
   * @return referenced {@link Source} resource.
   */
  @GetMapping(value = "{quoteId}/sources/{sourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Source get(@PathVariable("quoteId") UUID quoteId, @PathVariable("sourceId") UUID sourceId) {
    Quote quote = get(quoteId);
    Source source = sourceRepository.findById(sourceId).get();
    if (!quote.getSources().contains(source)) {
      throw new NoSuchElementException();
    }
    return source;
  }

  /**
   * Removes the association between the referenced {@link Quote} resource and the referenced {@link
   * Source} resource. If either does not exist, or if the referenced source is not associated with
   * the quote, this method will fail.
   *
   * @param quoteId {@link UUID} of {@link Quote} resource.
   * @param sourceId {@link UUID} of {@link Source} to be disassociated from referenced {@link
   * Quote}.
   */
  @DeleteMapping(value = "{quoteId}/sources/{sourceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void detach(
      @PathVariable("quoteId") UUID quoteId, @PathVariable("sourceId") UUID sourceId) {
    Quote quote = get(quoteId);
    Source source = get(quoteId, sourceId);
    quote.getSources().remove(source);
    quoteRepository.save(quote);
  }

  /**
   * Maps (via annotation) a {@link NoSuchElementException} to a response status code of {@link
   * HttpStatus#NOT_FOUND}.
   */
  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
  @ExceptionHandler(NoSuchElementException.class)
  public void notFound() {
  }

  /**
   * Maps (via annotation) {@link ConstraintViolationException} and {@link RollbackException} to a
   * response status code of {@link HttpStatus#BAD_REQUEST}.
   */
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid quote")
  @ExceptionHandler({ConstraintViolationException.class, RollbackException.class})
  public void badRequest() {
  }

}
