# Phase 04: Response, Error, and Mapping Requirements

## Response Models

Public model classes:

- `D1Result`
- `D1Meta`
- `D1Timings`
- `D1ResponseInfo`
- `D1ResponseSource`

## D1Result

Required accessors:

```java
public boolean success();
public List<Map<String, Object>> rows();
public D1Meta meta();
public List<D1ResponseInfo> messages();
public List<D1ResponseInfo> errors();
public String rawBody();
public boolean isEmpty();
public int rowCount();
public Optional<Map<String, Object>> firstRow();
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

## Error Mapping

- HTTP 401: `D1AuthenticationException`
- HTTP 403: `D1AuthorizationException`
- HTTP 429: `D1RateLimitException`
- HTTP 400: `D1QueryException`
- HTTP 404: `D1ApiException`
- HTTP 5xx: `D1ApiException`
- Query result `success=false`: `D1QueryException`
- Batch result with any failed item: `D1BatchException`
- Mapping failure: `D1MappingException`
- Timeout: `D1TimeoutException`
- Network failure: `D1TransportException`

## Mapping

Typed query uses Jackson.

Unknown properties are ignored by default.

Mapping failure in any row fails the entire typed query.
