package io.github.xxvw.cloudflare.d1;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * D1 API rate limit failure.
 */
public final class D1RateLimitException extends D1ApiException {
  /** Retry delay returned by the API. */
  private final Duration retryAfter;

  /**
   * Creates a rate limit exception.
   *
   * @param operation operation being performed
   * @param statusCode HTTP status code
   * @param rawBody raw HTTP response body
   * @param errors API errors
   * @param messages API messages
   * @param retryAfter retry delay from the Retry-After header, if available
   */
  public D1RateLimitException(
      D1Operation operation,
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages,
      Duration retryAfter) {
    super("D1 rate limit exceeded", operation, statusCode, rawBody, errors, messages);
    this.retryAfter = retryAfter;
  }

  /**
   * Retry delay returned by the API.
   *
   * @return retry delay or empty
   */
  public Optional<Duration> retryAfter() {
    return Optional.ofNullable(retryAfter);
  }
}
