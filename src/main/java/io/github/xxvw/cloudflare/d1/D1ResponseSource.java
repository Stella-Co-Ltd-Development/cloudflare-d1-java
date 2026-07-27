package io.github.xxvw.cloudflare.d1;

import io.github.xxvw.cloudflare.d1.internal.D1ImmutableValues;
import java.util.Map;
import java.util.Optional;

/**
 * Source location for a D1 response message or error.
 */
public final class D1ResponseSource {
  private final String pointer;
  private final Map<String, Object> additionalProperties;

  /**
   * Creates a response source model.
   *
   * @param pointer source pointer, if present
   * @param additionalProperties unknown source fields
   */
  public D1ResponseSource(String pointer, Map<String, Object> additionalProperties) {
    this.pointer = pointer;
    this.additionalProperties = D1ImmutableValues.immutableMap(additionalProperties);
  }

  /**
   * Source pointer.
   *
   * @return source pointer or empty
   */
  public Optional<String> pointer() {
    return Optional.ofNullable(pointer);
  }

  /**
   * Unknown source fields preserved from the API response.
   *
   * @return immutable additional properties
   */
  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
