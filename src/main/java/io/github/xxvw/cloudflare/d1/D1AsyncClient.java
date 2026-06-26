package io.github.xxvw.cloudflare.d1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Asynchronous client for the Cloudflare D1 REST API.
 *
 * <p>This client executes the same operations as {@link D1Client} on a configured {@link Executor}
 * and returns {@link CompletableFuture} values. Failed requests complete futures exceptionally with
 * the same public exception types as synchronous operations.
 */
public final class D1AsyncClient implements AutoCloseable {
  private final D1Client delegate;
  private final Executor executor;

  D1AsyncClient(D1Client delegate, Executor executor) {
    this.delegate = delegate;
    this.executor = executor;
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
   * Creates an async client from the standard environment variables.
   *
   * <p>The required variables are {@code CLOUDFLARE_ACCOUNT_ID}, {@code D1_DATABASE_ID}, and
   * {@code CLOUDFLARE_API_TOKEN}.
   *
   * @return an async client configured from environment variables
   * @throws IllegalStateException if any required variable is missing or blank
   */
  public static D1AsyncClient fromEnv() {
    return fromEnv(System::getenv);
  }

  static D1AsyncClient fromEnv(Function<String, String> env) {
    return new D1AsyncClient(D1Client.fromEnv(env), D1AsyncClientBuilder.defaultExecutor());
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
   * @param params positional parameter values
   * @param type target row type
   * @param <T> target row type
   * @return future completed with immutable mapped rows
   */
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
   * @param params positional parameter values
   * @param type target row type
   * @param <T> target row type
   * @return future completed with the mapped first row or empty
   */
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
   * Marks this client as closed and prevents further requests.
   */
  @Override
  public void close() {
    delegate.close();
  }

  private <T> CompletableFuture<T> supply(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, executor);
  }
}
