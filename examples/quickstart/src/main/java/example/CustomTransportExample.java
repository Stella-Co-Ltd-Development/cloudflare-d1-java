package example;

import io.github.xxvw.cloudflare.d1.D1Client;
import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.D1Transport;
import io.github.xxvw.cloudflare.d1.D1TransportResponse;
import java.util.Collections;

public final class CustomTransportExample {
  private CustomTransportExample() {}

  public static void main(String[] args) {
    D1Transport transport = request -> {
      if (!"POST".equals(request.method())) {
        throw new IllegalStateException("Unexpected method: " + request.method());
      }
      if (!request.uri().getPath().endsWith("/query")) {
        throw new IllegalStateException("Unexpected path: " + request.uri().getPath());
      }
      if (!request.body().contains("SELECT 1 AS value")) {
        throw new IllegalStateException("Unexpected request body");
      }

      String body = "{\"success\":true,\"result\":[{\"success\":true,"
          + "\"results\":[{\"value\":1}],"
          + "\"meta\":{\"changed_db\":false,\"changes\":0,\"rows_read\":1,"
          + "\"rows_written\":0,\"duration\":1.0}}],"
          + "\"errors\":[],\"messages\":[]}";
      return new D1TransportResponse(200, Collections.emptyMap(), body);
    };

    try (D1Client d1 = D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .transport(transport)
        .build()) {
      D1Result result = d1.query("SELECT 1 AS value");
      System.out.println(result.firstRow().orElse(null));
    }
  }
}
