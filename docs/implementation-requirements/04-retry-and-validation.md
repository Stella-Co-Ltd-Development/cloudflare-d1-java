# 04. Retry and Validation Requirements

## D1Query Validation

Rules:

- SQL must not be null.
- SQL must not be blank.
- SQL UTF-8 byte length must not exceed 100 KB.
- Params list must not be null.
- Params count must not exceed 100.
- Param values may be:
  - `String`
  - `Number`
  - `Boolean`
  - `null`
- Other parameter types must throw `IllegalArgumentException`.

Examples of allowed params:

```java
D1Query.of("SELECT * FROM users WHERE id = ?", 1);
D1Query.of("SELECT * FROM users WHERE email = ?", "taro@example.com");
D1Query.of("SELECT * FROM users WHERE active = ?", true);
D1Query.of("SELECT * FROM users WHERE deleted_at IS ?", (Object) null);
```

Examples of disallowed params:

```java
D1Query.of("INSERT INTO events(created_at) VALUES (?)", LocalDateTime.now());
D1Query.of("INSERT INTO users(profile) VALUES (?)", Map.of("name", "Taro"));
D1Query.of("INSERT INTO tags(values) VALUES (?)", List.of("a", "b"));
```

Users should convert dates and JSON values to strings before passing them as params.

## Cloudflare REST API Params Note

Cloudflare D1 REST API documentation describes params as an array of strings.

This library accepts `String`, `Number`, `Boolean`, and `null` for developer convenience.

If strict REST API compatibility is required, users should pass string values explicitly.

## Batch Validation

Rules:

- Empty batch must throw `IllegalArgumentException`.
- No independent batch size limit is enforced in v0.1.0.
- Every `D1Query` in a batch must pass normal `D1Query` validation.

## Retry Policy

Default policy:

```text
retryQuery = true
retryExecute = false
retryBatch = false
maxRetries = 2
baseDelay = 200ms
maxDelay = 2s
jitter = true
respectRetryAfter = true
retryStatusCodes = [429, 500, 502, 503, 504]
```

`query()` retries by default.

`execute()` and `batch()` do not retry by default because write operations may not be idempotent.

## D1RetryPolicy API

```java
public final class D1RetryPolicy {
    public static D1RetryPolicy defaultPolicy();
    public static D1RetryPolicy none();
    public static Builder builder();

    public boolean retryQuery();
    public boolean retryExecute();
    public boolean retryBatch();

    public int maxRetries();

    public Duration baseDelay();
    public Duration maxDelay();

    public boolean jitter();
    public Set<Integer> retryStatusCodes();
    public boolean respectRetryAfter();
}
```

## Retry Policy Validation

Rules:

- `maxRetries >= 0`
- `baseDelay` must not be null
- `maxDelay` must not be null
- `baseDelay >= 0`
- `maxDelay >= 0`
- `maxDelay >= baseDelay`
- `retryStatusCodes` must not be null
- `retryStatusCodes` must not be empty
- each status code must be between 100 and 599

## Retry-After

Support:

```text
Retry-After: 3
Retry-After: Wed, 21 Oct 2015 07:28:00 GMT
```

Rules:

- Seconds format returns `Duration.ofSeconds(n)`.
- HTTP-date format returns the difference from now.
- Past HTTP-date returns `Duration.ZERO`.
- Invalid values are ignored.
- Valid `Retry-After` takes priority over `maxDelay`.

## Backoff

Formula:

```text
calculatedDelay = min(maxDelay, baseDelay * 2^(attempt - 1))
```

Full jitter:

```text
actualDelay = random duration between 0 and calculatedDelay
```

When jitter is disabled:

```text
actualDelay = calculatedDelay
```
