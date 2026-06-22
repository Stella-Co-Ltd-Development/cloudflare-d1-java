package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Immutable HTTP response returned by a {@link D1Transport}.
 */
public final class D1TransportResponse {
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final String body;

  /**
   * Creates a transport response.
   *
   * @param statusCode HTTP status code
   * @param headers response headers
   * @param body response body
   */
  public D1TransportResponse(int statusCode, Map<String, List<String>> headers, String body) {
    this.statusCode = statusCode;
    this.headers = immutableHeaders(headers);
    this.body = body == null ? "" : body;
  }

  /**
   * HTTP status code.
   *
   * @return HTTP status code
   */
  public int statusCode() {
    return statusCode;
  }

  /**
   * Response headers.
   *
   * @return immutable response headers
   */
  public Map<String, List<String>> headers() {
    return headers;
  }

  /**
   * Response body.
   *
   * @return response body
   */
  public String body() {
    return body;
  }

  /**
   * Returns the first response header value for a case-insensitive header name.
   *
   * @param name header name
   * @return first header value or {@code null}
   */
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

  private static Map<String, List<String>> immutableHeaders(Map<String, List<String>> headers) {
    if (headers == null || headers.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, List<String>> copy = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      List<String> values = entry.getValue() == null
          ? Collections.<String>emptyList()
          : Collections.unmodifiableList(new ArrayList<>(entry.getValue()));
      copy.put(entry.getKey(), values);
    }
    return Collections.unmodifiableMap(copy);
  }
}
