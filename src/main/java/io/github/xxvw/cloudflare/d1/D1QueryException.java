package io.github.xxvw.cloudflare.d1;

import java.util.List;
import java.util.Optional;

/**
 * D1 SQL query failure.
 */
public final class D1QueryException extends D1ApiException {
  private final String sql;

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

  public Optional<String> sql() {
    return Optional.ofNullable(sql);
  }
}
