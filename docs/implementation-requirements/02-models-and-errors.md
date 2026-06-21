# 02. Models and Error Requirements

## Public Response Models

Public model classes:

- `D1Result`
- `D1Meta`
- `D1Timings`
- `D1ResponseInfo`
- `D1ResponseSource`

All public model classes should be `public final class`.

## D1Result

Required API:

```java
public final class D1Result {
    public boolean success();

    public List<Map<String, Object>> rows();

    public D1Meta meta();

    public List<D1ResponseInfo> messages();

    public List<D1ResponseInfo> errors();

    public String rawBody();

    public boolean isEmpty();

    public int rowCount();

    public Optional<Map<String, Object>> firstRow();
}
```

Rules:

- `rows()` must never return null.
- Missing `results` means empty rows.
- `rows()` must return an immutable list.
- Each row map must be immutable.
- `firstRow()` must return an immutable map if present.
- `messages()` and `errors()` must return immutable lists.
- `rawBody()` must contain the raw HTTP response body.

## D1Meta

Required API:

```java
public final class D1Meta {
    public boolean changedDb();

    public long changes();

    public OptionalLong lastRowId();

    public long rowsRead();

    public long rowsWritten();

    public double duration();

    public OptionalLong sizeAfter();

    public Optional<String> servedByColo();

    public Optional<String> servedByRegion();

    public Optional<Boolean> servedByPrimary();

    public Optional<D1Timings> timings();

    public Map<String, Object> additionalProperties();
}
```

Missing meta must be represented as an empty `D1Meta`.

Default values:

```text
changedDb = false
changes = 0
rowsRead = 0
rowsWritten = 0
duration = 0.0
lastRowId = OptionalLong.empty()
sizeAfter = OptionalLong.empty()
servedByColo = Optional.empty()
servedByRegion = Optional.empty()
servedByPrimary = Optional.empty()
timings = Optional.empty()
```

Unknown meta fields must be preserved in `additionalProperties()`.

## D1Timings

```java
public final class D1Timings {
    public double sqlDurationMs();

    public Map<String, Object> additionalProperties();
}
```

## D1ResponseInfo

```java
public final class D1ResponseInfo {
    public int code();

    public String message();

    public Optional<String> documentationUrl();

    public Optional<D1ResponseSource> source();

    public Map<String, Object> additionalProperties();
}
```

## D1ResponseSource

```java
public final class D1ResponseSource {
    public Optional<String> pointer();

    public Map<String, Object> additionalProperties();
}
```

## Exception Hierarchy

```text
D1Exception
 ├─ D1ApiException
 │   ├─ D1AuthenticationException
 │   ├─ D1AuthorizationException
 │   ├─ D1RateLimitException
 │   ├─ D1QueryException
 │   └─ D1BatchException
 ├─ D1MappingException
 ├─ D1TimeoutException
 └─ D1TransportException
```

All exceptions are unchecked.

## D1Exception

Required API:

```java
public class D1Exception extends RuntimeException {
    public Optional<D1Operation> operation();

    public OptionalInt statusCode();

    public Optional<String> rawBody();

    public List<D1ResponseInfo> errors();

    public List<D1ResponseInfo> messages();
}
```

Rules:

- API tokens must never appear in exception messages.
- SQL params must never appear in exception messages.
- `rawBody()` must preserve the response body when available.

## D1RateLimitException

Required API:

```java
public Optional<Duration> retryAfter();
```

## D1QueryException

Required API:

```java
public Optional<String> sql();
```

Rules:

- May store SQL.
- Must not store SQL params.
- Default exception message must not include SQL text.

## D1BatchException

Required API:

```java
public int failedIndex();

public List<D1Result> partialResults();
```

Rules:

- `failedIndex()` returns the first failed result index.
- `partialResults()` must be immutable.

## D1MappingException

Required API:

```java
public Class<?> targetType();

public int rowIndex();

public Map<String, Object> row();
```

Rules:

- `row()` must be immutable.
- Default exception message must not include row contents.

## Error Mapping

- HTTP 401: `D1AuthenticationException`
- HTTP 403: `D1AuthorizationException`
- HTTP 429: `D1RateLimitException`
- HTTP 400: `D1QueryException`
- HTTP 404: `D1ApiException`
- HTTP 5xx: `D1ApiException`
- HTTP 2xx with top-level `success=false`: `D1ApiException` family
- Query result `success=false`: `D1QueryException`
- Batch result with any failed item: `D1BatchException`
- Network failure: `D1TransportException`
- Timeout: `D1TimeoutException`
- Non-JSON error body: `D1ApiException` with `rawBody()`
