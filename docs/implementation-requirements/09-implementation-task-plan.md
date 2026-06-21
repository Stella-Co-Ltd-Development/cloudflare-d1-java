# 09. Implementation Task Plan

This task plan is intended for v0.1.0 implementation.

## Task 1: Initialize Maven Project

Acceptance criteria:

- Maven project compiles on Java 17.
- Dependencies are declared.
- `mvn clean verify` succeeds.
- Maven metadata is ready for Maven Central.

## Task 2: Implement D1Query

Acceptance criteria:

- SQL and params validation works.
- Params are immutable.
- Unit tests cover valid and invalid cases.

## Task 3: Implement Public Models

Acceptance criteria:

- `D1Result`, `D1Meta`, `D1Timings`, `D1ResponseInfo`, and `D1ResponseSource` exist.
- Collections are immutable.
- Missing fields return default values.

## Task 4: Implement Exception Hierarchy

Acceptance criteria:

- All exception classes exist.
- Exceptions expose required metadata.
- Sensitive values are not included in messages.

## Task 5: Implement Retry Policy

Acceptance criteria:

- Default policy matches requirements.
- `none()` disables retries.
- Validation works.
- Unit tests cover policy behavior.

## Task 6: Implement Retry-After Parser and Backoff

Acceptance criteria:

- Seconds and HTTP-date are supported.
- Invalid values are ignored.
- Backoff and jitter are testable.

## Task 7: Implement Endpoint Builder

Acceptance criteria:

- Endpoint path is correct.
- Base URL trailing slash is handled.
- Account and database IDs are encoded.

## Task 8: Implement Internal DTOs and Response Parser

Acceptance criteria:

- D1 API responses parse correctly.
- Missing meta/results are handled.
- Unknown fields are preserved.

## Task 9: Implement Exception Factory

Acceptance criteria:

- HTTP statuses map to expected exceptions.
- Query and batch failures are detected.
- Non-JSON error bodies are preserved.

## Task 10: Implement Internal HTTP Client

Acceptance criteria:

- Requests use Java HttpClient.
- Headers are correct.
- Request body shape is correct.
- Transport and timeout errors are mapped.

## Task 11: Implement D1ClientBuilder and fromEnv

Acceptance criteria:

- Builder defaults work.
- Required values are validated.
- `fromEnv()` loads expected environment variables.
- Missing env values throw `IllegalStateException`.

## Task 12: Implement Query and Execute

Acceptance criteria:

- Query sends correct body.
- Execute sends correct body.
- Query retries by default.
- Execute does not retry by default.

## Task 13: Implement Typed Query

Acceptance criteria:

- Rows map to Java records/classes.
- Mapping failures throw `D1MappingException`.
- Custom mapping ObjectMapper works.

## Task 14: Implement queryFirst

Acceptance criteria:

- Empty result returns empty.
- Multiple rows return first row.
- Typed queryFirst maps first row.

## Task 15: Implement Batch

Acceptance criteria:

- Batch list and varargs work.
- Empty batch fails.
- Partial failure throws `D1BatchException`.

## Task 16: Implement Preview Async API

Acceptance criteria:

- Async methods are deprecated.
- Successful calls complete normally.
- Failed calls complete exceptionally.

## Task 17: Add MockWebServer Tests

Acceptance criteria:

- Request shape tests pass.
- Response parsing tests pass.
- Error mapping tests pass.
- Retry tests pass.

## Task 18: Write User Documentation

Acceptance criteria:

- README examples are aligned with implemented API.
- Limitations are documented.
- Security notes are documented.
- Async preview status is documented.
