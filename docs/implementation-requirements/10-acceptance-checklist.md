# 10. v0.1.0 Acceptance Checklist

## API

- [ ] `D1Client.builder()` works.
- [ ] `D1Client.fromEnv()` works.
- [ ] `query()` works with no params.
- [ ] `query()` works with varargs params.
- [ ] `query()` works with list params.
- [ ] `query(D1Query)` works.
- [ ] typed `query()` works.
- [ ] `queryFirst()` works.
- [ ] typed `queryFirst()` works.
- [ ] `execute()` works.
- [ ] `batch(List<D1Query>)` works.
- [ ] `batch(D1Query...)` works.
- [ ] preview async APIs exist and are deprecated.

## HTTP

- [ ] Correct D1 endpoint is used.
- [ ] Correct headers are sent.
- [ ] Request body for single query is correct.
- [ ] Request body for batch is correct.
- [ ] `params: []` is sent when no params exist.
- [ ] `rawBody()` is preserved.

## Validation

- [ ] Null SQL rejected.
- [ ] Blank SQL rejected.
- [ ] SQL over 100 KB rejected.
- [ ] More than 100 params rejected.
- [ ] Invalid param types rejected.
- [ ] Empty batch rejected.

## Retry

- [ ] Query retries retryable statuses by default.
- [ ] Execute does not retry by default.
- [ ] Batch does not retry by default.
- [ ] Retry-After seconds supported.
- [ ] Retry-After HTTP-date supported.
- [ ] Exponential backoff supported.
- [ ] Full jitter supported.

## Models

- [ ] Rows list immutable.
- [ ] Row maps immutable.
- [ ] Messages immutable.
- [ ] Errors immutable.
- [ ] additionalProperties immutable.
- [ ] Missing meta returns empty meta.
- [ ] Missing results returns empty rows.
- [ ] Unknown response fields preserved.

## Errors

- [ ] 401 maps to `D1AuthenticationException`.
- [ ] 403 maps to `D1AuthorizationException`.
- [ ] 429 maps to `D1RateLimitException`.
- [ ] 400 maps to `D1QueryException`.
- [ ] 404 maps to `D1ApiException`.
- [ ] 5xx maps to `D1ApiException`.
- [ ] Query result failure maps to `D1QueryException`.
- [ ] Batch partial failure maps to `D1BatchException`.
- [ ] Mapping failure maps to `D1MappingException`.
- [ ] Timeout maps to `D1TimeoutException`.
- [ ] Transport failure maps to `D1TransportException`.

## Security

- [ ] API token is not logged.
- [ ] API token is not included in exception messages.
- [ ] SQL params are not included in exception messages.
- [ ] Tests use fake token and IDs only.

## Build and Test

- [ ] `mvn clean verify` passes with Java 8-compatible output.
- [ ] `mvn clean verify` passes on Java 8, 11, 15, 17, and 21.
- [ ] Javadocs build.
- [ ] Sources jar builds.
- [ ] Release profile signs artifacts.
