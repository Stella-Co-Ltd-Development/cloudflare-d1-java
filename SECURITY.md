# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 0.x | Best effort |

## Reporting a Vulnerability

Please do not open a public issue for security vulnerabilities.

Report security issues privately through GitHub Security Advisories if available.

If GitHub Security Advisories are not available, contact the maintainer through the repository owner profile or another private channel provided by the maintainers.

## Sensitive Information

This library must not log or expose Cloudflare API tokens.

Do not include real API tokens, private keys, account IDs, database IDs, customer data, or production SQL data in:

- issues
- pull requests
- logs
- screenshots
- stack traces
- test fixtures
- documentation examples

Use fake values in tests and examples:

```text
test-token
test-account-id
test-database-id
```

## AI Tool Usage and Secrets

Do not paste secrets, API tokens, private keys, customer data, production logs, or confidential information into AI tools while working on this project.

AI-assisted changes are allowed, but contributors are responsible for reviewing and validating all submitted changes.

## Library Security Requirements

- Do not log Authorization headers.
- Do not include API tokens in exception messages.
- Do not include SQL params in exception messages.
- `D1QueryException` may expose SQL through `sql()`, but the default exception message must not include the SQL text.
- Redact secrets from any debug output.
