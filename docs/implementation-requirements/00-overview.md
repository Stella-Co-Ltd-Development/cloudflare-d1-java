# 00. Overview

## Project

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

It is designed as a lightweight SDK for Java applications that need to call the D1 REST API directly.

## Goals

- Provide a simple Java API for Cloudflare D1 REST API access.
- Support query, execute, batch, typed query, and queryFirst operations.
- Use Java standard library HTTP APIs for the default transport.
- Use Jackson for JSON parsing and typed row mapping.
- Keep runtime dependencies minimal.
- Preserve Cloudflare D1 response metadata.
- Provide clear exception types for API, query, mapping, timeout, rate limit, authorization, and transport failures.
- Make the library easy to test using MockWebServer.
- Make public models immutable from the caller's perspective.

## Non-Goals for v0.1.0

- JDBC driver support
- JPA or ORM support
- SQL builder
- Migration framework
- Spring Boot starter
- Cloudflare Workers Binding API support
- High-throughput production data layer
- Logging framework integration
- Additional runtime dependencies beyond Jackson Databind

## Maven Coordinates

```xml
<groupId>io.github.xxvw</groupId>
<artifactId>cloudflare-d1-java</artifactId>
<version>0.1.0</version>
```

## Java Version

Minimum supported Java version:

```text
Java 8 or newer
```

## Package Layout

Public API package:

```text
io.github.xxvw.cloudflare.d1
```

Internal implementation package:

```text
io.github.xxvw.cloudflare.d1.internal
```

Internal DTO package:

```text
io.github.xxvw.cloudflare.d1.internal.dto
```

## Runtime Dependency Policy

The only runtime dependency for v0.1.0 should be:

```text
com.fasterxml.jackson.core:jackson-databind
```

HTTP must use:

```text
Java standard library HTTP APIs
```

Retry behavior must be implemented internally.
