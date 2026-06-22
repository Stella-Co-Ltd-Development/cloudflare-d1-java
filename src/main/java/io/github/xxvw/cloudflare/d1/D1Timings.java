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

  /**
   * Creates a timings model.
   *
   * @param sqlDurationMs SQL duration in milliseconds
   * @param additionalProperties unknown timing fields
   */
  public D1Timings(double sqlDurationMs, Map<String, Object> additionalProperties) {
    this.sqlDurationMs = sqlDurationMs;
    this.additionalProperties = additionalProperties == null
        ? Collections.<String, Object>emptyMap()
        : Collections.unmodifiableMap(new LinkedHashMap<>(additionalProperties));
  }

  /**
   * SQL duration in milliseconds.
   *
   * @return SQL duration in milliseconds
   */
  public double sqlDurationMs() {
    return sqlDurationMs;
  }

  /**
   * Unknown timing fields preserved from the API response.
   *
   * @return immutable additional properties
   */
  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
