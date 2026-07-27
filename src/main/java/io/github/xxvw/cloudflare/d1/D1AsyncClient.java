package io.github.xxvw.cloudflare.d1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Asynchronous client for the Cloudflare D1 REST API.
 *
 * <p>This client executes the same operations as {@link D1Client} on a configured {@link Executor}
 * and returns {@link CompletableFuture} values. Failed requests complete futures exceptionally with
 * the same public exception types as synchronous operations.
 *
 * <p>Query operations ({@code queryAsync}, {@code queryFirstAsync}, {@code rawAsync}) are retried
 * by default and must be used for reads only: a retried statement can reach the server more than
 * once (at-least-once semantics). Run writes through {@code executeAsync} or {@code batchAsync},
 * which do not retry by default. See {@link D1RetryPolicy}.
 *
 * <pre>{@code
 * try (D1AsyncClient d1 = D1AsyncClient.fromEnv()) {
 *   CompletableFuture<D1Result> future =
 *       d1.queryAsync("SELECT id, name FROM users WHERE active = ?", true);
 *   D1Result result = future.join();
 *   System.out.println(result.rows());
 * }
 * }</pre>
 */
public final class D1AsyncClient implements AutoCloseable {
  private static final String CLOSED_MESSAGE = "D1AsyncClient is closed";

  private final D1Client delegate;
  private final Executor executor;
  private final ExecutorService ownedExecutor;
  private final Object lifecycleLock = new Object();

  private boolean closed;
  private boolean delegateCloseStarted;
  private long pendingOperations;

  D1AsyncClient(D1Client delegate, Executor executor) {
    this(delegate, executor, null);
  }

  D1AsyncClient(D1Client delegate, Executor executor, ExecutorService ownedExecutor) {
    this.delegate = delegate;
    this.executor = executor;
    this.ownedExecutor = ownedExecutor;
  }

  /**
   * Creates a builder for configuring an async D1 client.
   *
   * @return a new async client builder
   */
  public static D1AsyncClientBuilder builder() {
    return new D1AsyncClientBuilder();
  }

  /**
   * Creates an async builder initialized from the standard environment variables.
   *
   * <p>The required variables are {@code CLOUDFLARE_ACCOUNT_ID}, {@code D1_DATABASE_ID}, and
   * {@code CLOUDFLARE_API_TOKEN}. Optional settings such as timeouts, retries, transports, typed
   * row mapping, and the executor can be customized on the returned builder before calling {@link
   * D1AsyncClientBuilder#build()}.
   *
   * @return an async client builder initialized from environment variables
   * @throws IllegalStateException if any required variable is missing or blank
   */
  public static D1AsyncClientBuilder builderFromEnv() {
    return builderFromEnv(System::getenv);
  }

  static D1AsyncClientBuilder builderFromEnv(Function<String, String> env) {
    return new D1AsyncClientBuilder(D1Client.builderFromEnv(env));
  }

  /**
   * Creates an async client from the standard environment variables.
   *
   * <p>The required variables are {@code CLOUDFLARE_ACCOUNT_ID}, {@code D1_DATABASE_ID}, and
   * {@code CLOUDFLARE_API_TOKEN}. This is equivalent to {@code builderFromEnv().build()}.
   *
   * @return an async client configured from environment variables
   * @throws IllegalStateException if any required variable is missing or blank
   */
  public static D1AsyncClient fromEnv() {
    return builderFromEnv().build();
  }

  static D1AsyncClient fromEnv(Function<String, String> env) {
    return builderFromEnv(env).build();
  }

  /**
   * Runs a SQL query without positional parameters.
   *
   * @param sql SQL text to execute
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> queryAsync(String sql) {
    return queryAsync(D1Query.of(sql));
  }

  /**
   * Runs a SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> queryAsync(String sql, Object... params) {
    return queryAsync(D1Query.of(sql, params));
  }

  /**
   * Runs a SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> queryAsync(String sql, List<?> params) {
    return queryAsync(D1Query.of(sql, params));
  }

  /**
   * Runs a prepared D1 query.
   *
   * @param query query object
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> queryAsync(D1Query query) {
    return supply(() -> delegate.query(query));
  }

  /**
   * Runs a query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param <T> target row type
   * @return future completed with immutable mapped rows
   */
  public <T> CompletableFuture<List<T>> queryAsync(String sql, Class<T> type) {
    return queryAsync(D1Query.of(sql), type);
  }

