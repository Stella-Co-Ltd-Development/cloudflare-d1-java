# Project Instructions

@./AGENTS.md

This file imports the authoritative repository instructions from [AGENTS.md](AGENTS.md). Follow that file before editing, testing, committing, or opening a pull request.

## Critical Rules

- Create a new branch before editing files.
- Do not commit directly to `main`.
- Use Java 8-compatible APIs only.
- Never commit API tokens, private keys, account IDs, database IDs, or secrets.
- Add or update tests for behavior changes.
- Run `mvn clean verify` before opening a pull request.
- Use the repository pull request template and link the relevant issue whenever possible.
- Do not include assistant names, tool names, generation metadata, or automation attribution in project artifacts, except where an instruction filename requires it.
