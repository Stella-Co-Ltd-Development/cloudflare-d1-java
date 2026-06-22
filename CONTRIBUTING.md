# Contributing

Thank you for your interest in contributing to `cloudflare-d1-java`.

This project is an unofficial Java client for the Cloudflare D1 REST API.

## Required Workflow

1. Create or find an issue for non-trivial work.
2. Create a new branch before making changes.
3. Make focused changes.
4. Add or update tests.
5. Run:

```bash
mvn clean verify
```

6. Open a pull request using the repository pull request template.
7. Link the relevant issue whenever possible.

Do not commit directly to `main`.

## Contribution License

By submitting a contribution to this project, you agree that your contribution is licensed under the Apache License 2.0.

You also confirm that you have the right to submit the contribution and that it does not include code, documentation, or assets copied from sources with incompatible licenses.

This project does not require a CLA or DCO for v0.1.0.

## AI-Assisted Contributions

AI-assisted development is allowed.

However:

- You are responsible for reviewing, testing, validating, and maintaining all submitted changes.
- AI-assisted changes must follow the same review, testing, licensing, and security requirements as any other contribution.
- Do not include AI tool names, assistant names, generation metadata, or automation attribution in:
  - PR titles
  - PR descriptions
  - commit messages
  - code
  - code comments
  - documentation
  - Javadocs
  - changelog entries
  - release notes
- Do not paste secrets, API tokens, private keys, customer data, or confidential information into AI tools while working on this project.

## Branching

- Create a branch before editing files.
- Do not commit directly to `main`.
- Use clear branch names, for example:
  - `feature/query-api`
  - `fix/retry-after-parser`
  - `docs/readme-quick-start`
  - `test/batch-error-mapping`

## Pull Requests

- Use the pull request template.
- Keep pull requests focused.
- Link the relevant issue whenever possible.
- Add or update tests for behavior changes.
- Update documentation when public behavior changes.
- Update `CHANGELOG.md` when user-visible behavior changes.

## Commit Messages

Commit messages must be clear and human-readable.

Conventional Commits are recommended but not required in v0.1.0.

Examples:

```text
feat: add D1Query validation
fix: handle non-JSON error responses
test: add retry policy tests
docs: add quick start example
ci: add Maven Central release workflow
```

Do not include AI tool names, assistant names, or generation metadata in commit messages.

## Development Requirements

- Java 8 or later
- Maven 3.9 or later

## Build

```bash
mvn clean verify
```

## Tests

```bash
mvn test
```

Test names, comments, and documentation should be written in English.

## Public API Rules

Public API belongs in:

```text
io.github.xxvw.cloudflare.d1
```

Internal implementation belongs in:

```text
io.github.xxvw.cloudflare.d1.internal
```

Do not expose internal DTOs or internal helper classes as public API.

## Dependency Governance

For v0.1.0, the only runtime dependency should be:

- `jackson-databind`

Test dependencies:

- JUnit Jupiter
- AssertJ
- MockWebServer

Rules:

- New runtime dependencies require an issue and explicit maintainer approval.
- Dependency licenses must be reviewed before adoption.
- Runtime dependencies must be compatible with Apache License 2.0.
- Test dependencies may be added when justified, but still require a clear reason.
- Dependabot pull requests must pass CI before merge.
- Major dependency updates require manual review.

## Security

- Never hard-code API tokens.
- Never commit secrets, private keys, API tokens, account IDs, or database IDs.
- Use fake values in tests:
  - `test-token`
  - `test-account-id`
  - `test-database-id`
- Never include API tokens or SQL params in exception messages.
- Remove secrets and sensitive data from logs, screenshots, bug reports, and stack traces.
- Report security vulnerabilities privately.

## Release

Only maintainers may publish releases.

Releases must be created from Git tags matching:

```text
v*
```

Maven Central publishing should be performed through GitHub Actions.

Before release:

- Update `CHANGELOG.md`
- Change the version from `SNAPSHOT` to the release version
- Ensure CI passes
- Ensure source jar, javadoc jar, and signatures are generated
