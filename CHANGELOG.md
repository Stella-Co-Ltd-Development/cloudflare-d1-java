# Changelog

All notable changes to this project will be documented in this file.

This project follows Semantic Versioning.

## [Unreleased]

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
