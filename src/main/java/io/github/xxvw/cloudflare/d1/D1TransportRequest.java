package io.github.xxvw.cloudflare.d1;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable HTTP request passed to a {@link D1Transport}.
 */
public final class D1TransportRequest {
  private final URI uri;
  private final String method;
  private final Map<String, String> headers;
  private final String body;
  private final Duration timeout;

  /**
   * Creates a transport request.
   *
   * @param uri request URI
   * @param method HTTP method
   * @param headers request headers
   * @param body request body
   * @param timeout request timeout
   */
  public D1TransportRequest(
      URI uri,
      String method,
      Map<String, String> headers,
      String body,
      Duration timeout) {
    this.uri = Objects.requireNonNull(uri, "uri must not be null");
    this.method = requireText(method, "method");
    this.headers = headers == null
        ? Collections.<String, String>emptyMap()
        : Collections.unmodifiableMap(new LinkedHashMap<>(headers));
    this.body = body == null ? "" : body;
    this.timeout = Objects.requireNonNull(timeout, "timeout must not be null");
  }

  /**
   * Request URI.
   *
   * @return request URI
   */
  public URI uri() {
    return uri;
  }

  /**
   * HTTP method.
   *
   * @return HTTP method
   */
  public String method() {
    return method;
  }

  /**
   * Request headers.
   *
   * @return immutable request headers
   */
  public Map<String, String> headers() {
    return headers;
  }

  /**
   * Request body.
   *
   * @return request body
   */
  public String body() {
    return body;
  }

  /**
   * Request timeout.
   *
   * @return request timeout
   */
  public Duration timeout() {
    return timeout;
  }

  private static String requireText(String value, String name) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(name + " must not be null or blank");
    }
    return value;
  }
}