  /**
   * Runs a parameterized query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return future completed with immutable mapped rows
   */
  public <T> CompletableFuture<List<T>> queryAsync(String sql, Class<T> type, Object... params) {
    return queryAsync(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return future completed with immutable mapped rows
   */
  public <T> CompletableFuture<List<T>> queryAsync(String sql, Class<T> type, List<?> params) {
    return queryAsync(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @param type target row type
   * @param <T> target row type
   * @return future completed with immutable mapped rows
   * @deprecated Use {@link #queryAsync(String, Class, List)}, which matches the varargs parameter
   *     order; scheduled for removal in 0.3.0.
   */
  @Deprecated
  public <T> CompletableFuture<List<T>> queryAsync(String sql, List<?> params, Class<T> type) {
    return queryAsync(D1Query.of(sql, params), type);
  }

  /**
   * Runs a prepared query and maps every returned row to the requested type.
   *
   * @param query query object
   * @param type target row type
   * @param <T> target row type
   * @return future completed with immutable mapped rows
   */
  public <T> CompletableFuture<List<T>> queryAsync(D1Query query, Class<T> type) {
    return supply(() -> delegate.query(query, type));
  }

  /**
   * Runs a query and returns the first row, if present.
   *
   * @param sql SQL text to execute
   * @return future completed with the first row or empty
   */
  public CompletableFuture<Optional<Map<String, Object>>> queryFirstAsync(String sql) {
    return queryFirstAsync(D1Query.of(sql));
  }

  /**
   * Runs a parameterized query and returns the first row, if present.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the first row or empty
   */
  public CompletableFuture<Optional<Map<String, Object>>> queryFirstAsync(String sql, Object... params) {
    return queryFirstAsync(D1Query.of(sql, params));
  }

  /**
   * Runs a parameterized query and returns the first row, if present.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the first row or empty
   */
  public CompletableFuture<Optional<Map<String, Object>>> queryFirstAsync(String sql, List<?> params) {
    return queryFirstAsync(D1Query.of(sql, params));
  }

  /**
   * Runs a prepared query and returns the first row, if present.
   *
   * @param query query object
   * @return future completed with the first row or empty
   */
  public CompletableFuture<Optional<Map<String, Object>>> queryFirstAsync(D1Query query) {
    return supply(() -> delegate.queryFirst(query));
  }

  /**
   * Runs a query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param <T> target row type
   * @return future completed with the mapped first row or empty
   */
  public <T> CompletableFuture<Optional<T>> queryFirstAsync(String sql, Class<T> type) {
    return queryFirstAsync(D1Query.of(sql), type);
  }

  /**
   * Runs a parameterized query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return future completed with the mapped first row or empty
   */
  public <T> CompletableFuture<Optional<T>> queryFirstAsync(String sql, Class<T> type, Object... params) {
    return queryFirstAsync(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return future completed with the mapped first row or empty
   */
  public <T> CompletableFuture<Optional<T>> queryFirstAsync(String sql, Class<T> type, List<?> params) {
    return queryFirstAsync(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @param type target row type
   * @param <T> target row type
   * @return future completed with the mapped first row or empty
   * @deprecated Use {@link #queryFirstAsync(String, Class, List)}, which matches the varargs
   *     parameter order; scheduled for removal in 0.3.0.
   */
  @Deprecated
  public <T> CompletableFuture<Optional<T>> queryFirstAsync(String sql, List<?> params, Class<T> type) {
    return queryFirstAsync(D1Query.of(sql, params), type);
  }

  /**
   * Runs a prepared query and maps the first returned row to the requested type.
   *
   * @param query query object
   * @param type target row type
   * @param <T> target row type
   * @return future completed with the mapped first row or empty
   */
  public <T> CompletableFuture<Optional<T>> queryFirstAsync(D1Query query, Class<T> type) {
    return supply(() -> delegate.queryFirst(query, type));
  }

  /**
   * Executes SQL without positional parameters.
   *
   * @param sql SQL text to execute
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> executeAsync(String sql) {
    return executeAsync(D1Query.of(sql));
  }

  /**
   * Executes SQL with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> executeAsync(String sql, Object... params) {
    return executeAsync(D1Query.of(sql, params));
  }

  /**
   * Executes SQL with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> executeAsync(String sql, List<?> params) {
    return executeAsync(D1Query.of(sql, params));
  }

  /**
   * Executes a prepared D1 query.
   *
   * @param query query object
   * @return future completed with the parsed D1 result
   */
  public CompletableFuture<D1Result> executeAsync(D1Query query) {
    return supply(() -> delegate.execute(query));
  }

  /**
   * Executes a batch of D1 queries.
   *
   * @param queries non-empty query list
   * @return future completed with immutable result items
   */
  public CompletableFuture<List<D1Result>> batchAsync(List<D1Query> queries) {
    return supply(() -> delegate.batch(queries));
  }

  /**
   * Executes a batch of D1 queries.
   *
   * @param queries non-empty query array
   * @return future completed with immutable result items
   */
  public CompletableFuture<List<D1Result>> batchAsync(D1Query... queries) {
    return supply(() -> delegate.batch(queries));
  }

  /**
   * Runs a raw SQL query without positional parameters.
   *
   * @param sql SQL text to execute
   * @return future completed with the parsed raw D1 result
   */
  public CompletableFuture<D1RawResult> rawAsync(String sql) {
    return rawAsync(D1Query.of(sql));
  }

  /**
   * Runs a raw SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the parsed raw D1 result
   */
  public CompletableFuture<D1RawResult> rawAsync(String sql, Object... params) {
    return rawAsync(D1Query.of(sql, params));
  }

  /**
   * Runs a raw SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return future completed with the parsed raw D1 result
   */
  public CompletableFuture<D1RawResult> rawAsync(String sql, List<?> params) {
    return rawAsync(D1Query.of(sql, params));
  }

  /**
   * Runs a prepared raw D1 query.
   *
   * @param query query object
   * @return future completed with the parsed raw D1 result
   */
  public CompletableFuture<D1RawResult> rawAsync(D1Query query) {
    return supply(() -> delegate.raw(query));
  }

  /**
   * Executes a raw batch of D1 queries.
   *
   * @param queries non-empty query list
   * @return future completed with immutable raw result items
   */
  public CompletableFuture<List<D1RawResult>> rawBatchAsync(List<D1Query> queries) {
    return supply(() -> delegate.rawBatch(queries));
  }

  /**
   * Executes a raw batch of D1 queries.
   *
   * @param queries non-empty query array
   * @return future completed with immutable raw result items
   */
  public CompletableFuture<List<D1RawResult>> rawBatchAsync(D1Query... queries) {
    return supply(() -> delegate.rawBatch(queries));
  }

  /**
   * Closes this client and prevents further requests.
   *
   * <p>This method does not wait for accepted operations to finish. Operations accepted before
   * close are allowed to complete, and the underlying transport is closed exactly once after the
   * final accepted operation. Operations submitted after close return futures failed with
   * {@link IllegalStateException} and the message {@code D1AsyncClient is closed}. A deferred
   * transport close failure fails the final accepted operation's future, or is suppressed by that
   * operation's failure when both fail.
   *
   * <p>When the client owns its executor (no executor was supplied to the builder), the owned
   * executor is shut down. Caller-supplied executors are never shut down.
   */
  @Override
  public void close() {
    boolean closeDelegate;
    synchronized (lifecycleLock) {
      if (closed) {
        return;
      }
      closed = true;
      if (ownedExecutor != null) {
        ownedExecutor.shutdown();
      }
      closeDelegate = beginDelegateCloseIfReady();
    }
    if (closeDelegate) {
      delegate.close();
    }
  }

  private <T> CompletableFuture<T> supply(Supplier<T> supplier) {
    CompletableFuture<T> future = new CompletableFuture<>();
    synchronized (lifecycleLock) {
      if (closed) {
        future.completeExceptionally(new IllegalStateException(CLOSED_MESSAGE));
        return future;
      }

      pendingOperations++;
    }

    try {
      executor.execute(() -> runAcceptedOperation(supplier, future));
      return future;
    } catch (RuntimeException | Error submissionFailure) {
      boolean closeDelegate;
      synchronized (lifecycleLock) {
        pendingOperations--;
        closeDelegate = beginDelegateCloseIfReady();
      }
      if (closeDelegate) {
        Throwable closeFailure = closeDelegateSafely();
        if (closeFailure != null && closeFailure != submissionFailure) {
          submissionFailure.addSuppressed(closeFailure);
        }
      }
      if (submissionFailure instanceof RejectedExecutionException) {
        future.completeExceptionally(submissionFailure);
        return future;
      }
      if (submissionFailure instanceof RuntimeException) {
        throw (RuntimeException) submissionFailure;
      }
      throw (Error) submissionFailure;
    }
  }

  private <T> void runAcceptedOperation(Supplier<T> supplier, CompletableFuture<T> future) {
    T result = null;
    Throwable operationFailure = null;
    try {
      result = supplier.get();
    } catch (Throwable failure) {
      operationFailure = failure;
    }

    Throwable closeFailure = completeAcceptedOperation();
    if (operationFailure != null) {
      if (closeFailure != null && closeFailure != operationFailure) {
        operationFailure.addSuppressed(closeFailure);
      }
      future.completeExceptionally(asCompletionException(operationFailure));
    } else if (closeFailure != null) {
      future.completeExceptionally(asCompletionException(closeFailure));
    } else {
      future.complete(result);
    }
  }

  private static CompletionException asCompletionException(Throwable failure) {
    return failure instanceof CompletionException
        ? (CompletionException) failure
        : new CompletionException(failure);
  }

  private Throwable completeAcceptedOperation() {
    boolean closeDelegate;
    synchronized (lifecycleLock) {
      pendingOperations--;
      closeDelegate = beginDelegateCloseIfReady();
    }
    if (!closeDelegate) {
      return null;
    }
    return closeDelegateSafely();
  }

  private Throwable closeDelegateSafely() {
    try {
      delegate.close();
      return null;
    } catch (Throwable failure) {
      return failure;
    }
  }

  private boolean beginDelegateCloseIfReady() {
    if (closed && pendingOperations == 0 && !delegateCloseStarted) {
      delegateCloseStarted = true;
      return true;
    }
    return false;
  }
}
