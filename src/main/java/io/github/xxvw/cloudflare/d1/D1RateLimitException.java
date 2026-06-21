package io.github.xxvw.cloudflare.d1;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * D1 API rate limit failure.
 */
public final class D1RateLimitException extends D1ApiException {
  private final Duration retryAfter;

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

  public Optional<Duration> retryAfter() {
    return Optional.ofNullable(retryAfter);
  }
}
