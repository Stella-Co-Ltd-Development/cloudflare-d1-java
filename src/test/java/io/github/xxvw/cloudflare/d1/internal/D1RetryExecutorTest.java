package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1RetryPolicy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class D1RetryExecutorTest {
  private final List<Duration> sleeps = new ArrayList<>();

  @Test
  void exponentialBackoffDoublesEachAttemptAndClampsAtMaxDelay() {
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .maxRetries(3)
        .baseDelay(Duration.ofMillis(100))
        .maxDelay(Duration.ofMillis(250))
        .jitter(false)
        .build();
    D1RetryExecutor executor = executor(policy);

    D1HttpResponse response = executor.execute(D1Operation.QUERY, responses(500, 500, 500, 200));

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(sleeps).containsExactly(
        Duration.ofMillis(100),
        Duration.ofMillis(200),
        Duration.ofMillis(250));
  }

  @Test
  void largeRetryAttemptCountsDoNotOverflowAndStayClampedAtMaxDelay() {
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .maxRetries(80)
        .baseDelay(Duration.ofMillis(200))
        .maxDelay(Duration.ofSeconds(2))
        .jitter(false)
        .build();
    D1RetryExecutor executor = executor(policy);

    D1HttpResponse response = executor.execute(D1Operation.QUERY, alwaysStatus(500));

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(sleeps).hasSize(80);
    assertThat(sleeps).allSatisfy(delay ->
        assertThat(delay).isBetween(Duration.ofMillis(200), Duration.ofSeconds(2)));
    assertThat(sleeps.get(sleeps.size() - 1)).isEqualTo(Duration.ofSeconds(2));
  }

  @Test
  void jitterKeepsDelaysWithinCalculatedBounds() {
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .maxRetries(3)
        .baseDelay(Duration.ofMillis(100))
        .maxDelay(Duration.ofMillis(250))
        .jitter(true)
        .build();
    D1RetryExecutor executor = executor(policy);

    executor.execute(D1Operation.QUERY, responses(500, 500, 500, 200));

    assertThat(sleeps).hasSize(3);
    assertThat(sleeps.get(0)).isBetween(Duration.ZERO, Duration.ofMillis(100));
    assertThat(sleeps.get(1)).isBetween(Duration.ZERO, Duration.ofMillis(200));
    assertThat(sleeps.get(2)).isBetween(Duration.ZERO, Duration.ofMillis(250));
  }

  @Test
  void jitterPreservesSubMillisecondDelays() {
    Duration baseDelay = Duration.ofNanos(500_000);
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .maxRetries(1)
        .baseDelay(baseDelay)
        .maxDelay(Duration.ofMillis(1))
        .jitter(true)
        .build();
    D1RetryExecutor executor = executor(policy);

    for (int i = 0; i < 50; i++) {
      executor.execute(D1Operation.QUERY, responses(503, 200));
    }

    assertThat(sleeps).hasSize(50);
    assertThat(sleeps).allSatisfy(delay -> assertThat(delay).isBetween(Duration.ZERO, baseDelay));
    assertThat(sleeps).anySatisfy(delay -> assertThat(delay).isPositive());
  }

  private D1RetryExecutor executor(D1RetryPolicy policy) {
    return new D1RetryExecutor(policy, new D1RetryAfterParser(), sleeps::add);
  }

  private static D1RetryExecutor.ThrowingSupplier<D1HttpResponse> responses(int... statusCodes) {
    AtomicInteger index = new AtomicInteger();
    return () -> {
      int position = Math.min(index.getAndIncrement(), statusCodes.length - 1);
      return new D1HttpResponse(statusCodes[position], Collections.emptyMap(), "{}");
    };
  }

  private static D1RetryExecutor.ThrowingSupplier<D1HttpResponse> alwaysStatus(int statusCode) {
    return () -> new D1HttpResponse(statusCode, Collections.emptyMap(), "{}");
  }
}
