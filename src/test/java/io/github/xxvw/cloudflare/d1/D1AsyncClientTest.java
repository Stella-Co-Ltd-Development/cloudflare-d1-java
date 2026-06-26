package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class D1AsyncClientTest {
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
    assertThatThrownBy(() -> D1AsyncClient.builder()
        .databaseId("test-database-id")
        .apiToken("test-token")
        .build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1AsyncClient.builder().executor(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1AsyncClient.fromEnv(name -> " "))
        .isInstanceOf(IllegalStateException.class);

    Map<String, String> env = new LinkedHashMap<>();
    env.put("CLOUDFLARE_ACCOUNT_ID", "test-account-id");
    env.put("D1_DATABASE_ID", "test-database-id");
    env.put("CLOUDFLARE_API_TOKEN", "test-token");

    D1AsyncClient client = D1AsyncClient.fromEnv(env::get);
    client.close();
  }

  @Test
  void queryAsyncUsesConfiguredExecutorAndSendsExpectedRequest() throws Exception {
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\"}]", metaBody())));
    AtomicInteger executions = new AtomicInteger();
    Executor directExecutor = command -> {
      executions.incrementAndGet();
      command.run();
    };
    D1AsyncClient client = testClient(directExecutor);

    D1Result result = client.queryAsync("SELECT * FROM users WHERE id = ?", 1).get(1, TimeUnit.SECONDS);
    RecordedRequest request = server.takeRequest();

    assertThat(executions).hasValue(1);
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getTarget()).isEqualTo("/client/v4/accounts/test-account-id/d1/database/test-database-id/query");
    assertThat(request.getHeaders().get("Authorization")).isEqualTo("Bearer test-token");
    assertThat(request.getBody().utf8()).isEqualTo("{\"sql\":\"SELECT * FROM users WHERE id = ?\",\"params\":[1]}");
    assertThat(result.rows()).containsExactly(row("id", 1, "name", "Taro"));
  }

  @Test
  void queryFirstAndTypedQueryAsyncReturnMappedResults() throws Exception {
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\"},{\"id\":2,\"name\":\"Jiro\"}]", metaBody())));
    server.enqueue(ok(selectBody("[{\"id\":3,\"name\":\"Saburo\"}]", metaBody())));
    server.enqueue(ok(selectBody("[{\"id\":4,\"name\":\"Shiro\"}]", metaBody())));
    D1AsyncClient client = testClient();

    Optional<Map<String, Object>> first =
        client.queryFirstAsync("SELECT id, name FROM users").get(1, TimeUnit.SECONDS);
    List<UserRow> rows =
        client.queryAsync("SELECT id, name FROM users WHERE active = ?", UserRow.class, true)
            .get(1, TimeUnit.SECONDS);
    Optional<UserRow> typedFirst =
        client.queryFirstAsync(D1Query.of("SELECT id, name FROM users LIMIT 1"), UserRow.class)
            .get(1, TimeUnit.SECONDS);

    assertThat(first).contains(row("id", 1, "name", "Taro"));
    assertThat(rows).containsExactly(new UserRow(3, "Saburo"));
    assertThat(typedFirst).contains(new UserRow(4, "Shiro"));
  }

  @Test
  void executeAndBatchAsyncReturnResults() throws Exception {
    server.enqueue(ok(selectBody("[]",
        "{\"changed_db\":true,\"changes\":1,\"last_row_id\":42,\"rows_read\":0,\"rows_written\":1,\"duration\":2.5}")));
    server.enqueue(ok("{\"success\":true,\"result\":["
        + "{\"success\":true,\"results\":[{\"value\":1}],\"meta\":{}},"
        + "{\"success\":true,\"results\":[{\"value\":2}],\"meta\":{}}"
        + "],\"errors\":[],\"messages\":[]}"));
    D1AsyncClient client = testClient();

    D1Result executeResult =
        client.executeAsync(D1Query.of("INSERT INTO users(name) VALUES (?)", "Taro")).get(1, TimeUnit.SECONDS);
    List<D1Result> batchResults =
        client.batchAsync(Arrays.asList(D1Query.of("SELECT 1 AS value"), D1Query.of("SELECT 2 AS value")))
            .get(1, TimeUnit.SECONDS);

    assertThat(executeResult.meta().changes()).isEqualTo(1);
    assertThat(executeResult.meta().lastRowId()).hasValue(42);
    assertThat(batchResults).hasSize(2);
    assertThat(batchResults.get(0).rows()).containsExactly(row("value", 1));
    assertThat(batchResults.get(1).rows()).containsExactly(row("value", 2));
  }

  @Test
  void asyncFailuresCompleteExceptionallyWithPublicExceptions() {
    server.enqueue(jsonError(401));
    server.enqueue(ok(selectBody("[{\"id\":\"not-a-number\",\"name\":\"Taro\"}]", metaBody())));
    D1AsyncClient client = testClient();
    D1AsyncClient timeoutClient = D1AsyncClient.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl("https://example.com/client/v4")
        .transport(request -> {
          throw new SocketTimeoutException("timed out");
        })
        .retryPolicy(D1RetryPolicy.none())
        .executor(Runnable::run)
        .build();

    CompletableFuture<D1Result> authenticationFailure = client.queryAsync("SELECT 1");
    CompletableFuture<List<UserRow>> mappingFailure = client.queryAsync("SELECT id, name FROM users", UserRow.class);
    CompletableFuture<D1Result> timeoutFailure = timeoutClient.queryAsync("SELECT 1");

    assertThatThrownBy(authenticationFailure::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1AuthenticationException.class);
    assertThatThrownBy(mappingFailure::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1MappingException.class);
    assertThatThrownBy(timeoutFailure::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1TimeoutException.class);
  }

  @Test
  void closePreventsFutureAsyncRequests() {
    D1AsyncClient client = testClient();
    client.close();

    assertThatThrownBy(() -> client.queryAsync("SELECT 1").join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(IllegalStateException.class);
  }

  private D1AsyncClient testClient() {
    return testClient(Runnable::run);
  }

  private D1AsyncClient testClient(Executor executor) {
    return D1AsyncClient.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(server.url("/client/v4").uri())
        .retryPolicy(D1RetryPolicy.none())
        .executor(executor)
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

  private static Map<String, Object> row(Object... values) {
    Map<String, Object> row = new LinkedHashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      row.put((String) values[i], values[i + 1]);
    }
    return row;
  }

  public static final class UserRow {
    public long id;
    public String name;

    public UserRow() {}

    UserRow(long id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof UserRow)) {
        return false;
      }
      UserRow userRow = (UserRow) other;
      return id == userRow.id && java.util.Objects.equals(name, userRow.name);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(id, name);
    }
  }
}
