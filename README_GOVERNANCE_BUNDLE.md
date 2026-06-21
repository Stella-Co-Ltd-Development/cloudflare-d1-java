# cloudflare-d1-java Governance Bundle

This ZIP contains governance and repository rule files for `cloudflare-d1-java`.

## Included Files

```text
AGENTS.md
CLAUDE.md
CONTRIBUTING.md
SECURITY.md
CODE_OF_CONDUCT.md
MAINTAINERS.md
GOVERNANCE.md
BRANCH_PROTECTION.md
DEPENDENCY_POLICY.md
RELEASE_POLICY.md
.github/pull_request_template.md
.github/ISSUE_TEMPLATE/feature_request.yml
.github/ISSUE_TEMPLATE/bug_report.yml
.github/ISSUE_TEMPLATE/task.yml
.github/ISSUE_TEMPLATE/config.yml
```

## Agent Files

`AGENTS.md` and `CLAUDE.md` intentionally contain the same instructions.

Use:

- `AGENTS.md` for Codex-style agents
- `CLAUDE.md` for Claude Code

## Core Rules

- Create a branch before working.
- Do not commit directly to `main`.
- Use pull request and issue templates.
- Keep AI tool names and generation metadata out of PRs, commits, code, comments, docs, changelog entries, and release notes.
- Use English for tests, comments, documentation, and Javadocs.
- Do not commit secrets or sensitive data.
- New runtime dependencies require an issue and maintainer approval.
- Releases are maintainer-only and should be published through GitHub Actions.
