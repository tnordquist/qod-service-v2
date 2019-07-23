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
package edu.cnm.deepdive.qod.model.dao;

import edu.cnm.deepdive.qod.model.entity.Quote;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Declares database operations that can be performed on {@link Quote} entity instances.
 */
public interface QuoteRepository extends CrudRepository<Quote, UUID> {

  /**
   * Selects and returns all {@link Quote} instances, sorted in alphabetical order.
   *
   * @return {@link Iterable} sequence of {@link Quote} instances.
   */
  Iterable<Quote> findAllByOrderByTextAsc();

  /**
   * Selects and returns all {@link Quote} instances containing the specified text fragment, in
   * alphabetical order.
   *
   * @param fragment filter text content.
   * @return {@link Iterable} sequence of {@link Quote} instances.
   */
  Iterable<Quote> findAllByTextContainingOrderByTextAsc(String fragment);

  /**
   * Selects and returns a randomly selected {@link Quote} instance. Note that this is currently
   * implemented with Derby-specific SQL, since there is not a JPQL-standard way of sorting on a
   * random value (i.e. shuffling), nor of limiting the number of rows returned.
   *
   * @return random {@link Quote} instance.
   */
  @Query(value = "SELECT * FROM sa.quote ORDER BY RANDOM() OFFSET 0 ROWS FETCH NEXT 1 ROW ONLY",
      nativeQuery = true)
  Optional<Quote> findRandom();

}
