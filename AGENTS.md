# Agent Instructions

This repository contains `cloudflare-d1-java`, an unofficial Java client for the Cloudflare D1 REST API.

These instructions apply to all automated or AI-assisted coding agents working in this repository.

## Source of Truth

This file is the authoritative repository instruction file for automated or AI-assisted coding work.

Tool-specific instruction files in this repository are lightweight entrypoints. They must point back to this file and must not redefine conflicting workflow, security, dependency, testing, or public API policy.

When instruction policy changes, update this file first and then update tool-specific entrypoints only as needed.

## Required Workflow

1. Create a new branch before editing files.
2. Do not commit directly to `main`.
3. Make focused changes.
4. Add or update tests for behavior changes.
5. Run the test suite before opening a pull request:

```bash
mvn clean verify
```

6. Open a pull request using the repository pull request template.
7. Link the relevant issue whenever possible.

## Branching Rules

- Always create a new branch before making changes.
- Do not commit directly to `main`.
- Use clear branch names such as:
  - `feature/query-api`
  - `fix/retry-after-parser`
  - `docs/readme-quick-start`
  - `test/batch-error-mapping`
  - `ci/maven-central-release`

## Pull Request Rules

- Always open a pull request for changes.
- Always use the repository pull request template.
- Link the relevant issue whenever possible.
- Keep pull requests focused and reasonably small.
- Do not include names of AI tools, assistants, or automation systems in:
  - PR titles
  - PR descriptions
  - commit messages
  - code
  - code comments
  - documentation
  - Javadocs
  - changelog entries
  - release notes

## Issue Rules

- Use the repository issue templates.
- Do not start implementation work for non-trivial changes without an issue.
- If no issue exists, create one first.
- Do not include secrets, API tokens, private keys, customer data, or confidential data in issues.

## AI-Assisted Development Policy

- AI-assisted development is allowed.
- Contributors are fully responsible for reviewing, testing, validating, and maintaining all submitted changes.
- AI-assisted changes must follow the same review, testing, licensing, and security requirements as any other contribution.
- Do not include AI tool names, assistant names, generation metadata, or automation attribution in project artifacts.
- Do not paste secrets, API tokens, private keys, customer data, or confidential information into AI tools while working on this project.
- Do not submit code copied from sources with incompatible licenses.

## Public API Rules

Public API belongs in:

```text
io.github.xxvw.cloudflare.d1
```

Internal implementation belongs in:

```text
io.github.xxvw.cloudflare.d1.internal
```

Rules:

- Do not expose internal DTOs or helper classes as public API.
- Keep public APIs stable and minimal.
- Use Java 8-compatible APIs only.
- Public model classes should be immutable where designed.
- Public collections returned by model classes must be immutable.
- Do not leak mutable internal state.

## Dependency Rules

For v0.1.x:

Runtime dependency:

- `jackson-databind`

Test dependencies:

- JUnit Jupiter
- AssertJ
- MockWebServer

Rules:

- Do not add new runtime dependencies without an issue and explicit maintainer approval.
- Dependency licenses must be reviewed before adoption.
- Runtime dependencies must be compatible with Apache License 2.0.
- Use Java standard library HTTP APIs for the default transport.
- Implement retry behavior internally.
- Do not add logging dependencies in v0.1.0.

## Security Rules

- Never hard-code Cloudflare API tokens.
- Never commit API tokens, private keys, account IDs, database IDs, or secrets.
- Use fake values in tests:
  - `test-token`
  - `test-account-id`
  - `test-database-id`
- Never log API tokens or Authorization headers.
- Never include API tokens in exception messages.
- Never include SQL params in exception messages.
- `D1QueryException` may expose SQL through `sql()`, but default exception messages must not include SQL text.
- Remove secrets and sensitive data from bug reports, logs, stack traces, and screenshots.
- Security vulnerabilities must be reported privately, not through public issues.

## Testing Rules

- Tests must be written in English.
- Test method names must be written in English.
- Test comments must be written in English.
- Use JUnit Jupiter, AssertJ, and MockWebServer.
- Add or update tests for all behavior changes.
- Add tests for:
  - request body shape
  - response parsing
  - error mapping
  - retry behavior
  - immutability
  - typed query mapping

## Documentation Rules

- README, Javadocs, comments, and public documentation must be written in English.
- The README must clearly state that this project is an unofficial Cloudflare D1 Java client.
- Keep examples simple and copy-paste friendly.
- Do not mention AI tools, AI-generated code, or automation metadata in public documentation.

## Commit and PR Style

- Commit messages must be clear and human-readable.
- Conventional Commits are recommended but not required in v0.1.0.
- PR titles should be concise and describe the change.
- PRs should be focused on one logical change.

Recommended commit examples:

```text
feat: add D1Query validation
fix: handle non-JSON error responses
test: add retry policy tests
docs: add quick start example
ci: add Maven Central release workflow
```

## Release Rules

- Only maintainers may publish releases.
- Maven Central publishing must happen through GitHub Actions.
- Release tags must match `v*`.
- Do not publish from a dirty working tree.
- Update `CHANGELOG.md` before release.
- Do not include AI tool names, assistant names, or generation metadata in release notes.
