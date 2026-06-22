package io.github.xxvw.cloudflare.d1;

import java.util.List;
import java.util.Optional;

/**
 * D1 SQL query failure.
 */
public final class D1QueryException extends D1ApiException {
  /** SQL text associated with the failure. */
  private final String sql;

  /**
   * Creates a query exception.
   *
   * @param operation operation being performed
   * @param statusCode HTTP status code
   * @param rawBody raw HTTP response body
   * @param errors API errors
   * @param messages API messages
   * @param sql SQL text, if available
   */
  public D1QueryException(
      D1Operation operation,
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages,
      String sql) {
    super("D1 query failed", operation, statusCode, rawBody, errors, messages);
    this.sql = sql;
  }

  /**
   * SQL text associated with the failure.
   *
   * @return SQL text or empty
   */
  public Optional<String> sql() {
    return Optional.ofNullable(sql);
  }
}
