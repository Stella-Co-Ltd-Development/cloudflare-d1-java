# cloudflare-d1-java

[![CI](https://github.com/Stella-Co-Ltd-Development/cloudflare-d1-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Stella-Co-Ltd-Development/cloudflare-d1-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.xxvw/cloudflare-d1-java.svg)](https://central.sonatype.com/artifact/io.github.xxvw/cloudflare-d1-java)
[![Javadocs](https://javadoc.io/badge2/io.github.xxvw/cloudflare-d1-java/0.1.3/javadoc.svg)](https://javadoc.io/doc/io.github.xxvw/cloudflare-d1-java/0.1.3)
[![License](https://img.shields.io/github/license/Stella-Co-Ltd-Development/cloudflare-d1-java.svg?cacheSeconds=3600)](LICENSE)

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

It is a lightweight SDK for Java applications that need direct REST API access to D1. It is not a JDBC driver, ORM, SQL builder, migration tool, Spring Boot starter, or Cloudflare Workers binding client.

The D1 REST API is a good fit for server-side tools, administration, batch jobs, and migration-style workflows. For latency-sensitive application traffic, also consider a Cloudflare Worker proxy or the Workers D1 binding.

## Why use this client?

- Use typed Java APIs instead of hand-written HTTP requests and response parsing.
- Keep Java 8 compatibility for older tools and deployment environments.
- Run synchronous or asynchronous D1 REST API calls from server-side Java code.
- Map query rows to immutable maps or Jackson-backed model classes.
- Get built-in retry handling for transient read failures and rate limits.
- Avoid extra runtime infrastructure dependencies beyond `jackson-databind`.

Use the Workers D1 binding for code running inside Cloudflare Workers. Use a Worker proxy when application traffic needs lower latency, custom authorization, or tighter control over public access.

## Installation

```xml
<dependency>
  <groupId>io.github.xxvw</groupId>
  <artifactId>cloudflare-d1-java</artifactId>
  <version>0.1.3</version>
</dependency>
```

Gradle:

```groovy
implementation "io.github.xxvw:cloudflare-d1-java:0.1.3"
```

Requirements:

- Java 8 or newer.
- Maven, Gradle, or another build tool that can consume Maven Central artifacts.
- A Cloudflare account with a D1 database.
- A Cloudflare API token with Account, D1, Edit permission for write operations. Use the minimum permission that fits your application.

Runtime dependency:

- `jackson-databind`

## At a Glance

| Area | Support |
|---|---|
| Java | Java 8 or newer |
| Distribution | Maven Central |
| Runtime dependency | `jackson-databind` |
| API style | Synchronous `D1Client` and asynchronous `D1AsyncClient` |
| D1 operations | Query, query first row, execute, batch |
| Mapping | `Map<String, Object>` rows and Jackson-backed typed rows |
| Retries | Enabled for transient read failures by default |
| Non-goals | JDBC, ORM, SQL builder, migrations, Spring Boot starter |

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

For a one-minute local check, run the bundled example:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java
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

The repository includes standalone Maven examples in `examples/quickstart`.

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

Run typed mapping and opt-in write examples:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.MappingAndWriteExamples

mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.MappingAndWriteExamples \
  -Dexec.args="--write"
```

## Supported Operations

| Operation | Method | Default retry behavior |
|---|---|---|
| Query rows | `query(...)` | Retries transient failures |
| Query first row | `queryFirst(...)` | Retries transient failures |
| Execute writes | `execute(...)` | Does not retry by default |
| Batch statements | `batch(...)` | Does not retry by default |
| Typed row mapping | `query(..., User.class)` | Same as query |
| Async operations | `D1AsyncClient` | Same as the underlying operation |

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

## Async API

Use `D1AsyncClient` for supported asynchronous operations:

```java
try (D1AsyncClient d1 = D1AsyncClient.fromEnv()) {
  CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1 AS value");
  D1Result result = future.join();
  System.out.println(result.firstRow());
}
```

Async operations run on a Java `Executor` and complete futures exceptionally with the same public exception types as synchronous operations.

Run the read-only async examples:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.AsyncExamples
```

Run the opt-in write example:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.AsyncExamples \
  -Dexec.args="--write"
```

See [Async API](docs/guides/async.md) for executor configuration and failure handling.

## Finding Cloudflare IDs

- Account ID: Cloudflare dashboard account details, or Wrangler output.
- Database ID: D1 database details in the Cloudflare dashboard, or `wrangler d1 list`.
- API token: Cloudflare dashboard API Tokens page. Store it outside source control.

The default REST API base URL is:

```text
https://api.cloudflare.com/client/v4
```

## Troubleshooting

| Symptom | First check |
|---|---|
| `D1AuthenticationException` | Confirm `CLOUDFLARE_API_TOKEN` is present, current, and passed to the client. |
| `D1AuthorizationException` | Confirm token permissions, account ID, and database ID. |
| `D1QueryException` | Confirm the SQL works in D1 and inspect `errors()`. |
| `D1RateLimitException` | Respect `retryAfter()` and reduce request rate. |
| `D1MappingException` | Confirm selected column names match the target Java model. |
| `D1TimeoutException` | Check network connectivity and request timeout settings. |

See [Troubleshooting](docs/guides/troubleshooting.md) for common setup, API, and mapping failures.

## Documentation

- [Quick Start](docs/guides/quick-start.md)
- [Async API](docs/guides/async.md)
- [Typed Mapping](docs/guides/typed-mapping.md)
- [Retry Policy](docs/guides/retry-policy.md)
- [Error Handling](docs/guides/error-handling.md)
- [Custom Transport](docs/guides/custom-transport.md)
- [Compatibility and Support](docs/guides/compatibility-support.md)
- [Production Usage Notes](docs/guides/production-usage.md)
- [Troubleshooting](docs/guides/troubleshooting.md)
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

## API Stability

The `0.x` series is intended to stay small and practical while the public API is refined. Public classes in `io.github.xxvw.cloudflare.d1` are the supported API surface. Internal classes under `io.github.xxvw.cloudflare.d1.internal` may change without notice.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Good first contributions include documentation improvements, additional tests around edge cases, and small examples that do not add runtime dependencies.
