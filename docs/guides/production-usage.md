# Production Usage Notes

Use `cloudflare-d1-java` for server-side tools, administration, batch jobs, and migration-style workflows that need direct access to the Cloudflare D1 REST API.

For latency-sensitive application traffic, consider putting a Cloudflare Worker in front of D1. A Worker can use the Workers D1 binding, enforce application-specific authorization, and keep database access close to Cloudflare infrastructure.

## Credentials

- Store API tokens outside source control.
- Pass credentials through environment variables, a secret manager, or your deployment platform.
- Use the minimum Cloudflare token permission needed for the workload.
- Do not log `Authorization` headers.
- Do not include account IDs, database IDs, tokens, customer data, or production SQL data in public issues.

The built-in `fromEnv()` helpers read:

```text
CLOUDFLARE_ACCOUNT_ID
D1_DATABASE_ID
CLOUDFLARE_API_TOKEN
```

## Retry Policy

Read queries retry transient failures by default. Write operations and batches do not retry by default because a repeated write may not be idempotent.

For production jobs:

- Keep default read retries unless the caller already owns retry behavior.
- Enable write retries only for statements that are safe to repeat.
- Respect `Retry-After` on rate limits.
- Use a bounded retry budget and surface failures to job monitoring.

See [Retry Policy](retry-policy.md) for configuration examples.

## Timeouts and Failure Handling

Configure timeouts based on the job type. Short interactive jobs usually need lower timeouts than background maintenance jobs.

Handle these exception categories separately:

- `D1AuthenticationException`: token is missing, malformed, expired, or invalid.
- `D1AuthorizationException`: token does not have access to the account or database.
- `D1RateLimitException`: Cloudflare rate limit was hit.
- `D1QueryException`: D1 accepted the request but the SQL failed.
- `D1MappingException`: rows could not be mapped to the requested Java type.
- `D1TransportException` or `D1TimeoutException`: network or transport failure.

Exception messages do not include API tokens or SQL parameter values. `D1QueryException.sql()` may expose SQL text for diagnostics, so treat it as sensitive when logging.

## Data Mapping

Typed mapping uses Jackson. Prefer selecting explicit column names that match the target model fields:

```sql
SELECT id, name, email FROM users WHERE active = ?
```

Avoid `SELECT *` for typed production queries. Explicit column lists make mapping behavior easier to review and safer during schema changes.
