# Java Library Implementation Requirements

This directory contains the implementation requirements for `cloudflare-d1-java`.

The documents are written for human contributors and coding agents. They define the Java library implementation scope, public API, internal architecture, HTTP behavior, response models, exceptions, retry behavior, testing requirements, and release-oriented implementation tasks.

## Document Map

| File | Purpose |
|---|---|
| `00-overview.md` | Product purpose, implementation principles, package layout, and non-goals |
| `01-public-api.md` | Public Java API requirements for `D1Client`, `D1Query`, query, execute, batch, typed query, and queryFirst |
| `02-models-and-errors.md` | Public response models and exception hierarchy |
| `03-http-and-d1-rest-api.md` | Cloudflare D1 REST API endpoint, request body, headers, raw body, and status handling |
| `04-retry-and-validation.md` | Retry policy, Retry-After parsing, backoff, SQL and params validation |
| `05-json-and-mapping.md` | Jackson usage, internal DTO mapping, typed row mapping, ObjectMapper policy |
| `06-internal-architecture.md` | Internal class responsibilities and package layout |
| `07-testing-requirements.md` | Unit and MockWebServer test matrix |
| `08-maven-and-release.md` | Maven project requirements, dependencies, plugins, release profile |
| `09-implementation-task-plan.md` | Suggested implementation issue/task breakdown |
| `10-acceptance-checklist.md` | v0.1.0 implementation acceptance checklist |
| `11-usage-examples.md` | Expected user-facing examples for README and tests |

## Extraction

This ZIP is intended to be extracted at the repository root:

```bash
unzip cloudflare-d1-java-implementation-docs.zip
```

It will create or update:

```text
docs/implementation-requirements/
```
