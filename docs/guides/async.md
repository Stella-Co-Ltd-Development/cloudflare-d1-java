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

By default, async operations use `ForkJoinPool.commonPool()`. Provide an executor when your application needs explicit concurrency control.

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

try (D1AsyncClient d1 = D1AsyncClient.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .executor(executor)
    .build()) {
  CompletableFuture<D1Result> future = d1.queryAsync("SELECT 1 AS value");
  D1Result result = future.join();
  System.out.println(result.firstRow());
} finally {
  executor.shutdown();
}
```

The executor is owned by your application. Closing `D1AsyncClient` does not shut it down.

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
