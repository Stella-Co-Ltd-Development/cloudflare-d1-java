package io.github.xxvw.cloudflare.d1.internal;

import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1RetryPolicy;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class D1RetryExecutor {
  private final D1RetryPolicy retryPolicy;
  private final D1RetryAfterParser retryAfterParser;
  private final D1Sleeper sleeper;

  public D1RetryExecutor(D1RetryPolicy retryPolicy) {
    this(retryPolicy, new D1RetryAfterParser(), duration -> Thread.sleep(duration.toMillis()));
  }

  D1RetryExecutor(D1RetryPolicy retryPolicy, D1RetryAfterParser retryAfterParser, D1Sleeper sleeper) {
    this.retryPolicy = retryPolicy;
    this.retryAfterParser = retryAfterParser;
    this.sleeper = sleeper;
  }

  public D1HttpResponse execute(D1Operation operation, ThrowingSupplier<D1HttpResponse> supplier) {
    int attempts = 0;
    while (true) {
      D1HttpResponse response = supplier.get();
      if (!shouldRetry(operation, response.statusCode(), attempts)) {
        return response;
      }
      attempts++;
      sleep(operation, delayForAttempt(attempts, response));
    }
  }

  private boolean shouldRetry(D1Operation operation, int statusCode, int attempts) {
    return retryPolicy.retries(operation)
        && attempts < retryPolicy.maxRetries()
        && retryPolicy.retryStatusCodes().contains(statusCode);
  }

  private Duration delayForAttempt(int retryAttempt, D1HttpResponse response) {
    if (retryPolicy.respectRetryAfter()) {
      Optional<Duration> retryAfter = parseRetryAfter(response.firstHeader("Retry-After"));
      if (retryAfter.isPresent()) {
        return retryAfter.get();
      }
    }
    Duration calculatedDelay = retryPolicy.baseDelay().multipliedBy(1L << Math.max(0, retryAttempt - 1));
    if (calculatedDelay.compareTo(retryPolicy.maxDelay()) > 0) {
      calculatedDelay = retryPolicy.maxDelay();
    }
    if (!retryPolicy.jitter() || calculatedDelay.isZero()) {
      return calculatedDelay;
    }
    long millis = calculatedDelay.toMillis();
    return Duration.ofMillis(ThreadLocalRandom.current().nextLong(millis + 1));
  }

  private Optional<Duration> parseRetryAfter(String value) {
    if (value == null) {
      return Optional.empty();
    }
    return retryAfterParser.parse(value);
  }

  private void sleep(D1Operation operation, Duration duration) {
    try {
      sleeper.sleep(duration);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new D1TransportException(operation, e);
    }
  }

  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    T get() throws D1TimeoutException, D1TransportException;
  }
}
