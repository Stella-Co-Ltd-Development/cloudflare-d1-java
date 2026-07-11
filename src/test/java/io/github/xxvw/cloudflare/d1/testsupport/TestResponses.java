package io.github.xxvw.cloudflare.d1.testsupport;

import mockwebserver3.MockResponse;

/**
 * Shared builders for D1 API response bodies used with MockWebServer.
 */
public final class TestResponses {
  private TestResponses() {}

  public static MockResponse ok(String body) {
    return new MockResponse.Builder().code(200).body(body).build();
  }

  public static MockResponse jsonError(int statusCode) {
    return new MockResponse.Builder()
        .code(statusCode)
        .body("{\"success\":false,\"errors\":[{\"code\":" + statusCode + ",\"message\":\"error\"}],\"messages\":[]}")
        .build();
  }

  public static String selectBody(String rows, String meta) {
    return "{\"success\":true,\"result\":[{\"success\":true,\"results\":" + rows + ",\"meta\":" + meta
        + "}],\"errors\":[],\"messages\":[]}";
  }

  public static String rawBody(String columns, String rows, String meta) {
    return "{\"success\":true,\"result\":[{\"success\":true,\"results\":{\"columns\":" + columns
        + ",\"rows\":" + rows + "},\"meta\":" + meta + "}],\"errors\":[],\"messages\":[]}";
  }

  public static String metaBody() {
    return "{\"changed_db\":false,\"changes\":0,\"rows_read\":1,\"rows_written\":0,\"duration\":1.0}";
  }
}
