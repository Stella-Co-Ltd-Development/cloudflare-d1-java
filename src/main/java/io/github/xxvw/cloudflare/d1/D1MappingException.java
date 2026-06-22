package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed row mapping failure.
 */
public final class D1MappingException extends D1Exception {
  private final Class<?> targetType;
  private final int rowIndex;
  private final Map<String, Object> row;

  public D1MappingException(Class<?> targetType, int rowIndex, Map<String, Object> row, Throwable cause) {
    super("D1 row mapping failed", cause, D1Operation.QUERY, null, null, List.of(), List.of());
    this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
    this.rowIndex = rowIndex;
    this.row = row == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(row));
  }

  public Class<?> targetType() {
    return targetType;
  }

  public int rowIndex() {
    return rowIndex;
  }

  public Map<String, Object> row() {
    return row;
  }
}
