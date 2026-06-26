package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a D1 raw query item.
 *
 * <p>Raw results represent rows as arrays and column names as a separate list.
 */
public final class D1RawResult {
  private final boolean success;
  private final List<String> columns;
  private final List<List<Object>> rows;
  private final D1Meta meta;
  private final List<D1ResponseInfo> messages;
  private final List<D1ResponseInfo> errors;
  private final String rawBody;

  /**
   * Creates a raw result model.
   *
   * @param success whether the result item succeeded
   * @param columns column names returned by D1
   * @param rows raw row arrays returned by D1
   * @param meta result metadata
   * @param messages informational messages
   * @param errors error messages
   * @param rawBody raw HTTP response body
   */
  public D1RawResult(
      boolean success,
      List<String> columns,
      List<List<Object>> rows,
      D1Meta meta,
      List<D1ResponseInfo> messages,
      List<D1ResponseInfo> errors,
      String rawBody) {
    this.success = success;
    this.columns = columns == null
        ? Collections.<String>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(columns));
    this.rows = immutableRows(rows);
    this.meta = meta == null ? D1Meta.empty() : meta;
    this.messages = messages == null
        ? Collections.<D1ResponseInfo>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(messages));
    this.errors = errors == null
        ? Collections.<D1ResponseInfo>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(errors));
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
   * Returns immutable column names.
   *
   * @return immutable column list
   */
  public List<String> columns() {
    return columns;
  }

  /**
   * Returns immutable raw row arrays.
   *
   * @return immutable row list
   */
  public List<List<Object>> rows() {
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

  private static List<List<Object>> immutableRows(List<List<Object>> rows) {
    if (rows == null || rows.isEmpty()) {
      return Collections.emptyList();
    }
    List<List<Object>> copy = new ArrayList<>(rows.size());
    for (List<Object> row : rows) {
      copy.add(row == null
          ? Collections.<Object>emptyList()
          : Collections.unmodifiableList(new ArrayList<>(row)));
    }
    return Collections.unmodifiableList(copy);
  }
}
