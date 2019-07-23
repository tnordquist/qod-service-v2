/**
 * Interfaces that declare properties exposed for &ldquo;flat&rdquo; serialization of the {@link
 * edu.cnm.deepdive.qod.model.entity.Source} and {@link edu.cnm.deepdive.qod.model.entity.Quote}
 * entities. When serialized this way, instances of those entities will not include nested objects
 * of either type, thus avoiding buffer or stack overflow.
 */
package edu.cnm.deepdive.qod.view;