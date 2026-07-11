# Maintainers

This project is maintained by the repository owner.

## Responsibilities

Maintainers are responsible for:

- reviewing pull requests
- managing releases
- approving new runtime dependencies
- handling security reports
- maintaining Maven Central publishing configuration
- maintaining repository governance rules
- enforcing the Code of Conduct
- protecting the stability of the public API

## Release Authority

Only maintainers may publish releases to Maven Central.

Releases must be created from Git tags matching:

```text
v*
```

Maven Central publishing should be performed through GitHub Actions.

Local manual publishing is discouraged.

## Dependency Approval

New runtime dependencies require:

1. an issue explaining the need
2. dependency license review
3. explicit maintainer approval

For v0.1.0, the only runtime dependency should be `jackson-databind`.

## Security Handling

Maintainers are responsible for reviewing security reports privately and coordinating fixes when needed.

Security vulnerabilities should not be handled through public issues.
