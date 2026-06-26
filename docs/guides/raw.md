# Raw Query API

Use `raw(...)` when you want D1 rows returned as arrays with a separate column list. This maps to Cloudflare D1's `/raw` REST endpoint and can reduce object allocation when callers already know the selected column order.

```java
try (D1Client d1 = D1Client.fromEnv()) {
  D1RawResult result = d1.raw(
      "SELECT id, name FROM users WHERE active = ?",
      true
  );

  List<String> columns = result.columns();
  List<List<Object>> rows = result.rows();
}
```

For example, a query selecting `id` and `name` may return:

```text
columns: [id, name]
rows: [[1, Taro], [2, Jiro]]
```

## Raw Batch

Use `rawBatch(...)` to send multiple raw queries in one request:

```java
List<D1RawResult> results = d1.rawBatch(
    D1Query.of("SELECT 1 AS value"),
    D1Query.of("SELECT 2 AS value")
);
```

Empty raw batches are rejected.

## Async Raw Queries

`D1AsyncClient` provides matching asynchronous methods:

```java
try (D1AsyncClient d1 = D1AsyncClient.fromEnv()) {
  D1RawResult result = d1.rawAsync("SELECT ? AS value", 42).join();
  System.out.println(result.rows());
}
```

## Retry Behavior

`raw(...)` retries transient failures by default, like `query(...)`.

`rawBatch(...)` does not retry by default, like `batch(...)`, because a raw batch may contain statements that are not safe to repeat.

## Notes

- Parameters may be `String`, `Number`, `Boolean`, or `null`.
- Convert dates, JSON values, and custom objects to strings before passing them as SQL parameters.
- Returned column and row collections are immutable.
- Exception messages do not include API tokens or SQL parameter values.
