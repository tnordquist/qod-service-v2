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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import javax.transaction.Transactional;
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
 * Defines REST endpoints for servicing requests on {@link Source} resources, invoking {@link
 * SourceRepository} methods to perform the required operations.
 */
@RestController
@ExposesResourceFor(Source.class)
@RequestMapping("/sources")
public class SourceController {

  private SourceRepository sourceRepository;
  private QuoteRepository quoteRepository;

  /**
   * Initializes this instance, injecting an instance of {@link SourceRepository} and an instance of
   * {@link QuoteRepository}.
   *
   * @param sourceRepository repository used for operations on {@link Source} entity instances.
   * @param quoteRepository repository used for operations on {@link Quote} entity instances.
   */
  @Autowired
  public SourceController(SourceRepository sourceRepository, QuoteRepository quoteRepository) {
    this.sourceRepository = sourceRepository;
    this.quoteRepository = quoteRepository;
  }

  /**
   * Returns a sequence of {@link Source} resources, containing the specified text.
   *
   * @param fragment text to match (case-insensitive).
   * @return sequence of {@link Source} resources.
   */
  @GetMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Source> search(@RequestParam("q") String fragment) {
    return sourceRepository.findAllByNameContainingOrderByNameAsc(fragment);
  }

  /**
   * Returns a sequence of all the {@link Source} resources in the database.
   *
   * @return sequence of {@link Source} resources.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Source> get() {
    return sourceRepository.findAllByOrderByNameAsc();
  }

  /**
   * Adds the provided {@link Source} resource to the database and returns the completed resource,
   * including timestamp &amp; ID. The provided resource is only required to contain a
   * <code>name</code> property, with a non-<code>null</code> value.
   *
   * @param source partial {@link Source} resource.
   * @return completed {@link Source} resource.
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(value = HttpStatus.CREATED)
  public ResponseEntity<Source> post(@RequestBody Source source) {
    sourceRepository.save(source);
    return ResponseEntity.created(source.getHref()).body(source);
  }

  /**
   * Retrieves and returns the {@link Source} resource with the specified ID.
   *
   * @param sourceId source {@link UUID}.
   * @return retrieved {@link Source} resource.
   */
  @GetMapping(value = "{sourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Source get(@PathVariable("sourceId") UUID sourceId) {
    return sourceRepository.findById(sourceId).get();
  }

  /**
   * Replaces the name (which is the only consumer-specifiable property) of the referenced existing
   * {@link Source} resource with the name from the provided resource.
   *
   * @param sourceId source {@link UUID}.
   * @param update {@link Source} resource to use to replace contents of existing source.
   */
  @PutMapping(value = "{sourceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void put(@PathVariable("sourceId") UUID sourceId, @RequestBody Source update) {
    Source source = sourceRepository.findById(sourceId).get();
    source.setName(update.getName());
    sourceRepository.save(source);
  }

  /**
   * Deletes the specified {@link Source} resource from the database.
   *
   * @param sourceId source {@link UUID}.
   */
  @Transactional
  @DeleteMapping(value = "{sourceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("sourceId") UUID sourceId) {
    Source source = get(sourceId);
    Set<Quote> quotes = source.getQuotes();
    for (Quote quote : quotes) {
      quote.getSources().remove(source);
    }
    quoteRepository.saveAll(quotes);
    sourceRepository.delete(source);
  }

  /**
   * Associates the {@link Quote} specified in the request body with the {@link Source} referenced
   * by the path parameter. Only the <code>id</code> property of the {@link Quote} must be
   * specified.
   *
   * @param sourceId {@link UUID} of {@link Source} resource.
   * @param quote {@link Quote} to be associated with referenced {@link Source}.
   * @return updated {@link Source} resource.
   */
  @PostMapping(value = "{sourceId}/quotes",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Source attach(@PathVariable("sourceId") UUID sourceId, @RequestBody Quote quote) {
    return attach(sourceId, quote.getId());
  }

  /**
   * Retrieves and returns all of the referenced {@link Quote} resources associated with the
   * referenced {@link Source} resource. If the latter does not exist, this method will fail.
   *
   * @param sourceId {@link UUID} of {@link Source} resource.
   * @return collection of {@link Quote} resources associated with specified {@link Source}.
   */
  @GetMapping(value = "{sourceId}/quotes", produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Quote> list(@PathVariable("sourceId") UUID sourceId) {
    Source source = get(sourceId);
    return source.getQuotes();
  }

  /**
   * Associates the {@link Quote} referenced in the path with the {@link Source}, also referenced by
   * a path parameter.
   *
   * @param sourceId {@link UUID} of {@link Source} resource.
   * @param quoteId {@link UUID} of {@link Quote} to be associated with referenced {@link Source}.
   * @return updated {@link Source} resource.
   */
  @PutMapping(value = "{sourceId}/quotes/{quoteId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Source attach(
      @PathVariable("sourceId") UUID sourceId, @PathVariable("quoteId") UUID quoteId) {
    Quote quote = quoteRepository.findById(quoteId).get();
    Source source = get(sourceId);
    quote.getSources().add(source);
    quoteRepository.save(quote);
    return source;
  }

  /**
   * Retrieves and returns the referenced {@link Quote} resource associated with the referenced
   * {@link Source} resource. If either does not exist, or if the referenced quote is not associated
   * with the source, this method will fail.
   *
   * @param sourceId {@link UUID} of {@link Source} resource.
   * @param quoteId {@link UUID} of {@link Quote} associated with referenced {@link Source}.
   * @return referenced {@link Quote} resource.
   */
  @GetMapping(value = "{sourceId}/quotes/{quoteId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Quote get(@PathVariable("sourceId") UUID sourceId, @PathVariable("quoteId") UUID quoteId) {
    Source source = get(sourceId);
    Quote quote = quoteRepository.findById(quoteId).get();
    if (!source.getQuotes().contains(quote)) {
      throw new NoSuchElementException();
    }
    return quote;
  }

  /**
   * Removes the association between the referenced {@link Source} resource and the referenced
   * {@link Quote} resource. If either does not exist, or if the referenced source is not associated
   * with the quote, this method will fail.
   *
   * @param sourceId {@link UUID} of {@link Source} resource.
   * @param quoteId {@link UUID} of {@link Quote} to be disassociated from the referenced {@link
   * Source}.
   */
  @DeleteMapping(value = "{sourceId}/quotes/{quoteId}")
  public void detach(@PathVariable("sourceId") UUID sourceId, @PathVariable("quoteId") UUID quoteId) {
    Source source = get(sourceId);
    Quote quote = get(sourceId, quoteId);
    quote.getSources().remove(source);
    quoteRepository.save(quote);
  }

  /**
   * Maps (via annotation) a {@link NoSuchElementException} to a response status code of {@link
   * HttpStatus#NOT_FOUND}.
   */
  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
  @ExceptionHandler(NoSuchElementException.class)
  public void notFound() {}
  
}
