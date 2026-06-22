package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Result of a D1 query, execute, or batch item.
 */
public final class D1Result {
  private final boolean success;
  private final List<Map<String, Object>> rows;
  private final D1Meta meta;
  private final List<D1ResponseInfo> messages;
  private final List<D1ResponseInfo> errors;
  private final String rawBody;

  /**
   * Creates a result model.
   *
   * @param success whether the result item succeeded
   * @param rows row maps returned by D1
   * @param meta result metadata
   * @param messages informational messages
   * @param errors error messages
   * @param rawBody raw HTTP response body
   */
  public D1Result(
      boolean success,
      List<Map<String, Object>> rows,
      D1Meta meta,
      List<D1ResponseInfo> messages,
      List<D1ResponseInfo> errors,
      String rawBody) {
    this.success = success;
    this.rows = immutableRows(rows);
    this.meta = meta == null ? D1Meta.empty() : meta;
    this.messages = messages == null ? List.of() : List.copyOf(messages);
    this.errors = errors == null ? List.of() : List.copyOf(errors);
    this.rawBody = rawBody;
  }

  /**
   * Whether the result item succeeded.
   *
   * @return success flag
   */
  public boolean success() {
    return success;
  }

  /**
   * Returns immutable result rows.
   *
   * @return immutable rows list
   */
  public List<Map<String, Object>> rows() {
    return rows;
  }

  /**
   * Returns result metadata.
   *
   * @return metadata
   */
  public D1Meta meta() {
    return meta;
  }

  /**
   * Returns immutable informational messages.
   *
   * @return immutable message list
   */
  public List<D1ResponseInfo> messages() {
    return messages;
  }

  /**
   * Returns immutable error messages.
   *
   * @return immutable error list
   */
  public List<D1ResponseInfo> errors() {
    return errors;
  }

  /**
   * Returns the raw HTTP response body.
   *
   * @return raw response body
   */
  public String rawBody() {
    return rawBody;
  }

  /**
   * Whether the result has no rows.
   *
   * @return true when no rows are present
   */
  public boolean isEmpty() {
    return rows.isEmpty();
  }

  /**
   * Returns the number of rows.
   *
   * @return row count
   */
  public int rowCount() {
    return rows.size();
  }

  /**
   * Returns the first row, if present.
   *
   * @return first row or empty
   */
  public Optional<Map<String, Object>> firstRow() {
    return rows.stream().findFirst();
  }

  private static List<Map<String, Object>> immutableRows(List<Map<String, Object>> rows) {
    if (rows == null || rows.isEmpty()) {
      return List.of();
    }
    List<Map<String, Object>> copy = new ArrayList<>(rows.size());
    for (Map<String, Object> row : rows) {
      copy.add(Collections.unmodifiableMap(new LinkedHashMap<>(row)));
    }
    return List.copyOf(copy);
  }
}
