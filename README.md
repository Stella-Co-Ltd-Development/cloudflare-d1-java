# cloudflare-d1-java

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

It is a lightweight SDK for applications that need direct REST API access to D1. It is not a JDBC driver, ORM, SQL builder, migration tool, or Cloudflare Workers binding client.

## Installation

```xml
<dependency>
  <groupId>io.github.xxvw</groupId>
  <artifactId>cloudflare-d1-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

Java 17 or newer is required.

## Quick Start

```java
import io.github.xxvw.cloudflare.d1.D1Client;
import io.github.xxvw.cloudflare.d1.D1Result;

try (D1Client d1 = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .build()) {

  D1Result result = d1.query("SELECT id, name FROM users WHERE id = ?", 1);
  result.rows().forEach(System.out::println);
}
```

You can also create a client directly from environment variables:

```java
D1Client d1 = D1Client.fromEnv();
```

Required environment variables:

```text
CLOUDFLARE_ACCOUNT_ID
D1_DATABASE_ID
CLOUDFLARE_API_TOKEN
```

## Query

```java
D1Result result = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    true
);

List<Map<String, Object>> rows = result.rows();
Optional<Map<String, Object>> first = result.firstRow();
```

Parameters may be `String`, `Number`, `Boolean`, or `null`. Convert dates and JSON values to strings before passing them as SQL parameters.

## Typed Query

```java
record User(long id, String name, String email) {}

List<User> users = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    User.class,
    true
);
```

Typed mapping uses Jackson. Unknown row properties are ignored by the default mapping `ObjectMapper`. A mapping failure in any row throws `D1MappingException`.

You may provide a custom mapper for typed row mapping:

```java
D1Client client = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .objectMapper(customMapper)
    .build();
```

The custom mapper is used only for row-to-type mapping, not for internal D1 request or response parsing.

## queryFirst

```java
Optional<Map<String, Object>> row = d1.queryFirst(
    "SELECT id, name FROM users WHERE id = ?",
    1
);

Optional<User> user = d1.queryFirst(
    "SELECT id, name, email FROM users WHERE id = ?",
    User.class,
    1
);
```

`queryFirst` returns `Optional.empty()` for zero rows and the first row when one or more rows are returned.

## Execute

```java
D1Result result = d1.execute(
    "INSERT INTO users(name, email) VALUES (?, ?)",
    "Taro",
    "taro@example.com"
);

long changes = result.meta().changes();
OptionalLong lastRowId = result.meta().lastRowId();
```

`execute` does not retry by default because write operations may not be idempotent.

## Batch

```java
List<D1Result> results = d1.batch(
    D1Query.of("INSERT INTO users(name) VALUES (?)", "Taro"),
    D1Query.of("SELECT id, name FROM users")
);
```

Use `batch(...)` for multiple operations instead of relying on semicolon-separated SQL statements. Empty batches are rejected.

## Retry Policy

Queries retry by default on `429`, `500`, `502`, `503`, and `504`.

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
```

Custom policy:

```java
D1RetryPolicy retryPolicy = D1RetryPolicy.builder()
    .retryQuery(true)
    .retryExecute(false)
    .retryBatch(false)
    .maxRetries(3)
    .baseDelay(Duration.ofMillis(300))
    .maxDelay(Duration.ofSeconds(5))
    .jitter(true)
    .respectRetryAfter(true)
    .build();
```

Disable retries:

```java
D1RetryPolicy retryPolicy = D1RetryPolicy.none();
```

## Async Preview

The async methods are preview APIs in `0.1.0` and are marked deprecated to avoid treating their signatures as stable:

```java
CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1");
```

Failures complete the future exceptionally.

## Error Handling

```java
try {
  d1.query("SELECT * FROM missing_table");
} catch (D1QueryException e) {
  e.sql().ifPresent(sql -> System.err.println("Query failed"));
} catch (D1RateLimitException e) {
  e.retryAfter().ifPresent(delay -> System.err.println("Retry after: " + delay));
} catch (D1AuthenticationException | D1AuthorizationException e) {
  System.err.println("Check API token permissions");
} catch (D1ApiException e) {
  System.err.println("D1 API error: " + e.statusCode());
} catch (D1TransportException | D1TimeoutException e) {
  System.err.println("Network failure");
}
```

Exception messages do not include API tokens or SQL parameter values. `D1QueryException.sql()` may expose SQL text for diagnostics.

## Security Notes

- Do not hard-code API tokens.
- Use environment variables or a secret management system.
- Do not log `Authorization` headers.
- Do not include API tokens, account IDs, database IDs, or production data in issues, test fixtures, logs, or screenshots.
- Use fake values such as `test-token`, `test-account-id`, and `test-database-id` in tests.

## Limitations

- No JDBC driver support.
- No JPA or ORM integration.
- No SQL builder.
- No migration framework.
- No Spring Boot starter.
- No Cloudflare Workers Binding API support.
- No logging framework integration.
- Runtime dependencies are intentionally limited to Jackson Databind.
