# Phase 03: HTTP, Retry, and Validation Requirements

## Endpoint

```text
POST {baseUrl}/accounts/{accountId}/d1/database/{databaseId}/query
```

Default base URL:

```text
https://api.cloudflare.com/client/v4
```

## Headers

Required headers:

```text
Authorization: Bearer {apiToken}
Content-Type: application/json
Accept: application/json
User-Agent: cloudflare-d1-java/{version}
```

## Request Body

Single query and execute:

```json
{
  "sql": "SELECT * FROM users WHERE id = ?",
  "params": [1]
}
```

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
    }
  ]
}
```

## Validation

Rules:

- SQL must not be null.
- SQL must not be blank.
- SQL UTF-8 byte length must not exceed 100 KB.
- Params list must not be null.
- Params count must not exceed 100.
- Param values may be:
  - `String`
  - `Number`
  - `Boolean`
  - `null`

## Retry

Default retry policy:

```text
retryQuery = true
retryExecute = false
retryBatch = false
maxRetries = 2
baseDelay = 200ms
maxDelay = 2s
jitter = true
respectRetryAfter = true
retryStatusCodes = [429, 500, 502, 503, 504]
```

`execute()` and `batch()` do not retry by default.
