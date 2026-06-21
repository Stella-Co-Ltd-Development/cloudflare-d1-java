# Phase 05: Testing Requirements

## Language

- Test names must be written in English.
- Test comments must be written in English.
- Documentation and Javadocs must be written in English.

## Test Dependencies

- JUnit Jupiter
- AssertJ
- MockWebServer

## Test Categories

Unit tests:

- `D1Query`
- `D1RetryPolicy`
- `D1RetryAfterParser`
- `D1Endpoint`
- `D1Result`
- `D1Meta`
- exception classes

MockWebServer tests:

- request body shape
- headers
- response parsing
- error mapping
- retry behavior

## Required Test Areas

- Query validation
- Retry policy validation
- Retry-After parsing
- Endpoint building
- Response parsing
- Immutability
- Typed query mapping
- queryFirst behavior
- Error mapping
- Batch partial failure
- Timeout and transport failures
