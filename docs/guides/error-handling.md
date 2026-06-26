# Error Handling

All client-specific exceptions extend `D1Exception`.

```java
try {
  d1.query("SELECT * FROM missing_table");
} catch (D1QueryException e) {
  e.sql().ifPresent(sql -> System.err.println("Query failed"));
} catch (D1RateLimitException e) {
  e.retryAfter().ifPresent(delay -> System.err.println("Retry after: " + delay));
} catch (D1AuthenticationException | D1AuthorizationException e) {
  System.err.println("Check API token permissions");
} catch (D1ApiException e) {
  System.err.println("D1 API error: " + e.statusCode());
} catch (D1TransportException | D1TimeoutException e) {
  System.err.println("Network failure");
}
```

## Exception Types

| Exception | Typical cause |
|---|---|
| `D1AuthenticationException` | Missing, expired, or invalid API token. |
| `D1AuthorizationException` | Token lacks permission for the account or database. |
| `D1RateLimitException` | Cloudflare returned HTTP 429. |
| `D1QueryException` | D1 accepted the request but the SQL operation failed. |
| `D1BatchException` | At least one batch item failed. |
| `D1MappingException` | Typed row mapping failed. |
| `D1TimeoutException` | Transport timed out. |
| `D1TransportException` | Network or transport failure. |
| `D1ApiException` | Other Cloudflare API failure. |

## First-Run Troubleshooting

| Symptom | Check |
|---|---|
| `D1AuthenticationException` or HTTP 401 | Confirm `CLOUDFLARE_API_TOKEN` is set and was copied without extra whitespace. |
| `D1AuthorizationException` or HTTP 403 | Confirm the token can access the account and D1 database. |
| `D1RateLimitException` or HTTP 429 | Reduce request rate and respect `retryAfter()` when it is present. |
| `D1TimeoutException` | Check network access to `https://api.cloudflare.com/client/v4` and review request timeout settings. |
| Empty rows | Confirm the SQL returns rows and that `D1_DATABASE_ID` points to the expected database. |
| Query fails in Java and dashboard | Fix the SQL or schema first, then retry from Java. |

## Sensitive Data

Exception messages do not include API tokens or SQL parameter values. `D1QueryException.sql()` may expose SQL text for diagnostics, so handle it carefully when SQL text itself is sensitive.
