package io.github.xxvw.cloudflare.d1;

import static io.github.xxvw.cloudflare.d1.testsupport.TestResponses.jsonError;
import static io.github.xxvw.cloudflare.d1.testsupport.TestResponses.metaBody;
import static io.github.xxvw.cloudflare.d1.testsupport.TestResponses.ok;
import static io.github.xxvw.cloudflare.d1.testsupport.TestResponses.rawBody;
import static io.github.xxvw.cloudflare.d1.testsupport.TestResponses.selectBody;
import static io.github.xxvw.cloudflare.d1.testsupport.TestRows.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xxvw.cloudflare.d1.testsupport.UserRow;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
  void typedListOverloadsUseTheVarargsParameterOrder() throws Exception {
    server.enqueue(ok(selectBody("[{\"id\":5,\"name\":\"Goro\"}]", metaBody())));
    server.enqueue(ok(selectBody("[{\"id\":6,\"name\":\"Roku\"}]", metaBody())));
    D1AsyncClient client = testClient();

    List<UserRow> rows =
        client.queryAsync("SELECT 1", UserRow.class, Collections.singletonList(5)).get(1, TimeUnit.SECONDS);
    Optional<UserRow> first =
        client.queryFirstAsync("SELECT 1", UserRow.class, Collections.singletonList(6)).get(1, TimeUnit.SECONDS);

    assertThat(rows).containsExactly(new UserRow(5, "Goro"));
    assertThat(first).contains(new UserRow(6, "Roku"));
    assertThat(server.takeRequest().getBody().utf8()).isEqualTo("{\"sql\":\"SELECT 1\",\"params\":[5]}");
    assertThat(server.takeRequest().getBody().utf8()).isEqualTo("{\"sql\":\"SELECT 1\",\"params\":[6]}");
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
  void rawAndRawBatchAsyncReturnResults() throws Exception {
    server.enqueue(ok(rawBody("[\"value\"]", "[[1]]", metaBody())));
    server.enqueue(ok("{\"success\":true,\"result\":["
        + "{\"success\":true,\"results\":{\"columns\":[\"left\"],\"rows\":[[10]]},\"meta\":{}},"
        + "{\"success\":true,\"results\":{\"columns\":[\"right\"],\"rows\":[[20]]},\"meta\":{}}"
        + "],\"errors\":[],\"messages\":[]}"));
    D1AsyncClient client = testClient();

    D1RawResult raw = client.rawAsync("SELECT ? AS value", 1).get(1, TimeUnit.SECONDS);
    List<D1RawResult> batch =
        client.rawBatchAsync(D1Query.of("SELECT 10 AS left"), D1Query.of("SELECT 20 AS right"))
            .get(1, TimeUnit.SECONDS);

    assertThat(raw.columns()).containsExactly("value");
    assertThat(raw.rows()).containsExactly(Collections.<Object>singletonList(1));
    assertThat(batch).hasSize(2);
    assertThat(batch.get(0).columns()).containsExactly("left");
    assertThat(batch.get(1).rows()).containsExactly(Collections.<Object>singletonList(20));
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
  void rawAsyncFailuresCompleteExceptionallyWithPublicExceptions() {
    server.enqueue(jsonError(401));
    D1AsyncClient client = testClient();

    CompletableFuture<D1RawResult> authenticationFailure = client.rawAsync("SELECT 1");

    assertThatThrownBy(authenticationFailure::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1AuthenticationException.class);
  }

  @Test
  void asyncQueryRetriesRetryableStatusesAndSucceeds() throws Exception {
    server.enqueue(new mockwebserver3.MockResponse.Builder()
        .code(429)
        .setHeader("Retry-After", "0")
        .body("{\"success\":false}")
        .build());
    server.enqueue(ok(selectBody("[{\"id\":1,\"name\":\"Taro\"}]", metaBody())));
    D1RetryPolicy policy = D1RetryPolicy.builder()
        .maxRetries(1)
        .baseDelay(Duration.ZERO)
        .maxDelay(Duration.ZERO)
        .jitter(false)
        .build();
    D1AsyncClient client = D1AsyncClient.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(server.url("/client/v4").uri())
        .retryPolicy(policy)
        .executor(Runnable::run)
        .build();

    D1Result result = client.queryAsync("SELECT 1").get(1, TimeUnit.SECONDS);

    assertThat(result.rowCount()).isEqualTo(1);
    assertThat(server.getRequestCount()).isEqualTo(2);
  }

  @Test
  void asyncBatchPartialFailurePropagatesBatchExceptionDetails() {
    server.enqueue(ok("{\"success\":true,\"result\":["
        + "{\"success\":true,\"results\":[],\"meta\":{\"changes\":1}},"
        + "{\"success\":false,\"results\":[],\"meta\":{},\"errors\":[{\"code\":7500,\"message\":\"failed\"}]}"
        + "],\"errors\":[],\"messages\":[]}"));
    D1AsyncClient client = testClient();

    CompletableFuture<List<D1Result>> future = client.batchAsync(Arrays.asList(
        D1Query.of("INSERT INTO users(name) VALUES (?)", "Taro"),
        D1Query.of("SELECT * FROM missing")));

    assertThatThrownBy(future::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1BatchException.class);
    D1BatchException cause = (D1BatchException) future.handle((value, error) -> error.getCause()).join();
    assertThat(cause.failedIndex()).isEqualTo(1);
    assertThat(cause.partialResults()).hasSize(2);
    assertThat(cause.errors()).extracting(D1ResponseInfo::code).containsExactly(7500);
  }

  @Test
  void executeAndRawBatchAsyncFailuresPropagatePublicExceptions() {
    server.enqueue(jsonError(500));
    server.enqueue(ok("{\"success\":true,\"result\":[{\"success\":false,"
        + "\"results\":{\"columns\":[],\"rows\":[]},\"errors\":[{\"code\":3,\"message\":\"batch\"}]}],"
        + "\"errors\":[],\"messages\":[]}"));
    D1AsyncClient client = testClient();

    assertThatThrownBy(() -> client.executeAsync("UPDATE users SET active = ?", false).join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1ApiException.class);
    assertThatThrownBy(() -> client.rawBatchAsync(D1Query.of("SELECT 1")).join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(D1ApiException.class);
  }

  @Test
  void closeAllowsAcceptedQueuedOperationsToFinishBeforeClosingTheTransport() throws Exception {
    ManualExecutor executor = new ManualExecutor();
    AtomicInteger closes = new AtomicInteger();
    D1AsyncClient client = lifecycleClient(executor, successfulTransport(closes));

    CompletableFuture<D1Result> firstAccepted = client.queryAsync("SELECT 1");
    CompletableFuture<D1Result> lastAccepted = client.queryAsync("SELECT 2");

    assertThat(executor.hasQueuedTask()).isTrue();
    assertThat(firstAccepted).isNotDone();
    assertThat(lastAccepted).isNotDone();

    client.close();
    client.close();

    assertThat(firstAccepted).isNotDone();
    assertThat(lastAccepted).isNotDone();
    assertThat(closes).hasValue(0);
    assertClosedFailure(client.queryAsync("SELECT 3"));

    executor.runQueuedTask();

    assertThat(firstAccepted.get(1, TimeUnit.SECONDS).success()).isTrue();
    assertThat(lastAccepted).isNotDone();
    assertThat(closes).hasValue(0);

    executor.runQueuedTask();

    assertThat(lastAccepted.get(1, TimeUnit.SECONDS).success()).isTrue();
    assertThat(closes).hasValue(1);
  }

  @Test
  void closeDoesNotWaitForExecutorAcceptanceInProgress() throws Exception {
    BlockingAcceptanceExecutor executor = new BlockingAcceptanceExecutor();
    AtomicInteger closes = new AtomicInteger();
    D1AsyncClient client = lifecycleClient(executor, successfulTransport(closes));
    ExecutorService callers = Executors.newFixedThreadPool(2);
    try {
      CompletableFuture<CompletableFuture<D1Result>> submitting =
          CompletableFuture.supplyAsync(() -> client.queryAsync("SELECT 1"), callers);
      assertThat(executor.awaitAcceptance()).isTrue();

      CountDownLatch closeInvoked = new CountDownLatch(1);
      CompletableFuture<Void> closing = CompletableFuture.runAsync(() -> {
        closeInvoked.countDown();
        client.close();
      }, callers);
      assertThat(closeInvoked.await(1, TimeUnit.SECONDS)).isTrue();
      closing.get(1, TimeUnit.SECONDS);
      assertThat(closes).hasValue(0);

      executor.finishAcceptance();

      CompletableFuture<D1Result> accepted = submitting.get(1, TimeUnit.SECONDS);
      assertThat(accepted).isNotDone();
      assertThat(closes).hasValue(0);

      executor.runAcceptedTask();

      assertThat(accepted.get(1, TimeUnit.SECONDS).success()).isTrue();
      assertThat(closes).hasValue(1);
    } finally {
      executor.finishAcceptance();
      callers.shutdownNow();
    }
  }

  @Test
  void closeFailsAllOperationFamiliesConsistentlyWithCallerSuppliedExecutor() {
    D1AsyncClient client = testClient();
    client.close();

    assertClosedFailure(client.queryAsync("SELECT 1"));
    assertClosedFailure(client.queryFirstAsync("SELECT 1"));
    assertClosedFailure(client.executeAsync("UPDATE users SET active = ?", false));
    assertClosedFailure(client.batchAsync(D1Query.of("SELECT 1")));
    assertClosedFailure(client.rawAsync("SELECT 1"));
    assertClosedFailure(client.rawBatchAsync(D1Query.of("SELECT 1")));
  }

  @Test
  void defaultExecutorFinishesInFlightWorkAndIsShutDownOnClose() throws Exception {
    AtomicReference<Thread> worker = new AtomicReference<>();
    AtomicInteger closes = new AtomicInteger();
    CountDownLatch requestStarted = new CountDownLatch(1);
    CountDownLatch finishRequest = new CountDownLatch(1);
    D1AsyncClient client = D1AsyncClient.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl("https://example.com/client/v4")
        .transport(new D1Transport() {
          @Override
          public D1TransportResponse send(D1TransportRequest request) throws IOException {
            worker.set(Thread.currentThread());
            requestStarted.countDown();
            try {
              finishRequest.await();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new IOException("interrupted", e);
            }
            return new D1TransportResponse(200, Collections.emptyMap(), selectBody("[]", metaBody()));
          }

          @Override
          public void close() {
            closes.incrementAndGet();
          }
        })
        .retryPolicy(D1RetryPolicy.none())
        .build();

    try {
      CompletableFuture<D1Result> accepted = client.queryAsync("SELECT 1");
      assertThat(requestStarted.await(1, TimeUnit.SECONDS)).isTrue();

      assertThat(worker.get().getName()).startsWith("cloudflare-d1-async-");
      assertThat(worker.get().isDaemon()).isTrue();

      client.close();

      assertThat(accepted).isNotDone();
      assertThat(closes).hasValue(0);
      assertClosedFailure(client.queryAsync("SELECT 2"));

      finishRequest.countDown();

      assertThat(accepted.get(1, TimeUnit.SECONDS).success()).isTrue();
      assertThat(closes).hasValue(1);
    } finally {
      finishRequest.countDown();
      client.close();
    }
  }

  @Test
  void rejectingCallerExecutorFailsWithRejectedExecutionExceptionWhileClientIsOpen() {
    AtomicInteger closes = new AtomicInteger();
    Executor rejectingExecutor = command -> {
      throw new RejectedExecutionException("executor rejected the operation");
    };
    D1AsyncClient client = lifecycleClient(rejectingExecutor, successfulTransport(closes));

    assertThatThrownBy(() -> client.queryAsync("SELECT 1").join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(RejectedExecutionException.class)
        .hasRootCauseMessage("executor rejected the operation");

    client.close();
    assertThat(closes).hasValue(1);
  }

  @Test
  void deferredTransportCloseFailureFailsTheLastAcceptedFuture() {
    ManualExecutor executor = new ManualExecutor();
    IllegalStateException closeFailure = new IllegalStateException("transport close failed");
    D1Transport transport = new D1Transport() {
      @Override
      public D1TransportResponse send(D1TransportRequest request) {
        return new D1TransportResponse(200, Collections.emptyMap(), selectBody("[]", metaBody()));
      }

      @Override
      public void close() {
        throw closeFailure;
      }
    };
    D1AsyncClient client = lifecycleClient(executor, transport);

    CompletableFuture<D1Result> accepted = client.queryAsync("SELECT 1");
    client.close();
    executor.runQueuedTask();

    assertThatThrownBy(accepted::join)
        .isInstanceOf(CompletionException.class)
        .hasCause(closeFailure);
  }

  @Test
  void operationCompletionExceptionIsNotWrappedAgain() {
    ManualExecutor executor = new ManualExecutor();
    CompletionException operationFailure =
        new CompletionException(new IllegalStateException("operation failed"));
    D1Transport transport = request -> {
      throw operationFailure;
    };
    D1AsyncClient client = lifecycleClient(executor, transport);

    CompletableFuture<D1Result> accepted = client.queryAsync("SELECT 1");
    executor.runQueuedTask();

    assertThatThrownBy(accepted::join).isSameAs(operationFailure);
    client.close();
  }

  @Test
  void deferredCloseCompletionExceptionIsNotWrappedAgain() {
    ManualExecutor executor = new ManualExecutor();
    CompletionException closeFailure =
        new CompletionException(new IllegalStateException("transport close failed"));
    D1Transport transport = new D1Transport() {
      @Override
      public D1TransportResponse send(D1TransportRequest request) {
        return new D1TransportResponse(200, Collections.emptyMap(), selectBody("[]", metaBody()));
      }

      @Override
      public void close() {
        throw closeFailure;
      }
    };
    D1AsyncClient client = lifecycleClient(executor, transport);

    CompletableFuture<D1Result> accepted = client.queryAsync("SELECT 1");
    client.close();
    executor.runQueuedTask();

    assertThatThrownBy(accepted::join).isSameAs(closeFailure);
  }

  @Test
  void deferredTransportCloseFailureIsSuppressedBehindOperationFailure() {
    ManualExecutor executor = new ManualExecutor();
    IllegalStateException closeFailure = new IllegalStateException("transport close failed");
    D1Transport transport = new D1Transport() {
      @Override
      public D1TransportResponse send(D1TransportRequest request) throws IOException {
        throw new IOException("request failed");
      }

      @Override
      public void close() {
        throw closeFailure;
      }
    };
    D1AsyncClient client = lifecycleClient(executor, transport);

    CompletableFuture<D1Result> accepted = client.queryAsync("SELECT 1");
    client.close();
    executor.runQueuedTask();

    assertThatThrownBy(accepted::join)
        .isInstanceOf(CompletionException.class)
        .cause()
        .isInstanceOf(D1TransportException.class)
        .satisfies(failure -> assertThat(failure.getSuppressed()).containsExactly(closeFailure));
  }

  @Test
  void callerSuppliedExecutorIsNotShutDownOnClose() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      D1AsyncClient client = testClient(executor);
      client.close();

      assertThat(executor.isShutdown()).isFalse();
    } finally {
      executor.shutdown();
    }
  }

  private void assertClosedFailure(CompletableFuture<?> future) {
    assertThatThrownBy(future::join)
        .isInstanceOf(CompletionException.class)
        .cause()
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("D1AsyncClient is closed");
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

  private D1AsyncClient lifecycleClient(Executor executor, D1Transport transport) {
    return D1AsyncClient.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl("https://example.com/client/v4")
        .transport(transport)
        .retryPolicy(D1RetryPolicy.none())
        .executor(executor)
        .build();
  }

  private D1Transport successfulTransport(AtomicInteger closes) {
    return new D1Transport() {
      @Override
      public D1TransportResponse send(D1TransportRequest request) {
        return new D1TransportResponse(200, Collections.emptyMap(), selectBody("[]", metaBody()));
      }

      @Override
      public void close() {
        closes.incrementAndGet();
      }
    };
  }

  private static final class ManualExecutor implements Executor {
    private final Queue<Runnable> queuedTasks = new ArrayDeque<>();

    @Override
    public void execute(Runnable command) {
      queuedTasks.add(command);
    }

    boolean hasQueuedTask() {
      return !queuedTasks.isEmpty();
    }

    void runQueuedTask() {
      Runnable task = queuedTasks.poll();
      if (task == null) {
        throw new IllegalStateException("no task is queued");
      }
      task.run();
    }
  }

  private static final class BlockingAcceptanceExecutor implements Executor {
    private final CountDownLatch acceptanceStarted = new CountDownLatch(1);
    private final CountDownLatch finishAcceptance = new CountDownLatch(1);
    private final AtomicReference<Runnable> acceptedTask = new AtomicReference<>();

    @Override
    public void execute(Runnable command) {
      acceptedTask.set(command);
      acceptanceStarted.countDown();
      try {
        finishAcceptance.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RejectedExecutionException("executor acceptance was interrupted", e);
      }
    }

    boolean awaitAcceptance() throws InterruptedException {
      return acceptanceStarted.await(1, TimeUnit.SECONDS);
    }

    void finishAcceptance() {
      finishAcceptance.countDown();
    }

    void runAcceptedTask() {
      Runnable task = acceptedTask.getAndSet(null);
      if (task == null) {
        throw new IllegalStateException("no task was accepted");
      }
      task.run();
    }
  }

}
