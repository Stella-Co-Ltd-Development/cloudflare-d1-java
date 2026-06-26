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
    .build();
```

Enable retries for writes only when the statement is safe to repeat or the application has its own idempotency guarantees.

## Rate Limits

When D1 returns `429`, `D1RateLimitException.retryAfter()` exposes the parsed `Retry-After` header when present. The retry executor also respects that header when the retry policy allows it.
