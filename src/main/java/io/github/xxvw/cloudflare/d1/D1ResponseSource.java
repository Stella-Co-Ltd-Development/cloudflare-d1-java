package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Source location for a D1 response message or error.
 */
public final class D1ResponseSource {
  private final String pointer;
  private final Map<String, Object> additionalProperties;

  public D1ResponseSource(String pointer, Map<String, Object> additionalProperties) {
    this.pointer = pointer;
    this.additionalProperties = additionalProperties == null
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(additionalProperties));
  }

  public Optional<String> pointer() {
    return Optional.ofNullable(pointer);
  }

  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
