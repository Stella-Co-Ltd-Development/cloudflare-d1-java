package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

class D1RetryPolicyTest {

  @Test
  void defaultPolicyMatchesRequirements() {
    D1RetryPolicy policy = D1RetryPolicy.defaultPolicy();

    assertThat(policy.retryQuery()).isTrue();
    assertThat(policy.retryExecute()).isFalse();
    assertThat(policy.retryBatch()).isFalse();
    assertThat(policy.maxRetries()).isEqualTo(2);
    assertThat(policy.baseDelay()).isEqualTo(Duration.ofMillis(200));
    assertThat(policy.maxDelay()).isEqualTo(Duration.ofSeconds(2));
    assertThat(policy.jitter()).isTrue();
    assertThat(policy.respectRetryAfter()).isTrue();
    assertThat(policy.retryStatusCodes()).containsExactlyInAnyOrder(429, 500, 502, 503, 504);
  }

  @Test
  void noneDisablesRetries() {
    D1RetryPolicy policy = D1RetryPolicy.none();

    assertThat(policy.retryQuery()).isFalse();
    assertThat(policy.retryExecute()).isFalse();
    assertThat(policy.retryBatch()).isFalse();
    assertThat(policy.maxRetries()).isZero();
  }

  @Test
  void acceptsZeroRetriesAndZeroDurations() {
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .maxRetries(0)
        .baseDelay(Duration.ZERO)
        .maxDelay(Duration.ZERO)
        .build();

    assertThat(policy.maxRetries()).isZero();
    assertThat(policy.baseDelay()).isEqualTo(Duration.ZERO);
  }

  @Test
  void validatesInvalidPolicyValues() {
    assertThatThrownBy(() -> D1RetryPolicy.builder().maxRetries(-1).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().baseDelay(null).build())
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().maxDelay(null).build())
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().baseDelay(Duration.ofMillis(-1)).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder()
        .baseDelay(Duration.ofSeconds(2))
        .maxDelay(Duration.ofSeconds(1))
        .build()).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().retryStatusCodes(null).build())
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().retryStatusCodes(Set.of()).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().retryStatusCodes(Set.of(99)).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1RetryPolicy.builder().retryStatusCodes(Set.of(600)).build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void retryStatusCodesAreImmutable() {
    D1RetryPolicy policy = D1RetryPolicy.builder().retryStatusCodes(Set.of(503)).build();

    assertThatThrownBy(() -> policy.retryStatusCodes().add(500))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
