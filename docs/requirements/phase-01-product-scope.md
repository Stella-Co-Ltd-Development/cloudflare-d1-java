# Phase 01: Product Scope

## Purpose

`cloudflare-d1-java` provides a lightweight Java client for the Cloudflare D1 REST API.

It is not a JDBC driver.

## Target Users

- Java developers
- Spring Boot developers
- CLI and batch application developers
- Internal tools that need low-to-medium frequency D1 access
- Developers experimenting with Cloudflare D1 outside Workers

## Non-Goals for v0.1.0

- JDBC driver support
- JPA or ORM integration
- SQL builder
- Migration framework
- Spring Boot starter
- Cloudflare Workers Binding API support
- High-throughput production data layer
- Logging framework integration

## Maven Coordinates

```xml
<groupId>io.github.xxvw</groupId>
<artifactId>cloudflare-d1-java</artifactId>
<version>0.1.0</version>
```

## Java Package

```text
io.github.xxvw.cloudflare.d1
```

## Java Version

Minimum supported Java version:

```text
Java 8 or newer
```

## Runtime Dependency

The only runtime dependency for v0.1.0 should be:

```text
jackson-databind
```
