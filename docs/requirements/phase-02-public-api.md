# Phase 02: Public API Requirements

## Main Client

```java
public final class D1Client implements AutoCloseable
```

## Client Creation

```java
D1Client client = D1Client.builder()
    .accountId("account-id")
    .databaseId("database-id")
    .apiToken("api-token")
    .build();
```

Environment-based creation:

```java
D1Client client = D1Client.fromEnv();
```

Required environment variables:

```text
CLOUDFLARE_ACCOUNT_ID
D1_DATABASE_ID
CLOUDFLARE_API_TOKEN
```

## Query API

```java
public D1Result query(String sql);
public D1Result query(String sql, Object... params);
public D1Result query(String sql, List<?> params);
public D1Result query(D1Query query);
```

## Typed Query API

```java
public <T> List<T> query(String sql, Class<T> type);
public <T> List<T> query(String sql, Class<T> type, Object... params);
public <T> List<T> query(String sql, List<?> params, Class<T> type);
public <T> List<T> query(D1Query query, Class<T> type);
```

## queryFirst API

```java
public Optional<Map<String, Object>> queryFirst(String sql);
public Optional<Map<String, Object>> queryFirst(String sql, Object... params);
public Optional<Map<String, Object>> queryFirst(String sql, List<?> params);
public Optional<Map<String, Object>> queryFirst(D1Query query);

public <T> Optional<T> queryFirst(String sql, Class<T> type);
public <T> Optional<T> queryFirst(String sql, Class<T> type, Object... params);
public <T> Optional<T> queryFirst(String sql, List<?> params, Class<T> type);
public <T> Optional<T> queryFirst(D1Query query, Class<T> type);
```

Behavior:

- 0 rows: `Optional.empty()`
- 1 row: first row
- Multiple rows: first row only

## Execute API

```java
public D1Result execute(String sql);
public D1Result execute(String sql, Object... params);
public D1Result execute(String sql, List<?> params);
public D1Result execute(D1Query query);
```

## Batch API

```java
public List<D1Result> batch(List<D1Query> queries);
public List<D1Result> batch(D1Query... queries);
```

## Preview Async API

```java
@Deprecated(since = "0.1.0", forRemoval = false)
public CompletableFuture<D1Result> queryAsync(String sql, Object... params);

@Deprecated(since = "0.1.0", forRemoval = false)
public CompletableFuture<D1Result> executeAsync(String sql, Object... params);

@Deprecated(since = "0.1.0", forRemoval = false)
public CompletableFuture<List<D1Result>> batchAsync(List<D1Query> queries);
```
