# Implementation Requirements ZIP Manifest

This ZIP is intended to be extracted at the repository root.

It will create:

```text
docs/implementation-requirements/
```

## Files

| File | Purpose |
|---|---|
| `README.md` | Index and usage instructions |
| `00-overview.md` | Product purpose, implementation principles, package layout, non-goals |
| `01-public-api.md` | Public Java API requirements |
| `02-models-and-errors.md` | Response model and exception requirements |
| `03-http-and-d1-rest-api.md` | HTTP and Cloudflare D1 REST API requirements |
| `04-retry-and-validation.md` | Retry policy and validation requirements |
| `05-json-and-mapping.md` | Jackson, ObjectMapper, DTO, and typed mapping requirements |
| `06-internal-architecture.md` | Internal class responsibilities and package layout |
| `07-testing-requirements.md` | Unit and MockWebServer test requirements |
| `08-maven-and-release.md` | Maven, dependencies, plugins, and release requirements |
| `09-implementation-task-plan.md` | Suggested implementation tasks for v0.1.0 |
| `10-acceptance-checklist.md` | Acceptance checklist for v0.1.0 |
| `11-usage-examples.md` | Expected user-facing examples |
