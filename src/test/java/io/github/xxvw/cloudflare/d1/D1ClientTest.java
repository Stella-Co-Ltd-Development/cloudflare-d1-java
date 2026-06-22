package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class D1ClientTest {
  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void builderRejectsMissingValuesAndFromEnvReadsRequiredValues() {
    assertThatThrownBy(() -> D1Client.builder().databaseId("test-database-id").apiToken("test-token").build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.builder().accountId(" ").databaseId("test-database-id").apiToken("test-token"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.fromEnv(name -> " "))
        .isInstanceOf(IllegalStateException.class);

    Map<String, String> env = Map.of(
        "CLOUDFLARE_ACCOUNT_ID", "test-account-id",
        "D1_DATABASE_ID", "test-database-id",
        "CLOUDFLARE_API_TOKEN", "test-token");

    D1Client client = D1Client.fromEnv(env::get);
    client.close();
  }

  @Test
  void querySendsExpectedPathHeadersAndBody() throws Exception {
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\"}]", metaBody())));
    D1Client client = testClient();

    D1Result result = client.query("SELECT * FROM users WHERE id = ?", 1);
    RecordedRequest request = server.takeRequest();

    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getTarget()).isEqualTo("/client/v4/accounts/test-account-id/d1/database/test-database-id/query");
    assertThat(request.getHeaders().get("Authorization")).isEqualTo("Bearer test-token");
    assertThat(request.getHeaders().get("Content-Type")).isEqualTo("application/json");
    assertThat(request.getHeaders().get("Accept")).isEqualTo("application/json");
    assertThat(request.getHeaders().get("User-Agent")).startsWith("cloudflare-d1-java/");
    assertThat(request.getBody().utf8()).isEqualTo("{\"sql\":\"SELECT * FROM users WHERE id = ?\",\"params\":[1]}");
    assertThat(result.rows()).hasSize(1);
    assertThat(result.firstRow()).contains(Map.of("id", 1, "name", "Taro"));
    assertThat(result.rawBody()).contains("\"success\":true");
  }

  @Test
  void queryWithoutParamsAlwaysSendsEmptyParams() throws Exception {
    server.enqueue(ok(selectBody("[]", metaBody())));
    D1Client client = testClient();

    client.query("SELECT 1");

    assertThat(server.takeRequest().getBody().utf8()).isEqualTo("{\"sql\":\"SELECT 1\",\"params\":[]}");
  }

  @Test
  void queryWithListParamsAndExecuteUseExpectedRequestShape() throws Exception {
    server.enqueue(ok(selectBody("[]", metaBody())));
    server.enqueue(ok(selectBody("[]", metaBody())));
    D1Client client = testClient();

    client.query("SELECT * FROM users WHERE name = ? AND active = ?", List.of("Taro", true));
    client.execute("UPDATE users SET active = ? WHERE id = ?", List.of(false, 1));

    assertThat(server.takeRequest().getBody().utf8())
        .isEqualTo("{\"sql\":\"SELECT * FROM users WHERE name = ? AND active = ?\",\"params\":[\"Taro\",true]}");
    assertThat(server.takeRequest().getBody().utf8())
        .isEqualTo("{\"sql\":\"UPDATE users SET active = ? WHERE id = ?\",\"params\":[false,1]}");
  }

  @Test
  void executeReturnsInsertMetadata() {
    server.enqueue(ok(selectBody("[]", """
        {"changed_db":true,"changes":1,"last_row_id":42,"rows_read":0,"rows_written":1,"duration":2.5}
        """)));
    D1Client client = testClient();

    D1Result result = client.execute("INSERT INTO users(name) VALUES (?)", "Taro");

    assertThat(result.meta().changedDb()).isTrue();
    assertThat(result.meta().changes()).isEqualTo(1);
    assertThat(result.meta().lastRowId()).hasValue(42);
    assertThat(result.meta().rowsWritten()).isEqualTo(1);
  }

  @Test
  void missingResultsAndMissingMetaUseSafeDefaultsAndUnknownMetaFieldsArePreserved() {
    server.enqueue(ok("""
        {"success":true,"result":[{"success":true,"meta":{"served_by_region":"wnam","unknown":null}}],"errors":[],"messages":[]}
        """));
    D1Client client = testClient();

    D1Result result = client.query("SELECT 1");

    assertThat(result.rows()).isEmpty();
    assertThat(result.meta().rowsRead()).isZero();
    assertThat(result.meta().servedByRegion()).contains("wnam");
    assertThat(result.meta().additionalProperties()).containsKey("unknown");
  }

  @Test
  void emptyResultArrayReturnsEmptyRowsAndInvalidJsonThrowsApiException() {
    server.enqueue(ok("{\"success\":true,\"result\":[],\"errors\":[],\"messages\":[]}"));
    server.enqueue(ok("not-json"));
    D1Client client = testClient();

    assertThat(client.query("SELECT 1").rows()).isEmpty();
    assertThatThrownBy(() -> client.query("SELECT 1"))
        .isInstanceOfSatisfying(D1ApiException.class,
            exception -> assertThat(exception.rawBody()).contains("not-json"));
  }

  @Test
  void responseMessagesAndBatchListResultsAreImmutable() {
    server.enqueue(ok("""
        {"success":true,"result":[{"success":true,"results":[],"meta":{},"messages":[{"code":10,"message":"item"}]}],
        "errors":[],"messages":[{"code":1,"message":"top"}]}
        """));
    D1Client client = testClient();

    List<D1Result> results = client.batch(List.of(D1Query.of("SELECT 1")));

    assertThat(results).hasSize(1);
    assertThat(results.get(0).messages()).extracting(D1ResponseInfo::code).containsExactly(1, 10);
    assertThatThrownBy(() -> results.add(results.get(0))).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> results.get(0).messages().add(new D1ResponseInfo(2, "x", null, null, Map.of())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void typedQueryMapsRowsAndIgnoresUnknownProperties() {
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\",\"unknown\":\"ignored\"}]", metaBody())));
    D1Client client = testClient();

    List<UserRow> rows = client.query("SELECT id, name FROM users", UserRow.class);

    assertThat(rows).containsExactly(new UserRow(1, "Taro"));
  }

  @Test
  void typedQueryMappingFailureThrowsMappingException() {
    server.enqueue(ok(selectBody("[{\"id\":\"not-a-number\",\"name\":\"Taro\"}]", metaBody())));
    D1Client client = testClient();

    assertThatThrownBy(() -> client.query("SELECT id, name FROM users", UserRow.class))
        .isInstanceOfSatisfying(D1MappingException.class, exception -> {
          assertThat(exception.targetType()).isEqualTo(UserRow.class);
          assertThat(exception.rowIndex()).isZero();
          assertThat(exception.row()).containsEntry("name", "Taro");
          assertThat(exception.getMessage()).doesNotContain("Taro");
        });
  }

  @Test
  void customMappingObjectMapperDoesNotAffectInternalResponseParsing() {
    server.enqueue(ok("""
        {"success":true,"result":[{"success":true,"results":[{"id":1,"name":"Taro","unknown":"value"}],"meta":{},"extra":"ok"}],
        "errors":[],"messages":[],"top_extra":true}
        """));
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\",\"unknown\":\"value\"}]", metaBody())));
    ObjectMapper strictMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    D1Client client = D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(server.url("/client/v4").uri())
        .retryPolicy(D1RetryPolicy.none())
        .objectMapper(strictMapper)
        .build();

    assertThat(client.query("SELECT id, name FROM users").rowCount()).isEqualTo(1);
    assertThatThrownBy(() -> client.query("SELECT id, name FROM users", UserRow.class))
        .isInstanceOf(D1MappingException.class);
  }

  @Test
  void queryFirstReturnsEmptyOrFirstRowAndTypedFirstRow() {
    server.enqueue(ok(selectBody("[]", metaBody())));
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\"},{\"id\":2,\"name\":\"Jiro\"}]", metaBody())));
    server.enqueue(ok(selectBody("[{\"id\":3,\"name\":\"Saburo\"}]", metaBody())));
    D1Client client = testClient();

    assertThat(client.queryFirst("SELECT id FROM users")).isEmpty();
    assertThat(client.queryFirst("SELECT id FROM users")).contains(Map.of("id", 1, "name", "Taro"));
    assertThat(client.queryFirst("SELECT id FROM users", UserRow.class)).contains(new UserRow(3, "Saburo"));
  }

  @Test
  void batchSendsBatchBodyAndThrowsOnPartialFailure() throws Exception {
    server.enqueue(ok("""
        {"success":true,"result":[
          {"success":true,"results":[],"meta":{"changes":1}},
          {"success":false,"results":[],"meta":{},"errors":[{"code":7500,"message":"failed"}]}
        ],"errors":[],"messages":[]}
        """));
    D1Client client = testClient();

    assertThatThrownBy(() -> client.batch(
        D1Query.of("INSERT INTO users(name) VALUES (?)", "Taro"),
        D1Query.of("SELECT * FROM missing")))
        .isInstanceOfSatisfying(D1BatchException.class, exception -> {
          assertThat(exception.failedIndex()).isEqualTo(1);
          assertThat(exception.partialResults()).hasSize(2);
        });

    assertThat(server.takeRequest().getBody().utf8())
        .isEqualTo("{\"batch\":[{\"sql\":\"INSERT INTO users(name) VALUES (?)\",\"params\":[\"Taro\"]},"
            + "{\"sql\":\"SELECT * FROM missing\",\"params\":[]}]}");
    assertThatThrownBy(() -> client.batch(List.of())).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void mapsHttpErrorsAndNonJsonErrorBodies() {
    server.enqueue(jsonError(401));
    server.enqueue(jsonError(403));
    server.enqueue(new MockResponse.Builder().code(429).setHeader("Retry-After", "3").body("{\"success\":false}").build());
    server.enqueue(jsonError(400));
    server.enqueue(jsonError(404));
    server.enqueue(jsonError(500));
    server.enqueue(new MockResponse.Builder().code(502).body("not-json").build());
    D1Client client = testClient(D1RetryPolicy.none());

    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1AuthenticationException.class);
    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1AuthorizationException.class);
    assertThatThrownBy(() -> client.query("SELECT 1"))
        .isInstanceOfSatisfying(D1RateLimitException.class,
            exception -> assertThat(exception.retryAfter()).contains(Duration.ofSeconds(3)));
    assertThatThrownBy(() -> client.query("SELECT 1"))
        .isInstanceOfSatisfying(D1QueryException.class,
            exception -> assertThat(exception.sql()).contains("SELECT 1"));
    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1ApiException.class);
    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1ApiException.class);
    assertThatThrownBy(() -> client.query("SELECT 1"))
        .isInstanceOfSatisfying(D1ApiException.class,
            exception -> assertThat(exception.rawBody()).contains("not-json"));
  }

  @Test
  void mapsTopLevelAndQueryResultFailures() {
    server.enqueue(ok("{\"success\":false,\"errors\":[{\"code\":1,\"message\":\"top\"}],\"messages\":[]}"));
    server.enqueue(ok("""
        {"success":true,"result":[{"success":false,"results":[],"errors":[{"code":2,"message":"query"}]}],"errors":[],"messages":[]}
        """));
    D1Client client = testClient();

    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1ApiException.class);
    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1QueryException.class);
  }

  @Test
  void retriesQueryButDoesNotRetryExecuteByDefault() {
    server.enqueue(new MockResponse.Builder().code(500).body("{\"success\":false}").build());
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\"}]", metaBody())));
    server.enqueue(new MockResponse.Builder().code(500).body("{\"success\":false}").build());
    server.enqueue(new MockResponse.Builder().code(500).body("{\"success\":false}").build());
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .baseDelay(Duration.ZERO)
        .maxDelay(Duration.ZERO)
        .jitter(false)
        .maxRetries(1)
        .build();
    D1Client client = testClient(policy);

    assertThat(client.query("SELECT 1").rowCount()).isEqualTo(1);
    assertThatThrownBy(() -> client.execute("UPDATE users SET name = ?", "Taro"))
        .isInstanceOf(D1ApiException.class);
    assertThatThrownBy(() -> client.batch(D1Query.of("SELECT 1")))
        .isInstanceOf(D1ApiException.class);
    assertThat(server.getRequestCount()).isEqualTo(4);
  }

  @Test
  void retryStopsAfterMaxRetriesSkipsNonRetryableStatusAndFallsBackForInvalidRetryAfter() {
    server.enqueue(new MockResponse.Builder().code(500).body("{\"success\":false}").build());
    server.enqueue(new MockResponse.Builder().code(500).body("{\"success\":false}").build());
    server.enqueue(new MockResponse.Builder().code(418).body("{\"success\":false}").build());
    server.enqueue(new MockResponse.Builder().code(500).setHeader("Retry-After", "invalid").body("{\"success\":false}").build());
    server.enqueue(ok(selectBody("[]", metaBody())));
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .baseDelay(Duration.ZERO)
        .maxDelay(Duration.ZERO)
        .jitter(false)
        .maxRetries(1)
        .build();
    D1Client client = testClient(policy);

    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1ApiException.class);
    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(D1ApiException.class);
    assertThat(client.query("SELECT 1").success()).isTrue();
    assertThat(server.getRequestCount()).isEqualTo(5);
  }

  @Test
  void executeAndBatchRetryWhenEnabledAndRetryAfterIsHonored() {
    server.enqueue(new MockResponse.Builder().code(429).setHeader("Retry-After", "0").body("{\"success\":false}").build());
    server.enqueue(ok(selectBody("[]", metaBody())));
    server.enqueue(new MockResponse.Builder().code(503).body("{\"success\":false}").build());
    server.enqueue(ok("{\"success\":true,\"result\":[{\"success\":true,\"results\":[],\"meta\":{}}],\"errors\":[],\"messages\":[]}"));
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .retryExecute(true)
        .retryBatch(true)
        .baseDelay(Duration.ZERO)
        .maxDelay(Duration.ZERO)
        .jitter(false)
        .maxRetries(1)
        .build();
    D1Client client = testClient(policy);

    assertThat(client.execute("UPDATE users SET name = ?", "Taro").success()).isTrue();
    assertThat(client.batch(D1Query.of("SELECT 1"))).hasSize(1);
    assertThat(server.getRequestCount()).isEqualTo(4);
  }

  @Test
  void mapsTransportAndTimeoutFailures() {
    D1Client transportClient = D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(URI.create("http://127.0.0.1:9"))
        .requestTimeout(Duration.ofMillis(100))
        .retryPolicy(D1RetryPolicy.none())
        .build();

    assertThatThrownBy(() -> transportClient.query("SELECT 1")).isInstanceOf(D1TransportException.class);

    D1Client timeoutClient = D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(server.url("/client/v4").uri())
        .httpClient(new TimeoutHttpClient())
        .retryPolicy(D1RetryPolicy.none())
        .build();

    assertThatThrownBy(() -> timeoutClient.query("SELECT 1")).isInstanceOf(D1TimeoutException.class);
  }

  @Test
  void closePreventsFurtherRequestsAndAsyncFailuresCompleteExceptionally() {
    D1Client client = testClient();
    client.close();

    assertThatThrownBy(() -> client.query("SELECT 1")).isInstanceOf(IllegalStateException.class);
    assertThat(client.queryAsync("SELECT 1")).failsWithin(Duration.ofSeconds(1));
  }

  private D1Client testClient() {
    return testClient(D1RetryPolicy.none());
  }

  private D1Client testClient(D1RetryPolicy retryPolicy) {
    return D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(server.url("/client/v4").uri())
        .retryPolicy(retryPolicy)
        .build();
  }

  private static MockResponse ok(String body) {
    return new MockResponse.Builder().code(200).body(body).build();
  }

  private static MockResponse jsonError(int statusCode) {
    return new MockResponse.Builder()
        .code(statusCode)
        .body("{\"success\":false,\"errors\":[{\"code\":" + statusCode + ",\"message\":\"error\"}],\"messages\":[]}")
        .build();
  }

  private static String selectBody(String rows, String meta) {
    return "{\"success\":true,\"result\":[{\"success\":true,\"results\":" + rows + ",\"meta\":" + meta
        + "}],\"errors\":[],\"messages\":[]}";
  }

  private static String metaBody() {
    return "{\"changed_db\":false,\"changes\":0,\"rows_read\":1,\"rows_written\":0,\"duration\":1.0}";
  }

  record UserRow(long id, String name) {}

  private static final class TimeoutHttpClient extends HttpClient {
    private final HttpClient delegate = HttpClient.newHttpClient();

    @Override
    public Optional<CookieHandler> cookieHandler() {
      return delegate.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
      return delegate.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
      return delegate.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
      return delegate.proxy();
    }

    @Override
    public SSLContext sslContext() {
      return delegate.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
      return delegate.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
      return delegate.authenticator();
    }

    @Override
    public Version version() {
      return delegate.version();
    }

    @Override
    public Optional<Executor> executor() {
      return delegate.executor();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException {
      throw new HttpTimeoutException("timed out");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
      return CompletableFuture.failedFuture(new HttpTimeoutException("timed out"));
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
      return CompletableFuture.failedFuture(new HttpTimeoutException("timed out"));
    }

    @Override
    public WebSocket.Builder newWebSocketBuilder() {
      return delegate.newWebSocketBuilder();
    }
  }
}
