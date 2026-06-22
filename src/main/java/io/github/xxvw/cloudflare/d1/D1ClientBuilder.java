package io.github.xxvw.cloudflare.d1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxvw.cloudflare.d1.internal.D1Endpoint;
import io.github.xxvw.cloudflare.d1.internal.D1HttpClient;
import io.github.xxvw.cloudflare.d1.internal.D1JsonMapper;
import io.github.xxvw.cloudflare.d1.internal.D1RetryExecutor;
import io.github.xxvw.cloudflare.d1.internal.D1Version;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/**
 * Builder for {@link D1Client}.
 *
 * <p>Account ID, database ID, and API token are required. Optional settings provide custom
 * endpoint, timeout, retry, HTTP client, and typed row mapping behavior.
 */
public final class D1ClientBuilder {
  private static final URI DEFAULT_BASE_URL = URI.create("https://api.cloudflare.com/client/v4");
  private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private String accountId;
  private String databaseId;
  private String apiToken;
  private URI baseUrl = DEFAULT_BASE_URL;
  private String userAgent = D1Version.DEFAULT_USER_AGENT;
  private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
  private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
  private D1RetryPolicy retryPolicy = D1RetryPolicy.defaultPolicy();
  private HttpClient httpClient;
  private ObjectMapper objectMapper;

  D1ClientBuilder() {}

  /**
   * Sets the Cloudflare account ID.
   *
   * @param accountId account ID
   * @return this builder
   */
  public D1ClientBuilder accountId(String accountId) {
    this.accountId = requireText(accountId, "accountId");
    return this;
  }

  /**
   * Sets the Cloudflare D1 database ID.
   *
   * @param databaseId database ID
   * @return this builder
   */
  public D1ClientBuilder databaseId(String databaseId) {
    this.databaseId = requireText(databaseId, "databaseId");
    return this;
  }

  /**
   * Sets the Cloudflare API token used for Authorization.
   *
   * @param apiToken API token
   * @return this builder
   */
  public D1ClientBuilder apiToken(String apiToken) {
    this.apiToken = requireText(apiToken, "apiToken");
    return this;
  }

  /**
   * Sets the Cloudflare API base URL.
   *
   * @param baseUrl absolute base URL
   * @return this builder
   */
  public D1ClientBuilder baseUrl(String baseUrl) {
    requireText(baseUrl, "baseUrl");
    this.baseUrl = URI.create(baseUrl);
    return this;
  }

  /**
   * Sets the Cloudflare API base URL.
   *
   * @param baseUrl absolute base URL
   * @return this builder
   */
  public D1ClientBuilder baseUrl(URI baseUrl) {
    this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
    return this;
  }

  /**
   * Sets the User-Agent header value.
   *
   * @param userAgent User-Agent header value
   * @return this builder
   */
  public D1ClientBuilder userAgent(String userAgent) {
    this.userAgent = requireText(userAgent, "userAgent");
    return this;
  }

  /**
   * Sets the connection timeout for the default HTTP client.
   *
   * @param connectTimeout non-negative timeout
   * @return this builder
   */
  public D1ClientBuilder connectTimeout(Duration connectTimeout) {
    this.connectTimeout = requireNonNegative(connectTimeout, "connectTimeout");
    return this;
  }

  /**
   * Sets the per-request timeout.
   *
   * @param requestTimeout non-negative timeout
   * @return this builder
   */
  public D1ClientBuilder requestTimeout(Duration requestTimeout) {
    this.requestTimeout = requireNonNegative(requestTimeout, "requestTimeout");
    return this;
  }

  /**
   * Sets the retry policy used by client operations.
   *
   * @param retryPolicy retry policy
   * @return this builder
   */
  public D1ClientBuilder retryPolicy(D1RetryPolicy retryPolicy) {
    this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
    return this;
  }

  /**
   * Sets a custom Java HTTP client.
   *
   * @param httpClient HTTP client
   * @return this builder
   */
  public D1ClientBuilder httpClient(HttpClient httpClient) {
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    return this;
  }

  /**
   * Sets the ObjectMapper used only for typed row mapping.
   *
   * @param objectMapper mapper for row-to-type conversion
   * @return this builder
   */
  public D1ClientBuilder objectMapper(ObjectMapper objectMapper) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    return this;
  }

  /**
   * Builds a configured D1 client.
   *
   * @return configured client
   */
  public D1Client build() {
    requireText(accountId, "accountId");
    requireText(databaseId, "databaseId");
    requireText(apiToken, "apiToken");
    D1JsonMapper jsonMapper = new D1JsonMapper(objectMapper);
    HttpClient client = httpClient == null
        ? HttpClient.newBuilder().connectTimeout(connectTimeout).build()
        : httpClient;
    D1Endpoint endpoint = new D1Endpoint(baseUrl, accountId, databaseId);
    D1HttpClient d1HttpClient =
        new D1HttpClient(client, endpoint, apiToken, userAgent, requestTimeout, jsonMapper);
    return new D1Client(
        d1HttpClient,
        jsonMapper,
        new io.github.xxvw.cloudflare.d1.internal.D1ResponseParser(jsonMapper),
        new D1RetryExecutor(retryPolicy));
  }

  private static String requireText(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be null or blank");
    }
    return value;
  }

  private static Duration requireNonNegative(Duration duration, String name) {
    Objects.requireNonNull(duration, name + " must not be null");
    if (duration.isNegative()) {
      throw new IllegalArgumentException(name + " must not be negative");
    }
    return duration;
  }
}
