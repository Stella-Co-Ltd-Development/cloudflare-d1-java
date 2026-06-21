# 11. Usage Examples

These examples should be kept aligned with the implemented API and README.

## Create Client

```java
D1Client d1 = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .build();
```

## Create Client from Environment

```java
D1Client d1 = D1Client.fromEnv();
```

## Query

```java
D1Result result = d1.query(
    "SELECT id, name FROM users WHERE id = ?",
    1
);

List<Map<String, Object>> rows = result.rows();
```

## Typed Query

```java
record User(long id, String name, String email) {}

List<User> users = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    User.class,
    true
);
```

## Query First

```java
Optional<Map<String, Object>> row = d1.queryFirst(
    "SELECT id, name FROM users WHERE id = ?",
    1
);
```

## Typed Query First

```java
Optional<User> user = d1.queryFirst(
    "SELECT id, name, email FROM users WHERE id = ?",
    User.class,
    1
);
```

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

## Batch

```java
List<D1Result> results = d1.batch(
    D1Query.of("INSERT INTO users(name) VALUES (?)", "Taro"),
    D1Query.of("SELECT * FROM users")
);
```

## Retry Policy

```java
D1Client client = D1Client.builder()
    .accountId("account-id")
    .databaseId("database-id")
    .apiToken("api-token")
    .retryPolicy(D1RetryPolicy.builder()
        .retryQuery(true)
        .retryExecute(false)
        .retryBatch(false)
        .maxRetries(3)
        .baseDelay(Duration.ofMillis(300))
        .maxDelay(Duration.ofSeconds(5))
        .jitter(true)
        .respectRetryAfter(true)
        .build())
    .build();
```

## Custom ObjectMapper for Typed Mapping

```java
ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule());

D1Client client = D1Client.builder()
    .accountId("account-id")
    .databaseId("database-id")
    .apiToken("api-token")
    .objectMapper(mapper)
    .build();
```

The configured ObjectMapper is used only for row-to-type mapping. It is not used for internal Cloudflare D1 API request or response parsing.

## Error Handling

```java
try {
    d1.query("SELECT * FROM missing_table");
} catch (D1QueryException e) {
    System.err.println("Query failed");
    e.sql().ifPresent(System.err::println);
} catch (D1RateLimitException e) {
    System.err.println("Rate limited");
    e.retryAfter().ifPresent(System.err::println);
} catch (D1ApiException e) {
    System.err.println("D1 API error: " + e.statusCode());
}
```

## Security Notes

Do not hard-code API tokens.

Use environment variables or a secret management system.

Do not include real API tokens, account IDs, database IDs, or production data in tests, logs, issues, or examples.
