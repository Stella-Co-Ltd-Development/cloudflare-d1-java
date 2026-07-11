package io.github.xxvw.cloudflare.d1.internal;

import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1RetryPolicy;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class D1RetryExecutor {
  private static final int MAX_BACKOFF_EXPONENT = 62;
  private static final Duration MAX_NANOS_DELAY = Duration.ofNanos(Long.MAX_VALUE);

  private final D1RetryPolicy retryPolicy;
  private final D1RetryAfterParser retryAfterParser;
  private final D1Sleeper sleeper;

  public D1RetryExecutor(D1RetryPolicy retryPolicy) {
    this(retryPolicy, new D1RetryAfterParser(), D1RetryExecutor::sleepFor);
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
        Duration delay = retryAfter.get();
        Duration maxRetryAfter = retryPolicy.maxRetryAfter();
        return delay.compareTo(maxRetryAfter) > 0 ? maxRetryAfter : delay;
      }
    }
    Duration calculatedDelay = exponentialDelay(retryAttempt);
    if (!retryPolicy.jitter() || calculatedDelay.isZero()) {
      return calculatedDelay;
    }
    return Duration.ofNanos(ThreadLocalRandom.current().nextLong(jitterBoundNanos(calculatedDelay)));
  }

  private Duration exponentialDelay(int retryAttempt) {
    Duration baseDelay = retryPolicy.baseDelay();
    Duration maxDelay = retryPolicy.maxDelay();
    int exponent = Math.min(Math.max(0, retryAttempt - 1), MAX_BACKOFF_EXPONENT);
    long multiplier = 1L << exponent;
    // Clamp by comparison before multiplying so large attempt counts cannot overflow Duration.
    if (baseDelay.compareTo(maxDelay.dividedBy(multiplier)) > 0) {
      return maxDelay;
    }
    return baseDelay.multipliedBy(multiplier);
  }

  private static long jitterBoundNanos(Duration delay) {
    if (delay.compareTo(MAX_NANOS_DELAY) >= 0) {
      return Long.MAX_VALUE;
    }
    return delay.toNanos() + 1;
  }

  private static void sleepFor(Duration duration) throws InterruptedException {
    Thread.sleep(duration.toMillis(), (int) (duration.getNano() % 1_000_000));
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
