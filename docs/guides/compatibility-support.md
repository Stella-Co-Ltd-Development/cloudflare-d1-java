# Compatibility and Support

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

## Supported Java Versions

| Java version | Status |
|---|---|
| Java 8 | Supported and used as the bytecode target |
| Java 11 | Tested in CI |
| Java 15 | Tested in CI |
| Java 17 | Tested in CI |
| Java 21 | Tested in CI |

The library uses Java 8-compatible APIs. Newer Java versions can consume the same artifact from Maven Central.

## Supported D1 REST API Usage

| Capability | Status |
|---|---|
| Query rows | Supported |
| Query first row | Supported |
| Execute writes | Supported |
| Batch statements | Supported |
| Typed row mapping | Supported through Jackson |
| Asynchronous calls | Supported through `D1AsyncClient` |
| Custom transport | Supported through `D1Transport` |

The default transport uses Java standard library HTTP APIs and does not add a logging framework or HTTP client dependency.

## Non-goals

This project does not provide:

- JDBC driver support.
- JPA or ORM integration.
- SQL query building.
- Schema migrations.
- A Spring Boot starter.
- Cloudflare Workers binding APIs.
- Local D1 emulation.

## API Stability

The supported public API lives in `io.github.xxvw.cloudflare.d1`.

Classes under `io.github.xxvw.cloudflare.d1.internal` are implementation details and may change without notice. Do not use internal DTOs, parsers, transports, or helpers from application code.

For the `0.x` series, public APIs are intended to remain small and practical while the project learns from real usage. User-visible behavior changes should be documented in `CHANGELOG.md`.
