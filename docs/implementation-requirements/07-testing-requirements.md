# 07. Testing Requirements

## Language

- Test names must be written in English.
- Test comments must be written in English.
- Documentation and Javadocs must be written in English.

## Test Dependencies

- JUnit Jupiter
- AssertJ
- MockWebServer

## Test Structure

Suggested test files:

```text
src/test/java/io/github/xxvw/cloudflare/d1/
  D1ClientTest.java
  D1QueryTest.java
  D1RetryPolicyTest.java
  D1ResultTest.java
  D1ExceptionTest.java

src/test/java/io/github/xxvw/cloudflare/d1/internal/
  D1EndpointTest.java
  D1RetryAfterParserTest.java
  D1ResponseParserTest.java
  D1ExceptionFactoryTest.java
```

## D1Query Tests

Required cases:

- Create query without params
- Create query with varargs params
- Create query with list params
- Allow null param element
- Allow String param
- Allow Number param
- Allow Boolean param
- Reject null SQL
- Reject blank SQL
- Reject null params list
- Reject invalid param type
- Reject more than 100 params
- Reject SQL over 100 KB
- Allow SQL exactly 100 KB
- Verify params are defensively copied
- Verify params list is immutable

## D1RetryPolicy Tests

Required cases:

- Default policy values
- `none()` values
- Accept maxRetries 0
- Accept `Duration.ZERO`
- Accept maxDelay equal to baseDelay
- Reject negative maxRetries
- Reject null baseDelay
- Reject null maxDelay
- Reject negative durations
- Reject maxDelay less than baseDelay
- Reject null retryStatusCodes
- Reject empty retryStatusCodes
- Reject status code below 100
- Reject status code above 599
- Verify retryStatusCodes are immutable

## D1RetryAfterParser Tests

Required cases:

- Parse seconds
- Parse zero seconds
- Parse future HTTP-date
- Parse past HTTP-date as zero
- Ignore invalid value
- Ignore null value
- Ignore negative seconds

## D1Endpoint Tests

Required cases:

- Build URL with base URL without trailing slash
- Build URL with base URL with trailing slash
- Encode account ID as path segment
- Encode database ID as path segment
- Reject null base URL
- Reject blank base URL
- Reject invalid URI string

## D1Result and D1Meta Tests

Required cases:

- Rows list is immutable
- Row map is immutable
- Messages list is immutable
- Errors list is immutable
- additionalProperties map is immutable
- Empty rows means `isEmpty() == true`
- Non-empty rows means `isEmpty() == false`
- `rowCount()` returns row count
- `firstRow()` returns first row
- Empty meta returns default values
- Unknown meta fields are preserved

## MockWebServer D1Client Tests

Request tests:

- Query sends correct path
- Query sends Authorization header
- Query sends Content-Type
- Query sends Accept
- Query sends User-Agent
- Query without params sends `params: []`
- Query with params sends params
- Execute sends same request shape
- Batch list sends `batch`
- Batch varargs sends `batch`
- Empty batch throws `IllegalArgumentException`

Response tests:

- Successful SELECT response returns rows
- Successful INSERT response returns meta
- Missing results returns empty rows
- Empty results returns empty rows
- Missing meta returns empty meta
- Messages are mapped
- Unknown meta fields are preserved

Typed query tests:

- Record mapping succeeds
- Unknown property is ignored
- Missing required field fails
- Type conversion failure throws `D1MappingException`
- Mapping exception contains targetType, rowIndex, and row

queryFirst tests:

- Empty result returns `Optional.empty()`
- One row returns that row
- Multiple rows returns first row
- Typed queryFirst maps first row

Error tests:

- HTTP 401 throws `D1AuthenticationException`
- HTTP 403 throws `D1AuthorizationException`
- HTTP 429 throws `D1RateLimitException`
- HTTP 400 throws `D1QueryException`
- HTTP 404 throws `D1ApiException`
- HTTP 500 throws `D1ApiException`
- HTTP 200 with top-level `success=false` throws D1 exception
- Query result `success=false` throws `D1QueryException`
- Batch partial failure throws `D1BatchException`
- Non-JSON error body is preserved in `rawBody()`
- JSON parse failure throws `D1ApiException`
- Network failure throws `D1TransportException`
- Timeout throws `D1TimeoutException`

Retry tests:

- Query retries 429 then succeeds
- Query retries 500 then succeeds
- Query stops after max retries
- Query does not retry non-retryable status
- Execute does not retry by default
- Batch does not retry by default
- Execute retries when enabled
- Batch retries when enabled
- Retry-After seconds is honored
- Invalid Retry-After falls back to backoff
