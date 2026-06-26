# cloudflare-d1-java

[![CI](https://github.com/Stella-Co-Ltd-Development/cloudflare-d1-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Stella-Co-Ltd-Development/cloudflare-d1-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.xxvw/cloudflare-d1-java.svg)](https://central.sonatype.com/artifact/io.github.xxvw/cloudflare-d1-java)
[![Javadocs](https://javadoc.io/badge2/io.github.xxvw/cloudflare-d1-java/0.1.2/javadoc.svg)](https://javadoc.io/doc/io.github.xxvw/cloudflare-d1-java/0.1.2)
[![License](https://img.shields.io/github/license/Stella-Co-Ltd-Development/cloudflare-d1-java.svg?cacheSeconds=3600)](LICENSE)

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

It is a lightweight SDK for Java applications that need direct REST API access to D1. It is not a JDBC driver, ORM, SQL builder, migration tool, Spring Boot starter, or Cloudflare Workers binding client.

The D1 REST API is a good fit for server-side tools, administration, batch jobs, and migration-style workflows. For latency-sensitive application traffic, also consider a Cloudflare Worker proxy or the Workers D1 binding.

## Installation

```xml
<dependency>
  <groupId>io.github.xxvw</groupId>
  <artifactId>cloudflare-d1-java</artifactId>
  <version>0.1.2</version>
</dependency>
```

Gradle:

```groovy
implementation "io.github.xxvw:cloudflare-d1-java:0.1.2"
```

Requirements:

- Java 8 or newer.
- Maven, Gradle, or another build tool that can consume Maven Central artifacts.
- A Cloudflare account with a D1 database.
- A Cloudflare API token with Account, D1, Edit permission for write operations. Use the minimum permission that fits your application.

Runtime dependency:

- `jackson-databind`

## Quick Start

Set credentials with environment variables:

```bash
export CLOUDFLARE_ACCOUNT_ID="your-account-id"
export D1_DATABASE_ID="your-d1-database-id"
export CLOUDFLARE_API_TOKEN="your-api-token"
```

Use the client:

```java
import io.github.xxvw.cloudflare.d1.D1Client;
import io.github.xxvw.cloudflare.d1.D1Result;

try (D1Client d1 = D1Client.fromEnv()) {
  D1Result result = d1.query("SELECT id, name FROM users WHERE id = ?", 1);
  result.rows().forEach(System.out::println);
}
```

You can also configure values explicitly:

```java
D1Client d1 = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .build();
```

See [Quick Start](docs/guides/quick-start.md) for a complete copy-paste friendly example.

## Runnable Example

The repository includes a standalone Maven example in `examples/quickstart`.

```bash
export CLOUDFLARE_ACCOUNT_ID="your-account-id"
export D1_DATABASE_ID="your-d1-database-id"
export CLOUDFLARE_API_TOKEN="your-api-token"

mvn -f examples/quickstart/pom.xml compile exec:java
```

Expected output for the default query:

```text
SQL: SELECT 1 AS value
Rows: 1
{value=1}
```

Pass a SQL statement as `exec.args` to try another read query:

```bash
mvn -f examples/quickstart/pom.xml exec:java -Dexec.args="SELECT 42 AS answer"
```

## Supported Operations

| Operation | Method | Default retry behavior |
|---|---|---|
| Query rows | `query(...)` | Retries transient failures |
| Query first row | `queryFirst(...)` | Retries transient failures |
| Execute writes | `execute(...)` | Does not retry by default |
| Batch statements | `batch(...)` | Does not retry by default |
| Typed row mapping | `query(..., User.class)` | Same as query |

Parameters may be `String`, `Number`, `Boolean`, or `null`. Convert dates, JSON values, and custom objects to strings before passing them as SQL parameters.

## Query

```java
D1Result result = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    true
);

List<Map<String, Object>> rows = result.rows();
Optional<Map<String, Object>> first = result.firstRow();
```

## Typed Query

```java
public class User {
  public long id;
  public String name;
  public String email;
}

List<User> users = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    User.class,
    true
);
```

Typed mapping uses Jackson. Unknown row properties are ignored by the default mapping `ObjectMapper`. A mapping failure in any row throws `D1MappingException`.

See [Typed Mapping](docs/guides/typed-mapping.md) for custom mapper guidance.

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

See [Retry Policy](docs/guides/retry-policy.md) for retry tradeoffs.

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

See [Error Handling](docs/guides/error-handling.md) for the exception hierarchy.

## Custom Transport

The default transport uses the Java standard library and does not add runtime dependencies. You may provide a custom transport:

```java
import java.util.Collections;

D1Client client = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .transport(request -> {
      return new D1TransportResponse(200, Collections.emptyMap(), "{\"success\":true,\"result\":[]}");
    })
    .build();
```

See [Custom Transport](docs/guides/custom-transport.md) for implementation notes.

## Async Preview

The async methods are preview APIs and are marked deprecated to avoid treating their signatures as stable:

```java
CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1");
```

Failures complete the future exceptionally.

## Finding Cloudflare IDs

- Account ID: Cloudflare dashboard account details, or Wrangler output.
- Database ID: D1 database details in the Cloudflare dashboard, or `wrangler d1 list`.
- API token: Cloudflare dashboard API Tokens page. Store it outside source control.

The default REST API base URL is:

```text
https://api.cloudflare.com/client/v4
```

## Troubleshooting

| Symptom | Check |
|---|---|
| `D1AuthenticationException` | The API token is missing, expired, malformed, or not being passed to the client. |
| `D1AuthorizationException` | The token does not have permission for the account or database. |
| `D1QueryException` | SQL failed at D1. Inspect `errors()` and, when appropriate, `sql()`. |
| `D1RateLimitException` | Respect `retryAfter()` and reduce request rate. |
| `D1TimeoutException` | Check network connectivity and consider increasing request timeout. |
| Empty result rows | Confirm the SQL query, target database ID, and whether the statement returns rows. |

## Documentation

- [Quick Start](docs/guides/quick-start.md)
- [Typed Mapping](docs/guides/typed-mapping.md)
- [Retry Policy](docs/guides/retry-policy.md)
- [Error Handling](docs/guides/error-handling.md)
- [Custom Transport](docs/guides/custom-transport.md)
- [Release Readiness](docs/release/release-readiness.md)
- [Implementation Requirements](docs/implementation-requirements/README.md)
- Cloudflare D1 REST API import tutorial: <https://developers.cloudflare.com/d1/tutorials/import-to-d1-with-rest-api/>
- Cloudflare Worker proxy tutorial: <https://developers.cloudflare.com/d1/tutorials/build-an-api-to-access-d1/>

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

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Good first contributions include documentation improvements, additional tests around edge cases, and small examples that do not add runtime dependencies.
