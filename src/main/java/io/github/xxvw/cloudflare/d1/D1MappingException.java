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
  /** Requested target type. */
  private final Class<?> targetType;
  /** Zero-based row index that failed mapping. */
  private final int rowIndex;
  /** Source row that failed mapping. */
  private final Map<String, Object> row;

  /**
   * Creates a mapping exception.
   *
   * @param targetType requested target type
   * @param rowIndex zero-based row index
   * @param row source row
   * @param cause mapping failure
   */
  public D1MappingException(Class<?> targetType, int rowIndex, Map<String, Object> row, Throwable cause) {
    super("D1 row mapping failed", cause, D1Operation.QUERY, null, null,
        Collections.<D1ResponseInfo>emptyList(), Collections.<D1ResponseInfo>emptyList());
    this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
    this.rowIndex = rowIndex;
    this.row = row == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(row));
  }

  /**
   * Requested target type.
   *
   * @return target type
   */
  public Class<?> targetType() {
    return targetType;
  }

  /**
   * Zero-based row index that failed mapping.
   *
   * @return row index
   */
  public int rowIndex() {
    return rowIndex;
  }

  /**
   * Source row that failed mapping.
   *
   * @return immutable source row
   */
  public Map<String, Object> row() {
    return row;
  }
}
