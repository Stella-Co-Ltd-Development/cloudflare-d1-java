package io.github.xxvw.cloudflare.d1;

import java.util.List;

/**
 * D1 API authorization failure.
 */
public final class D1AuthorizationException extends D1ApiException {
  public D1AuthorizationException(
      D1Operation operation,
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages) {
    super("D1 authorization failed", operation, statusCode, rawBody, errors, messages);
  }
}
