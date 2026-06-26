package io.github.xxvw.cloudflare.d1;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Retry policy for D1 operations.
 *
 * <p>The default policy retries read-style query operations on common transient HTTP statuses.
 * Raw and object-row query operations retry by default. Execute, batch, and raw batch operations
 * do not retry by default.
 *
 * <pre>{@code
 * D1Client client = D1Client.builder()
 *     .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
 *     .databaseId(System.getenv("D1_DATABASE_ID"))
 *     .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
 *     .retryPolicy(D1RetryPolicy.none())
 *     .build();
 * }</pre>
 */
public final class D1RetryPolicy {
  private final boolean retryQuery;
  private final boolean retryExecute;
  private final boolean retryBatch;
  private final boolean retryRaw;
  private final boolean retryRawBatch;
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
    this.retryRaw = builder.retryRaw;
    this.retryRawBatch = builder.retryRawBatch;
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

  /**
   * Returns the default retry policy.
   *
   * @return default retry policy
   */
  public static D1RetryPolicy defaultPolicy() {
    return builder().build();
  }

  /**
   * Returns a policy with retries disabled.
   *
   * @return no-retry policy
   */
  public static D1RetryPolicy none() {
    return builder()
        .retryQuery(false)
        .retryExecute(false)
        .retryBatch(false)
        .retryRaw(false)
        .retryRawBatch(false)
        .maxRetries(0)
        .build();
  }

  /**
   * Creates a retry policy builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Whether query operations may be retried.
   *
   * @return true when query retries are enabled
   */
  public boolean retryQuery() {
    return retryQuery;
  }

  /**
   * Whether execute operations may be retried.
   *
   * @return true when execute retries are enabled
   */
  public boolean retryExecute() {
    return retryExecute;
  }

  /**
   * Whether batch operations may be retried.
   *
   * @return true when batch retries are enabled
   */
  public boolean retryBatch() {
    return retryBatch;
  }

  /**
   * Whether raw query operations may be retried.
   *
   * @return true when raw query retries are enabled
   */
  public boolean retryRaw() {
    return retryRaw;
  }

  /**
   * Whether raw batch operations may be retried.
   *
   * @return true when raw batch retries are enabled
   */
  public boolean retryRawBatch() {
    return retryRawBatch;
  }

  /**
   * Maximum number of retry attempts after the initial request.
   *
   * @return maximum retry count
   */
  public int maxRetries() {
    return maxRetries;
  }

  /**
   * Base delay used for exponential backoff.
   *
   * @return base delay
   */
  public Duration baseDelay() {
    return baseDelay;
  }

  /**
   * Maximum delay used for exponential backoff.
   *
   * @return maximum delay
   */
  public Duration maxDelay() {
    return maxDelay;
  }

  /**
   * Whether full jitter is applied to calculated backoff delays.
   *
   * @return true when jitter is enabled
   */
  public boolean jitter() {
    return jitter;
  }

  /**
   * Whether valid Retry-After response headers take priority over backoff delays.
   *
   * @return true when Retry-After is respected
   */
  public boolean respectRetryAfter() {
    return respectRetryAfter;
  }

  /**
   * HTTP status codes eligible for retry.
   *
   * @return immutable status code set
   */
  public Set<Integer> retryStatusCodes() {
    return retryStatusCodes;
  }

  /**
   * Returns whether the operation category may retry under this policy.
   *
   * @param operation operation category
   * @return true when retries are enabled for the operation
   */
  public boolean retries(D1Operation operation) {
    switch (operation) {
      case QUERY:
        return retryQuery;
      case EXECUTE:
        return retryExecute;
      case BATCH:
        return retryBatch;
      case RAW:
        return retryRaw;
      case RAW_BATCH:
        return retryRawBatch;
      default:
        return false;
    }
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
    return Collections.unmodifiableSet(copy);
  }

  /**
   * Builder for {@link D1RetryPolicy}.
   */
  public static final class Builder {
    private boolean retryQuery = true;
    private boolean retryExecute;
    private boolean retryBatch;
    private boolean retryRaw = true;
    private boolean retryRawBatch;
    private int maxRetries = 2;
    private Duration baseDelay = Duration.ofMillis(200);
    private Duration maxDelay = Duration.ofSeconds(2);
    private boolean jitter = true;
    private boolean respectRetryAfter = true;
    private Set<Integer> retryStatusCodes =
        new LinkedHashSet<>(Arrays.asList(429, 500, 502, 503, 504));

    private Builder() {}

    /**
     * Enables or disables query retries.
     *
     * @param retryQuery whether query retries are enabled
     * @return this builder
     */
    public Builder retryQuery(boolean retryQuery) {
      this.retryQuery = retryQuery;
      return this;
    }

    /**
     * Enables or disables execute retries.
     *
     * @param retryExecute whether execute retries are enabled
     * @return this builder
     */
    public Builder retryExecute(boolean retryExecute) {
      this.retryExecute = retryExecute;
      return this;
    }

    /**
     * Enables or disables batch retries.
     *
     * @param retryBatch whether batch retries are enabled
     * @return this builder
     */
    public Builder retryBatch(boolean retryBatch) {
      this.retryBatch = retryBatch;
      return this;
    }

    /**
     * Enables or disables raw query retries.
     *
     * @param retryRaw whether raw query retries are enabled
     * @return this builder
     */
    public Builder retryRaw(boolean retryRaw) {
      this.retryRaw = retryRaw;
      return this;
    }

    /**
     * Enables or disables raw batch retries.
     *
     * @param retryRawBatch whether raw batch retries are enabled
     * @return this builder
     */
    public Builder retryRawBatch(boolean retryRawBatch) {
      this.retryRawBatch = retryRawBatch;
      return this;
    }

    /**
     * Sets the maximum retry count.
     *
     * @param maxRetries non-negative retry count
     * @return this builder
     */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    /**
     * Sets the base backoff delay.
     *
     * @param baseDelay non-negative base delay
     * @return this builder
     */
    public Builder baseDelay(Duration baseDelay) {
      this.baseDelay = baseDelay;
      return this;
    }

    /**
     * Sets the maximum backoff delay.
     *
     * @param maxDelay non-negative maximum delay
     * @return this builder
     */
    public Builder maxDelay(Duration maxDelay) {
      this.maxDelay = maxDelay;
      return this;
    }

    /**
     * Enables or disables full jitter.
     *
     * @param jitter whether jitter is enabled
     * @return this builder
     */
    public Builder jitter(boolean jitter) {
      this.jitter = jitter;
      return this;
    }

    /**
     * Enables or disables Retry-After handling.
     *
     * @param respectRetryAfter whether Retry-After is respected
     * @return this builder
     */
    public Builder respectRetryAfter(boolean respectRetryAfter) {
      this.respectRetryAfter = respectRetryAfter;
      return this;
    }

    /**
     * Sets retryable HTTP status codes.
     *
     * @param retryStatusCodes non-empty set of status codes from 100 through 599
     * @return this builder
     */
    public Builder retryStatusCodes(Set<Integer> retryStatusCodes) {
      this.retryStatusCodes = retryStatusCodes;
      return this;
    }

    /**
     * Builds the retry policy.
     *
     * @return retry policy
     */
    public D1RetryPolicy build() {
      return new D1RetryPolicy(this);
    }
  }
}
