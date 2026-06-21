# 05. JSON and Mapping Requirements

## Jackson Dependency

The library uses Jackson Databind.

Runtime dependency:

```text
com.fasterxml.jackson.core:jackson-databind
```

Do not add `jackson-datatype-jsr310` as a runtime dependency in v0.1.0.

Users who need Java time mapping can provide their own `ObjectMapper`.

## ObjectMapper Separation

Use two ObjectMapper roles:

```text
internalObjectMapper
mappingObjectMapper
```

### internalObjectMapper

Used for:

- D1 API request JSON generation
- D1 API response JSON parsing
- Internal DTO parsing

Rules:

- Owned by the library.
- Not replaceable by users.
- Must not be affected by user-provided mapping configuration.

### mappingObjectMapper

Used for:

- Mapping `rows()` to user-provided Java types.

Rules:

- Configurable through `D1ClientBuilder.objectMapper(...)`.
- Used only for typed query and typed queryFirst.
- Must not be used for internal API request/response parsing.

## Default Mapping ObjectMapper

The default mapping ObjectMapper must ignore unknown properties.

Equivalent behavior:

```java
FAIL_ON_UNKNOWN_PROPERTIES = false
```

## Internal DTOs

Internal DTOs belong in:

```text
io.github.xxvw.cloudflare.d1.internal.dto
```

Suggested DTO classes:

- `D1ApiResponseDto`
- `D1QueryResultDto`
- `D1MetaDto`
- `D1TimingsDto`
- `D1ResponseInfoDto`
- `D1ResponseSourceDto`

Rules:

- DTOs must not be public API.
- Unknown fields should be captured in additionalProperties.
- Field names should map Cloudflare snake_case fields safely.
- Do not rely on the user's mapping ObjectMapper for DTO parsing.

## Row Value Types

`D1Result.rows()` returns:

```java
List<Map<String, Object>>
```

Typical values:

```text
JSON string  -> String
JSON number  -> Integer / Long / Double
JSON boolean -> Boolean
JSON null    -> null
JSON object  -> Map
JSON array   -> List
```

Rows may contain nested `Map` or `List` values.

The outer rows list and each row map must be immutable.

Deep immutability for nested values is not required in v0.1.0.

## Typed Mapping

Example:

```java
record User(long id, String name, String email) {}

List<User> users = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    User.class,
    true
);
```

Rules:

- Unknown row properties are ignored.
- Missing required fields are handled by Jackson.
- Conversion failures are wrapped in `D1MappingException`.
- If one row fails, the entire typed query fails.
- `D1MappingException` must include target type, row index, and immutable row.
- Exception messages must not include row data by default.
