# Custom Transport

The default transport uses Java standard library HTTP APIs. Use `D1Transport` when your application needs a different HTTP stack, proxy behavior, or test transport.

```java
D1Transport transport = request -> {
  // Execute request.method(), request.uri(), request.headers(), request.body(),
  // and request.timeout() with your HTTP client.
  return new D1TransportResponse(200, Collections.emptyMap(), "{\"success\":true,\"result\":[]}");
};

D1Client client = D1Client.builder()
    .accountId(System.getenv("CLOUDFLARE_ACCOUNT_ID"))
    .databaseId(System.getenv("D1_DATABASE_ID"))
    .apiToken(System.getenv("CLOUDFLARE_API_TOKEN"))
    .transport(transport)
    .build();
```

## Requirements

- Return the HTTP status code, response headers, and response body exactly enough for the client to parse D1 responses.
- Preserve the `Authorization`, `Content-Type`, `Accept`, and `User-Agent` headers from the request.
- Respect `request.timeout()` where your HTTP stack supports it.
- Do not log API tokens or authorization headers.

The custom transport does not change JSON parsing, retry behavior, or exception mapping.
