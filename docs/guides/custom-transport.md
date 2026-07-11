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

## Resource Lifecycle

A client owns its transport: `D1Client.close()` calls `D1Transport.close()` exactly once. Override the default no-op `close()` to release resources held by your HTTP stack, such as connection pools or executor threads.

```java
D1Transport transport = new D1Transport() {
  @Override
  public D1TransportResponse send(D1TransportRequest request) throws IOException {
    // Execute the request with your HTTP client.
    return new D1TransportResponse(200, Collections.emptyMap(), "{\"success\":true,\"result\":[]}");
  }

  @Override
  public void close() {
    // Release connection pools, executors, or other resources here.
  }
};
```

When one transport instance is shared across multiple clients, manage its lifecycle externally and keep `close()` a no-op. Lambda transports keep working unchanged and inherit the no-op `close()`.

## Runnable Fake Transport Example

The quickstart module includes an example that inspects the generated request and returns a fake D1
response without contacting Cloudflare:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.CustomTransportExample
```

The example uses fake credential values and does not print authorization headers.
