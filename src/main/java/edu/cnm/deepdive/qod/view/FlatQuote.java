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
package edu.cnm.deepdive.qod.view;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Declares the getters (and thus the JSON properties) of a quote for serialization, excluding
 * references to other objects that could result in stack or buffer overflow on serialization.
 */
public interface FlatQuote {

  /**
   * Returns the universally unique ID (UUID) of a quote resource.
   *
   * @return quote UUID.
   */
  UUID getId();

  /**
   * Returns the date-time stamp recorded when a quote resource is first written to the database.
   *
   * @return creation timestamp.
   */
  Date getCreated();

  /**
   * Returns the text of the quote.
   *
   * @return quote text.
   */
  String getText();

  /**
   * Returns a URL referencing the quote resource.
   *
   * @return quote URL.
   */
  URI getHref();

}
