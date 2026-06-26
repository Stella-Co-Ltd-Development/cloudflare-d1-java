package io.github.xxvw.cloudflare.d1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Builder for {@link D1AsyncClient}.
 *
 * <p>Account ID, database ID, and API token are required. Optional settings provide custom
 * endpoint, timeout, retry, HTTP transport, typed row mapping behavior, and executor selection.
 */
public final class D1AsyncClientBuilder {
  private final D1ClientBuilder clientBuilder = D1Client.builder();
  private Executor executor = defaultExecutor();

  D1AsyncClientBuilder() {}

  static Executor defaultExecutor() {
    return ForkJoinPool.commonPool();
  }

  /**
   * Sets the Cloudflare account ID.
   *
   * @param accountId account ID
   * @return this builder
   */
  public D1AsyncClientBuilder accountId(String accountId) {
    clientBuilder.accountId(accountId);
    return this;
  }

  /**
   * Sets the Cloudflare D1 database ID.
   *
   * @param databaseId database ID
   * @return this builder
   */
  public D1AsyncClientBuilder databaseId(String databaseId) {
    clientBuilder.databaseId(databaseId);
    return this;
  }

  /**
   * Sets the Cloudflare API token used for Authorization.
   *
   * @param apiToken API token
   * @return this builder
   */
  public D1AsyncClientBuilder apiToken(String apiToken) {
    clientBuilder.apiToken(apiToken);
    return this;
  }

  /**
   * Sets the Cloudflare API base URL.
   *
   * @param baseUrl absolute base URL
   * @return this builder
   */
  public D1AsyncClientBuilder baseUrl(String baseUrl) {
    clientBuilder.baseUrl(baseUrl);
    return this;
  }

  /**
   * Sets the Cloudflare API base URL.
   *
   * @param baseUrl absolute base URL
   * @return this builder
   */
  public D1AsyncClientBuilder baseUrl(URI baseUrl) {
    clientBuilder.baseUrl(baseUrl);
    return this;
  }

  /**
   * Sets the User-Agent header value.
   *
   * @param userAgent User-Agent header value
   * @return this builder
   */
  public D1AsyncClientBuilder userAgent(String userAgent) {
    clientBuilder.userAgent(userAgent);
    return this;
  }

  /**
   * Sets the connection timeout for the default HTTP client.
   *
   * @param connectTimeout non-negative timeout
   * @return this builder
   */
  public D1AsyncClientBuilder connectTimeout(Duration connectTimeout) {
    clientBuilder.connectTimeout(connectTimeout);
    return this;
  }

  /**
   * Sets the per-request timeout.
   *
   * @param requestTimeout non-negative timeout
   * @return this builder
   */
  public D1AsyncClientBuilder requestTimeout(Duration requestTimeout) {
    clientBuilder.requestTimeout(requestTimeout);
    return this;
  }

  /**
   * Sets the retry policy used by client operations.
   *
   * @param retryPolicy retry policy
   * @return this builder
   */
  public D1AsyncClientBuilder retryPolicy(D1RetryPolicy retryPolicy) {
    clientBuilder.retryPolicy(retryPolicy);
    return this;
  }

  /**
   * Sets a custom HTTP transport.
   *
   * @param transport HTTP transport
   * @return this builder
   */
  public D1AsyncClientBuilder transport(D1Transport transport) {
    clientBuilder.transport(transport);
    return this;
  }

  /**
   * Sets the ObjectMapper used only for typed row mapping.
   *
   * @param objectMapper mapper for row-to-type conversion
   * @return this builder
   */
  public D1AsyncClientBuilder objectMapper(ObjectMapper objectMapper) {
    clientBuilder.objectMapper(objectMapper);
    return this;
  }

  /**
   * Sets the executor used to run asynchronous operations.
   *
   * <p>The executor is owned by the caller and is not shut down when the client is closed.
   *
   * @param executor executor for asynchronous operations
   * @return this builder
   */
  public D1AsyncClientBuilder executor(Executor executor) {
    this.executor = Objects.requireNonNull(executor, "executor must not be null");
    return this;
  }

  /**
   * Builds a configured async D1 client.
   *
   * @return configured async client
   */
  public D1AsyncClient build() {
    return new D1AsyncClient(clientBuilder.build(), executor);
  }
}
