package io.github.xxvw.cloudflare.d1;

import java.util.List;

/**
 * D1 API authentication failure.
 */
public final class D1AuthenticationException extends D1ApiException {
  public D1AuthenticationException(
      D1Operation operation,
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages) {
    super("D1 authentication failed", operation, statusCode, rawBody, errors, messages);
  }
}
