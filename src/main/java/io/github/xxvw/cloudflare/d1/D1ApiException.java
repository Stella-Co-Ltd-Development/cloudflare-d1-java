package io.github.xxvw.cloudflare.d1;

import java.util.List;

/**
 * D1 API error response.
 */
public class D1ApiException extends D1Exception {
  /**
   * Creates a D1 API exception.
   *
   * @param message exception message
   * @param operation operation being performed
   * @param statusCode HTTP status code
   * @param rawBody raw HTTP response body
   * @param errors API errors
   * @param messages API messages
   */
  public D1ApiException(
      String message,
      D1Operation operation,
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages) {
    super(message, null, operation, statusCode, rawBody, errors, messages);
  }
}
