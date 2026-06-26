# Troubleshooting

This guide covers common first-run and production failures when using `cloudflare-d1-java`.

## Authentication and Authorization

### `D1AuthenticationException`

Check that `CLOUDFLARE_API_TOKEN` is set, current, and passed to the client. If credentials are configured explicitly, confirm the value is not blank and does not include shell quoting characters.

### `D1AuthorizationException`

Check that:

- `CLOUDFLARE_ACCOUNT_ID` belongs to the account that owns the D1 database.
- `D1_DATABASE_ID` is the database ID, not the database name.
- The token has D1 permission for the target account.
- The token was created for the account scope needed by the workload.

## API and Transport Failures

### `D1RateLimitException`

Reduce request rate and respect `retryAfter()` when it is present. Queries retry transient failures by default, but callers should still avoid unbounded concurrent requests.

### `D1TimeoutException`

Check network connectivity, proxy settings, and request timeout configuration. For background jobs, consider a longer timeout than for interactive requests.

### Non-JSON API Responses

Cloudflare errors may occasionally be returned in a shape that is not the normal D1 JSON response. Treat these as API or transport failures and inspect the HTTP status code through the public exception fields.

Do not paste full production response bodies into public issues if they contain account IDs, database IDs, SQL, or customer data.

## SQL and Mapping

### `D1QueryException`

Check that the same SQL works in the Cloudflare dashboard or Wrangler. Confirm that all parameters are one of the supported parameter types: `String`, `Number`, `Boolean`, or `null`.

`D1QueryException.sql()` may expose SQL text for diagnostics. Default exception messages do not include SQL parameter values.

### `D1MappingException`

Typed mapping uses Jackson. Check that selected column names match Java field names, setters, or annotations on the target type.

Prefer:

```sql
SELECT id, name, email FROM users WHERE active = ?
```

over:

```sql
SELECT * FROM users WHERE active = ?
```

Explicit columns make mapping failures easier to diagnose.

## Empty Results

If a query succeeds but returns no rows, confirm:

- The target database ID is correct.
- The SQL statement is a query that returns rows.
- The same query returns rows in the Cloudflare dashboard or Wrangler.
- The query filters match the expected test data.

## Quick Environment Check

Run the repository quickstart after setting environment variables:

```bash
export CLOUDFLARE_ACCOUNT_ID="your-account-id"
export D1_DATABASE_ID="your-d1-database-id"
export CLOUDFLARE_API_TOKEN="your-api-token"

mvn -f examples/quickstart/pom.xml compile exec:java
```

The default query is:

```sql
SELECT 1 AS value
```
