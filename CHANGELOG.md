# Changelog

All notable changes to this project will be documented in this file.

This project follows Semantic Versioning.

## [Unreleased]

### Fixed

- `D1AsyncClient.close()` now allows accepted queued and in-flight operations to finish before closing the transport, rejects all post-close operations consistently, and leaves caller-supplied executors open.

## [0.2.0] - 2026-07-11

See the [migration guide](docs/guides/migration-0.2.md) for upgrade steps from 0.1.x.

### Removed

- Removed the deprecated preview async methods `D1Client.queryAsync`, `D1Client.executeAsync`, and `D1Client.batchAsync`. Use `D1AsyncClient` instead.

### Deprecated

- Deprecated `query(String, List, Class)`, `queryFirst(String, List, Class)`, and the matching async overloads in favor of new `(sql, type, params)` overloads that match the varargs parameter order; removal is planned for 0.3.0.

### Changed

- The default async executor changed from `ForkJoinPool.commonPool()` to a client-owned pool with named daemon threads; `D1AsyncClient.close()` shuts it down, while caller-supplied executors are never shut down.
- `D1Client.close()` now invokes the new `D1Transport.close()` lifecycle hook exactly once, so custom transports can release pooled connections and other resources; the default hook is a no-op and lambda transports keep working unchanged.
- Transient network failures (transport and timeout errors) are now retried with exponential backoff for operations whose retries are enabled; opt out with the new `retryNetworkErrors(false)` retry policy setting.
- Server-provided Retry-After delays are now capped by the new `maxRetryAfter` retry policy setting (default 30 seconds); `D1RateLimitException.retryAfter()` still exposes the raw server value.

### Fixed

- The default transport no longer disconnects the connection after every request, allowing HTTP keep-alive reuse; connections are torn down only on failure paths.
- Fixed exponential retry backoff overflowing for large retry attempt counts; delays are now always clamped at the configured maximum delay.
- Preserved sub-millisecond backoff delays instead of truncating them to zero during jitter calculation and sleeping.

### Documentation

- Documented that retried query operations have at-least-once semantics and must be used for reads only.
- Consolidated detailed governance documents under `docs/governance/` and stated the unofficial status more prominently.

## [0.1.4] - 2026-06-26

### Changed

- Improved README positioning, adoption guidance, compatibility notes, and troubleshooting links.
- Added compatibility, production usage, and troubleshooting guides.
- Added runnable quickstart examples for typed mapping and opt-in write operations.
- Added public Javadoc examples for common client and retry-policy usage.
- Added a runnable fake custom transport example.
- Added focused non-JSON HTTP error tests for status exposure and sanitized exception messages.
- Added the raw result model and internal raw response parsing foundation.
- Added synchronous raw query and raw batch APIs for Cloudflare D1 `/raw`.
- Added asynchronous raw query APIs and raw query documentation.

## [0.1.3] - 2026-06-26

### Changed

- Enabled automatic Maven Central publishing after release validation.
- Added a manual workflow for publishing validated Maven Central deployments.
- Replaced the shortened license file with Apache License 2.0 text for GitHub license detection.
- Added first-run quickstart example documentation and expected output.
- Added cross-agent development instruction entrypoints.
- Added supported asynchronous client APIs, documentation, tests, and runnable examples.

## [0.1.2] - 2026-06-23

### Changed

- Improved README onboarding, badges, setup guidance, and troubleshooting.
- Added focused usage guides for quick start, typed mapping, retry policy, error handling, and custom transport.
- Clarified contributor workflow, issue labels, issue templates, and beginner-friendly contribution examples.

## [0.1.1] - 2026-06-22

### Changed

- Added Java 8-compatible transport customization and default HTTP transport.
- Changed build output to Java 8-compatible bytecode.
- Added CI coverage for Java 8, 11, 15, 17, and 21.

## [0.1.0] - 2026-06-22

### Added

- Initial project governance files.
- Initial implementation requirements documentation.
- Planned Cloudflare D1 REST API Java client design.
- Implemented the v0.1.0 D1 REST API client with query, execute, batch, typed query, queryFirst, retry handling, response models, exception mapping, tests, and README usage documentation.
- Added v0.1.0 release readiness documentation and improved public API Javadocs.
