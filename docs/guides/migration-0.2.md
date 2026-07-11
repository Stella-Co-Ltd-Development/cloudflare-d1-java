# Migrating from 0.1.x to 0.2.0

Version 0.2.0 contains a small set of breaking changes focused on safer defaults. Most applications only need to recompile; review the items below before upgrading.

## Removed: preview async methods on `D1Client`

`D1Client.queryAsync`, `D1Client.executeAsync`, and `D1Client.batchAsync` (deprecated since 0.1.0) were removed.

Use `D1AsyncClient` instead:

```java
try (D1AsyncClient d1 = D1AsyncClient.fromEnv()) {
  CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1 AS value");
}
```

## Changed: default async executor

`D1AsyncClientBuilder` no longer defaults to `ForkJoinPool.commonPool()`. When no executor is supplied, the client creates its own executor backed by named daemon threads (`cloudflare-d1-async-*`) and shuts it down on `close()`.

- No action is needed for most applications; call `close()` when done (as before).
- Caller-supplied executors are never shut down, unchanged from 0.1.x.
- If you relied on async work running on the common pool, pass an executor explicitly.

## Changed: `D1Client.close()` closes the transport

`D1Transport` gained a no-op `default void close()`, and `D1Client.close()` now invokes it exactly once. Lambda transports keep working unchanged.

- If a custom transport holds resources (connection pools, executors), override `close()` to release them.
- If one transport instance is shared across multiple clients, manage its lifecycle externally and keep `close()` a no-op.

## Changed: transient network failures are retried by default

Transport and timeout failures are now retried with exponential backoff for operations whose retries are enabled (`query`/`raw` by default). Retried operations have at-least-once semantics.

- Keep write statements out of `query(...)` and `raw(...)`; run writes through `execute(...)`/`batch(...)`, which do not retry by default.
- Opt out with `D1RetryPolicy.builder().retryNetworkErrors(false)`.

## Changed: `Retry-After` is capped

Server-provided `Retry-After` delays are capped by the new `maxRetryAfter` setting (default 30 seconds). `D1RateLimitException.retryAfter()` still exposes the raw server value. Raise or lower the cap with `D1RetryPolicy.builder().maxRetryAfter(...)`.

## Deprecated: typed overloads with the type last

`query(String, List, Class)`, `queryFirst(String, List, Class)`, and the matching async overloads are deprecated and will be removed in 0.3.0. Switch to the new overloads that match the varargs parameter order:

```java
// Before
List<User> users = d1.query("SELECT * FROM users WHERE active = ?", params, User.class);

// After
List<User> users = d1.query("SELECT * FROM users WHERE active = ?", User.class, params);
```
