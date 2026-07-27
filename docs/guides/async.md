# Async API

`D1AsyncClient` is the supported asynchronous entry point for `cloudflare-d1-java`.

The async client uses the same REST API behavior, retry policy, response parsing, typed mapping, and exception types as `D1Client`. Operations run on a Java `Executor` and return `CompletableFuture` values. It does not add a separate non-blocking HTTP transport.

## Create a Client

```java
try (D1AsyncClient d1 = D1AsyncClient.fromEnv()) {
  CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1 AS value");
  D1Result result = future.join();
  System.out.println(result.firstRow());
}
```

## Configure an Executor

By default, the client creates its own executor backed by named daemon threads (`cloudflare-d1-async-*`) and shuts it down when the client closes. Provide an executor when your application needs explicit concurrency control.

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

try (D1AsyncClient d1 = D1AsyncClient.builderFromEnv()
    .executor(executor)
    .build()) {
  CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1 AS value");
  D1Result result = future.join();
  System.out.println(result.firstRow());
} finally {
  executor.shutdown();
}
```

`D1AsyncClient.fromEnv()` is equivalent to `D1AsyncClient.builderFromEnv().build()`. The
environment-backed builder also accepts timeout, retry policy, custom transport, and typed mapping
configuration before `build()`.

A caller-supplied executor is owned by your application. Closing `D1AsyncClient` never shuts it down; only the client-created default executor is shut down on close.

## Close a Client

`close()` rejects new operations immediately and returns without waiting for work that was already
accepted. Accepted queued and in-flight operations are allowed to finish. The underlying transport
is closed exactly once after the final accepted operation completes.

Calls made after close return a failed `CompletableFuture` whose cause is an
`IllegalStateException` with the message `D1AsyncClient is closed`. If deferred transport cleanup
fails, that failure completes the final accepted future exceptionally. When the operation itself
also fails, the cleanup failure is attached to the operation failure as a suppressed exception.

## Run Parallel Queries

```java
CompletableFuture<D1Result> users = d1.queryAsync("SELECT COUNT(*) AS count FROM users");
CompletableFuture<D1Result> orders = d1.queryAsync("SELECT COUNT(*) AS count FROM orders");

CompletableFuture.allOf(users, orders).join();

System.out.println(users.join().firstRow());
System.out.println(orders.join().firstRow());
```

## Handle Failures

Failed operations complete the future exceptionally with the same public exception types used by synchronous operations.

```java
try {
  d1.queryAsync("SELECT * FROM missing_table").join();
} catch (CompletionException exception) {
  Throwable cause = exception.getCause();
  if (cause instanceof D1QueryException) {
    System.err.println("Query failed");
  }
}
```

## Run the Repository Example

Read-only async examples:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.AsyncExamples
```

Expected output shape:

```text
simple: {value=1}
parameterized: {value=42}
typed-first: ExampleRow{id=7, name='async'}
parallel: {value=10}, {value=20}
batch: {value=100}, {value=200}
```

Opt-in write example:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.AsyncExamples \
  -Dexec.args="--write"
```

The write example creates a temporary table, inserts one row, selects it, and drops the table in a `finally` block.

The read-only lines are printed first, followed by a write line similar to:

```text
write-select: {id=1, name=write-example}
```
