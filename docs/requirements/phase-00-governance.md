# Phase 00: Governance Requirements

## Project Identity

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

This project is not affiliated with, endorsed by, or sponsored by Cloudflare.

## License

- Project license: Apache License 2.0
- Per-file license headers are not required for v0.1.0
- Contributions are accepted under Apache License 2.0
- CLA is not required for v0.1.0
- DCO is not required for v0.1.0

## AI-Assisted Development

AI-assisted development is allowed.

Contributors remain responsible for reviewing, testing, validating, and maintaining submitted changes.

AI tool names, assistant names, generation metadata, and automation attribution must not appear in:

- PR titles
- PR descriptions
- commit messages
- code
- code comments
- documentation
- Javadocs
- changelog entries
- release notes

## Branching

- Create a branch before editing files.
- Do not commit directly to `main`.
- Use pull requests for all changes.
- Use the repository pull request template.
- Link relevant issues whenever possible.

## Main Branch Protection

The `main` branch must be protected.

Required:

- Direct pushes disabled
- Pull requests required
- CI required before merge
- Force pushes disabled
- Branch deletion disabled

## Security

- Do not commit secrets.
- Do not log Authorization headers.
- Do not include API tokens or SQL params in exception messages.
- Use fake values in tests:
  - `test-token`
  - `test-account-id`
  - `test-database-id`

## Release Authority

Only maintainers may publish releases.

Maven Central publishing must be performed through GitHub Actions.

Release tags must match:

```text
v*
```
