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

import edu.cnm.deepdive.qod.model.entity.Source;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

/**
 * Declares database operations that can be performed on {@link Source} entity instances.
 */
public interface SourceRepository extends CrudRepository<Source, UUID> {

  /**
   * Selects and returns all {@link Source} instances, sorted in alphabetical order.
   *
   * @return {@link Iterable} sequence of {@link Source} instances.
   */
  Iterable<Source> findAllByOrderByNameAsc();

  /**
   * Selects and returns all {@link Source} instances containing the specified text fragment, in
   * alphabetical order.
   *
   * @param fragment filter text content.
   * @return {@link Iterable} sequence of {@link Source} instances.
   */
  Iterable<Source> findAllByNameContainingOrderByNameAsc(String fragment);

}
