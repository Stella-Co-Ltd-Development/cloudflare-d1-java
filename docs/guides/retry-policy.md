# Retry Policy

`query(...)` and `raw(...)` retry transient HTTP failures by default. `execute(...)`, `batch(...)`, and `rawBatch(...)` do not retry by default because write operations and batches may not be idempotent.

## Defaults

```text
retryQuery = true
retryExecute = false
retryBatch = false
retryRaw = true
retryRawBatch = false
maxRetries = 2
baseDelay = 200ms
maxDelay = 2s
jitter = true
respectRetryAfter = true
maxRetryAfter = 30s
retryNetworkErrors = true
retryStatusCodes = 429, 500, 502, 503, 504
```

## Disable Retries

```java
D1Client client = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .retryPolicy(D1RetryPolicy.none())
    .build();
```

## Custom Policy

```java
D1RetryPolicy retryPolicy = D1RetryPolicy.builder()
    .retryQuery(true)
    .retryExecute(false)
    .retryBatch(false)
    .retryRaw(true)
    .retryRawBatch(false)
    .maxRetries(3)
    .baseDelay(Duration.ofMillis(300))
    .maxDelay(Duration.ofSeconds(5))
    .jitter(true)
    .respectRetryAfter(true)
    .maxRetryAfter(Duration.ofSeconds(30))
    .retryNetworkErrors(true)
    .build();
```

Enable retries for writes only when the statement is safe to repeat or the application has its own idempotency guarantees.

## Network Failures

Transient network failures (connection failures and timeouts) are retried with exponential backoff for operations whose retries are enabled, mirroring the handling of retryable HTTP statuses. Because a request may have reached the server before the connection failed, retried operations have at-least-once semantics. Disable this behavior with `retryNetworkErrors(false)`.

## Rate Limits

When D1 returns `429`, `D1RateLimitException.retryAfter()` exposes the parsed `Retry-After` header when present. The retry executor also respects that header when the retry policy allows it, but never sleeps longer than `maxRetryAfter` even when the server requests a longer delay.
