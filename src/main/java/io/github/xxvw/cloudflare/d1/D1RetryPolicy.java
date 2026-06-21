package io.github.xxvw.cloudflare.d1;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Retry policy for D1 operations.
 */
public final class D1RetryPolicy {
  private final boolean retryQuery;
  private final boolean retryExecute;
  private final boolean retryBatch;
  private final int maxRetries;
  private final Duration baseDelay;
  private final Duration maxDelay;
  private final boolean jitter;
  private final boolean respectRetryAfter;
  private final Set<Integer> retryStatusCodes;

  private D1RetryPolicy(Builder builder) {
    this.retryQuery = builder.retryQuery;
    this.retryExecute = builder.retryExecute;
    this.retryBatch = builder.retryBatch;
    this.maxRetries = builder.maxRetries;
    this.baseDelay = requireNonNegative(builder.baseDelay, "baseDelay");
    this.maxDelay = requireNonNegative(builder.maxDelay, "maxDelay");
    if (maxDelay.compareTo(baseDelay) < 0) {
      throw new IllegalArgumentException("maxDelay must be greater than or equal to baseDelay");
    }
    this.jitter = builder.jitter;
    this.respectRetryAfter = builder.respectRetryAfter;
    if (maxRetries < 0) {
      throw new IllegalArgumentException("maxRetries must be greater than or equal to 0");
    }
    this.retryStatusCodes = validateStatusCodes(builder.retryStatusCodes);
  }

  public static D1RetryPolicy defaultPolicy() {
    return builder().build();
  }

  public static D1RetryPolicy none() {
    return builder()
        .retryQuery(false)
        .retryExecute(false)
        .retryBatch(false)
        .maxRetries(0)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean retryQuery() {
    return retryQuery;
  }

  public boolean retryExecute() {
    return retryExecute;
  }

  public boolean retryBatch() {
    return retryBatch;
  }

  public int maxRetries() {
    return maxRetries;
  }

  public Duration baseDelay() {
    return baseDelay;
  }

  public Duration maxDelay() {
    return maxDelay;
  }

  public boolean jitter() {
    return jitter;
  }

  public boolean respectRetryAfter() {
    return respectRetryAfter;
  }

  public Set<Integer> retryStatusCodes() {
    return retryStatusCodes;
  }

  public boolean retries(D1Operation operation) {
    return switch (operation) {
      case QUERY -> retryQuery;
      case EXECUTE -> retryExecute;
      case BATCH -> retryBatch;
    };
  }

  private static Duration requireNonNegative(Duration duration, String name) {
    Objects.requireNonNull(duration, name + " must not be null");
    if (duration.isNegative()) {
      throw new IllegalArgumentException(name + " must not be negative");
    }
    return duration;
  }

  private static Set<Integer> validateStatusCodes(Set<Integer> statusCodes) {
    Objects.requireNonNull(statusCodes, "retryStatusCodes must not be null");
    if (statusCodes.isEmpty()) {
      throw new IllegalArgumentException("retryStatusCodes must not be empty");
    }
    LinkedHashSet<Integer> copy = new LinkedHashSet<>();
    for (Integer statusCode : statusCodes) {
      if (statusCode == null || statusCode < 100 || statusCode > 599) {
        throw new IllegalArgumentException("retry status codes must be between 100 and 599");
      }
      copy.add(statusCode);
    }
    return Set.copyOf(copy);
  }

  public static final class Builder {
    private boolean retryQuery = true;
    private boolean retryExecute;
    private boolean retryBatch;
    private int maxRetries = 2;
    private Duration baseDelay = Duration.ofMillis(200);
    private Duration maxDelay = Duration.ofSeconds(2);
    private boolean jitter = true;
    private boolean respectRetryAfter = true;
    private Set<Integer> retryStatusCodes = Set.of(429, 500, 502, 503, 504);

    private Builder() {}

    public Builder retryQuery(boolean retryQuery) {
      this.retryQuery = retryQuery;
      return this;
    }

    public Builder retryExecute(boolean retryExecute) {
      this.retryExecute = retryExecute;
      return this;
    }

    public Builder retryBatch(boolean retryBatch) {
      this.retryBatch = retryBatch;
      return this;
    }

    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public Builder baseDelay(Duration baseDelay) {
      this.baseDelay = baseDelay;
      return this;
    }

    public Builder maxDelay(Duration maxDelay) {
      this.maxDelay = maxDelay;
      return this;
    }

    public Builder jitter(boolean jitter) {
      this.jitter = jitter;
      return this;
    }

    public Builder respectRetryAfter(boolean respectRetryAfter) {
      this.respectRetryAfter = respectRetryAfter;
      return this;
    }

    public Builder retryStatusCodes(Set<Integer> retryStatusCodes) {
      this.retryStatusCodes = retryStatusCodes;
      return this;
    }

    public D1RetryPolicy build() {
      return new D1RetryPolicy(this);
    }
  }
}
