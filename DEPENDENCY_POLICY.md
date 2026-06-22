# Dependency Policy

## Runtime Dependencies

For v0.1.0, the only runtime dependency should be:

- `jackson-databind`

HTTP must use Java standard library APIs for the default transport.

Retry behavior must be implemented internally.

Logging dependencies must not be added in v0.1.0.

## Adding Runtime Dependencies

New runtime dependencies require:

1. an issue explaining the need
2. dependency license review
3. explicit maintainer approval

Runtime dependencies must be compatible with Apache License 2.0.

## Test Dependencies

Allowed test dependencies for v0.1.0:

- JUnit Jupiter
- AssertJ
- MockWebServer

Additional test dependencies may be added when justified, but they should still be reviewed.

## Dependency Updates

Dependabot pull requests must pass CI before merge.

Major dependency updates require manual review.
