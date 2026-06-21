# Governance

This document summarizes the repository governance rules for `cloudflare-d1-java`.

## Project Identity

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

This project is not affiliated with, endorsed by, or sponsored by Cloudflare.

## License

The project is licensed under the Apache License 2.0.

New source files do not require per-file license headers in v0.1.0.

## Contribution Model

- CLA is not required for v0.1.0.
- DCO is not required for v0.1.0.
- By submitting a contribution, contributors agree that their contribution is licensed under Apache License 2.0.
- Contributors must have the right to submit their contribution.
- Contributions must not include code, documentation, or assets copied from sources with incompatible licenses.

## AI-Assisted Development

AI-assisted development is allowed.

Contributors remain fully responsible for reviewing, testing, validating, and maintaining any submitted changes.

AI tool names, assistant names, generation metadata, or automation attribution must not appear in:

- PR titles
- PR descriptions
- commit messages
- code
- code comments
- documentation
- Javadocs
- changelog entries
- release notes

## Branch Protection

The `main` branch must be protected.

Recommended GitHub settings:

- Require pull requests before merging
- Require CI before merging
- Disable direct pushes to `main`
- Disable force pushes
- Disable branch deletion
- Require conversation resolution before merging
- Use squash merge by default

## Dependency Governance

For v0.1.0, the only runtime dependency should be:

- `jackson-databind`

Rules:

- New runtime dependencies require an issue and explicit maintainer approval.
- Dependency licenses must be reviewed before adoption.
- Runtime dependencies must be compatible with Apache License 2.0.
- Test dependencies may be added when justified.
- Major dependency updates require manual review.

## Security

- Never commit secrets.
- Never log Authorization headers.
- Never include API tokens or SQL params in exception messages.
- Use fake values in tests and examples.
- Report vulnerabilities privately.

## Release

Only maintainers may publish releases.

Maven Central publishing must be performed through GitHub Actions.

Release tags must match:

```text
v*
```

Release notes must not include AI tool names, assistant names, or generation metadata.
