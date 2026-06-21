# 06. Internal Architecture Requirements

## Public Package

```text
io.github.xxvw.cloudflare.d1
```

Public classes:

- `D1Client`
- `D1ClientBuilder`
- `D1Query`
- `D1Result`
- `D1Meta`
- `D1Timings`
- `D1ResponseInfo`
- `D1ResponseSource`
- `D1RetryPolicy`
- `D1Operation`
- exception classes

## Internal Package

```text
io.github.xxvw.cloudflare.d1.internal
```

Suggested internal classes:

- `D1HttpClient`
- `D1JsonMapper`
- `D1Endpoint`
- `D1RetryExecutor`
- `D1RetryAfterParser`
- `D1ResponseParser`
- `D1ExceptionFactory`
- `D1Version`
- `D1Sleeper`

## Internal DTO Package

```text
io.github.xxvw.cloudflare.d1.internal.dto
```

Suggested DTO classes:

- `D1ApiResponseDto`
- `D1QueryResultDto`
- `D1MetaDto`
- `D1TimingsDto`
- `D1ResponseInfoDto`
- `D1ResponseSourceDto`

## D1HttpClient

Responsibilities:

- Build HTTP requests
- Add required headers
- Send synchronous requests
- Send preview asynchronous requests
- Apply request timeout
- Preserve raw response body
- Convert transport failures to `D1TransportException`
- Convert timeouts to `D1TimeoutException`

## D1JsonMapper

Responsibilities:

- Serialize single query request body
- Serialize batch request body
- Parse D1 API response DTOs
- Map rows to typed Java objects using the mapping ObjectMapper

## D1Endpoint

Responsibilities:

- Normalize base URL
- Build D1 query endpoint path
- Path-segment encode account ID and database ID

## D1RetryExecutor

Responsibilities:

- Decide whether an operation can retry
- Decide whether a status code is retryable
- Apply retry count
- Use Retry-After when available
- Use exponential backoff with optional full jitter
- Use a testable sleeper abstraction

## D1RetryAfterParser

Responsibilities:

- Parse seconds-based Retry-After
- Parse HTTP-date Retry-After
- Return empty for invalid values
- Return zero for past HTTP-date values

## D1ResponseParser

Responsibilities:

- Parse raw response body
- Convert DTOs to public models
- Handle missing results
- Handle missing meta
- Preserve unknown fields
- Preserve raw body

## D1ExceptionFactory

Responsibilities:

- Map HTTP status and D1 response failures to public exceptions
- Preserve status code, raw body, errors, and messages
- Create `D1BatchException` for batch partial failures
- Ensure sensitive values are not included in exception messages

## D1Version

Responsibilities:

- Provide version for default User-Agent.
- v0.1.0 may use a simple constant.
- Future versions may read from Maven package metadata.

## D1Sleeper

Internal testability abstraction for retry sleeping:

```java
interface D1Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
```

This must not be public API.
