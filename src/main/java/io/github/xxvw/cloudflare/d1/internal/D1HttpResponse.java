package io.github.xxvw.cloudflare.d1.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class D1HttpResponse {
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final String body;

  public D1HttpResponse(int statusCode, Map<String, List<String>> headers, String body) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }

  public int statusCode() {
    return statusCode;
  }

  public Map<String, List<String>> headers() {
    return headers;
  }

  public String body() {
    return body;
  }

  public String firstHeader(String name) {
    if (name == null) {
      return null;
    }
    String expected = name.toLowerCase(Locale.ROOT);
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getKey() != null
          && entry.getKey().toLowerCase(Locale.ROOT).equals(expected)
          && !entry.getValue().isEmpty()) {
        return entry.getValue().get(0);
      }
    }
    return null;
  }
}
