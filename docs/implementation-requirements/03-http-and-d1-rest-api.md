# 03. HTTP and Cloudflare D1 REST API Requirements

## Endpoint

The D1 REST API endpoint used by this library is:

```text
POST {baseUrl}/accounts/{accountId}/d1/database/{databaseId}/query
```

Default base URL:

```text
https://api.cloudflare.com/client/v4
```

Rules:

- `baseUrl` may be configured as `String`.
- `baseUrl` may be configured as `URI`.
- `baseUrl` with or without a trailing slash must work.
- `accountId` and `databaseId` must be path-segment encoded.
- Do not validate `databaseId` as UUID.
- Null or blank endpoint values must be rejected.

## HTTP Client

Use Java standard library HTTP APIs for the default transport:

```java
java.net.HttpURLConnection
```

Rules:

- A custom `D1Transport` can be provided by `D1ClientBuilder`.
- Request timeout is applied per request.
- Connect timeout is used when the default client is created.
- No third-party HTTP client dependency is allowed in v0.1.0.

## Headers

Required request headers:

```text
Authorization: Bearer {apiToken}
Content-Type: application/json
Accept: application/json
User-Agent: cloudflare-d1-java/{version}
```

Rules:

- API token must never be logged.
- API token must never appear in exception messages.
- Authorization header must never be exposed in public API.

## Request Body

Single query and execute:

```json
{
  "sql": "SELECT * FROM users WHERE id = ?",
  "params": [1]
}
```

Params must always be present.

No params:

```json
{
  "sql": "SELECT * FROM users",
  "params": []
}
```

Batch:

```json
{
  "batch": [
    {
      "sql": "INSERT INTO users(name) VALUES (?)",
      "params": ["Taro"]
    },
    {
      "sql": "SELECT * FROM users",
      "params": []
    }
  ]
}
```

## Multi-Statement SQL

Cloudflare D1 may accept SQL containing semicolon-separated statements. The SDK must not reject semicolons by default.

However, for predictable behavior, documentation should recommend `batch(...)` for multiple operations.

## Response Body

The raw response body must be preserved as `rawBody()`.

Use `rawBody()` rather than `rawJson()` because error responses may not always be JSON.

## Status Handling

Rules:

- HTTP 2xx with API `success=true` may be successful.
- HTTP 2xx with top-level `success=false` must throw a D1 exception.
- HTTP 4xx/5xx with parseable JSON must attach `errors` and `messages` to the exception.
- HTTP 4xx/5xx with non-JSON body must throw `D1ApiException` and preserve `rawBody()`.
- JSON parse failures in error responses should result in `D1ApiException`.
