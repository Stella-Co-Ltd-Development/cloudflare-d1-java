# Typed Mapping

Typed mapping converts each D1 row map into a Java class with Jackson.

```java
public class User {
  public long id;
  public String name;
  public String email;
}

List<User> users = d1.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    User.class,
    true
);
```

Unknown row properties are ignored by the default mapper. If any row cannot be converted to the target type, the client throws `D1MappingException`.

## First Row

```java
Optional<User> user = d1.queryFirst(
    "SELECT id, name, email FROM users WHERE id = ?",
    User.class,
    1
);
```

`Optional.empty()` means D1 returned zero rows.

## Custom ObjectMapper

Use `objectMapper(...)` when your row types need custom Jackson configuration:

```java
ObjectMapper mapper = new ObjectMapper();

D1Client client = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .objectMapper(mapper)
    .build();
```

The custom mapper is used only for row-to-type mapping. Internal D1 request and response parsing remains isolated.

## Practical Notes

- Use column aliases when SQL column names do not match Java field names.
- Convert dates and JSON values to strings unless your custom mapper expects another representation.
- Prefer simple DTOs for API boundaries. The client is not an ORM and does not track entity state.
