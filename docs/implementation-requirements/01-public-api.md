# 01. Public API Requirements

## D1Client

`D1Client` is the main entry point.

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

Environment variables:

```text
CLOUDFLARE_ACCOUNT_ID
D1_DATABASE_ID
CLOUDFLARE_API_TOKEN
```

If an environment variable is missing or blank, `fromEnv()` must throw `IllegalStateException`.

## D1ClientBuilder

Required builder methods:

```java
public final class D1ClientBuilder {
    public D1ClientBuilder accountId(String accountId);
    public D1ClientBuilder databaseId(String databaseId);
    public D1ClientBuilder apiToken(String apiToken);

    public D1ClientBuilder baseUrl(String baseUrl);
    public D1ClientBuilder baseUrl(URI baseUrl);

    public D1ClientBuilder userAgent(String userAgent);

    public D1ClientBuilder connectTimeout(Duration connectTimeout);
    public D1ClientBuilder requestTimeout(Duration requestTimeout);

    public D1ClientBuilder retryPolicy(D1RetryPolicy retryPolicy);
    public D1ClientBuilder transport(D1Transport transport);
    public D1ClientBuilder objectMapper(ObjectMapper objectMapper);

    public D1Client build();
}
```

Required values:

- `accountId`
- `databaseId`
- `apiToken`

Blank and null values must be rejected.

Default values:

```text
baseUrl = https://api.cloudflare.com/client/v4
connectTimeout = 5 seconds
requestTimeout = 30 seconds
retryPolicy = D1RetryPolicy.defaultPolicy()
userAgent = cloudflare-d1-java/{version}
```

## D1Query

```java
public final class D1Query {
    public static D1Query of(String sql);
    public static D1Query of(String sql, Object... params);
    public static D1Query of(String sql, List<?> params);

    public String sql();
    public List<Object> params();
}
```

`D1Query` must be immutable.

Validation rules are defined in `04-retry-and-validation.md`.

## Query API

```java
public D1Result query(String sql);
public D1Result query(String sql, Object... params);
public D1Result query(String sql, List<?> params);
public D1Result query(D1Query query);
```

`query()` uses the retry policy by default.

## Typed Query API

```java
public <T> List<T> query(String sql, Class<T> type);
public <T> List<T> query(String sql, Class<T> type, Object... params);
public <T> List<T> query(String sql, List<?> params, Class<T> type);
public <T> List<T> query(D1Query query, Class<T> type);
```

Rules:

- Uses mapping ObjectMapper only.
- Unknown row properties are ignored by default.
- A mapping failure in any row fails the entire typed query with `D1MappingException`.

## Query First API

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
- More than 1 row: first row only

## Execute API

```java
public D1Result execute(String sql);
public D1Result execute(String sql, Object... params);
public D1Result execute(String sql, List<?> params);
public D1Result execute(D1Query query);
```

`execute()` returns `D1Result`.

`execute()` does not retry by default.

## Batch API

```java
public List<D1Result> batch(List<D1Query> queries);
public List<D1Result> batch(D1Query... queries);
```

Rules:

- Empty batch must throw `IllegalArgumentException`.
- Result list must be immutable.
- If any batch item fails, throw `D1BatchException`.

## Preview Async API

Async APIs are available in v0.1.0 but are preview APIs and must be annotated as deprecated.

```java
@Deprecated(since = "0.1.0", forRemoval = false)
public CompletableFuture<D1Result> queryAsync(String sql, Object... params);

@Deprecated(since = "0.1.0", forRemoval = false)
public CompletableFuture<D1Result> executeAsync(String sql, Object... params);

@Deprecated(since = "0.1.0", forRemoval = false)
public CompletableFuture<List<D1Result>> batchAsync(List<D1Query> queries);
```

Async failures must complete the future exceptionally.
