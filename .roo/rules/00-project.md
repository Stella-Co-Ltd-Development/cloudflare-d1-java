# Repository Instructions

The authoritative instructions are in [AGENTS.md](../../AGENTS.md). Follow that file for workflow, security, dependency, testing, documentation, and public API rules.

## Critical Rules

- Create a new branch before editing files.
- Do not commit directly to `main`.
- Use Java 8-compatible APIs only.
- Keep public API in `io.github.xxvw.cloudflare.d1`.
- Keep internal implementation in `io.github.xxvw.cloudflare.d1.internal`.
- Never commit API tokens, private keys, account IDs, database IDs, or secrets.
- Add or update tests for behavior changes.
- Run `mvn clean verify` before opening a pull request.
- Use the repository pull request template and link the relevant issue whenever possible.
