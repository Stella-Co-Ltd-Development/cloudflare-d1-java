package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Timing metadata returned by D1.
 */
public final class D1Timings {
  private final double sqlDurationMs;
  private final Map<String, Object> additionalProperties;

  public D1Timings(double sqlDurationMs, Map<String, Object> additionalProperties) {
    this.sqlDurationMs = sqlDurationMs;
    this.additionalProperties = additionalProperties == null
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(additionalProperties));
  }

  public double sqlDurationMs() {
    return sqlDurationMs;
  }

  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
