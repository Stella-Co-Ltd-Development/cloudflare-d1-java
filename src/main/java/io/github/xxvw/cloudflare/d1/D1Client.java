package io.github.xxvw.cloudflare.d1;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.xxvw.cloudflare.d1.internal.D1ExceptionFactory;
import io.github.xxvw.cloudflare.d1.internal.D1HttpClient;
import io.github.xxvw.cloudflare.d1.internal.D1HttpResponse;
import io.github.xxvw.cloudflare.d1.internal.D1JsonMapper;
import io.github.xxvw.cloudflare.d1.internal.D1ResponseParser;
import io.github.xxvw.cloudflare.d1.internal.D1RetryExecutor;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ApiResponseDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1RawApiResponseDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Synchronous client for the Cloudflare D1 REST API.
 *
 * <p>Create instances with {@link #builder()}, {@link #builderFromEnv()}, or {@link #fromEnv()}.
 * Instances are closeable to prevent accidental reuse after application shutdown.
 *
 * <p>Query operations ({@code query}, {@code queryFirst}, {@code raw}) are retried by default and
 * must be used for reads only: a retried statement can reach the server more than once
 * (at-least-once semantics). Run writes through {@code execute} or {@code batch}, which do not
 * retry by default. See {@link D1RetryPolicy}.
 *
 * <pre>{@code
 * try (D1Client d1 = D1Client.fromEnv()) {
 *   D1Result result = d1.query("SELECT id, name FROM users WHERE active = ?", true);
 *   result.rows().forEach(System.out::println);
 * }
 * }</pre>
 *
 * <p>Typed queries map each returned row with Jackson:
 *
 * <pre>{@code
 * List<User> users = d1.query(
 *     "SELECT id, name, email FROM users WHERE active = ?",
 *     User.class,
 *     true);
 * }</pre>
 */
public final class D1Client implements AutoCloseable {
  private static final String ENV_ACCOUNT_ID = "CLOUDFLARE_ACCOUNT_ID";
  private static final String ENV_DATABASE_ID = "D1_DATABASE_ID";
  private static final String ENV_API_TOKEN = "CLOUDFLARE_API_TOKEN";

  private final D1HttpClient httpClient;
  private final D1JsonMapper jsonMapper;
  private final D1ResponseParser responseParser;
  private final D1RetryExecutor retryExecutor;
  private final D1ExceptionFactory exceptionFactory;
  private final D1Transport transport;
  private final AtomicBoolean closed = new AtomicBoolean();

  D1Client(
      D1HttpClient httpClient,
      D1JsonMapper jsonMapper,
      D1ResponseParser responseParser,
      D1RetryExecutor retryExecutor,
      D1Transport transport) {
    this.httpClient = httpClient;
    this.jsonMapper = jsonMapper;
    this.responseParser = responseParser;
    this.retryExecutor = retryExecutor;
    this.exceptionFactory = new D1ExceptionFactory(jsonMapper, responseParser);
    this.transport = transport;
  }

  /**
   * Creates a builder for configuring a D1 client.
   *
   * @return a new client builder
   */
  public static D1ClientBuilder builder() {
    return new D1ClientBuilder();
  }

  /**
   * Creates a builder initialized from the standard environment variables.
   *
   * <p>The required variables are {@code CLOUDFLARE_ACCOUNT_ID}, {@code D1_DATABASE_ID}, and
   * {@code CLOUDFLARE_API_TOKEN}. Optional settings such as timeouts, retries, transports, and
   * typed row mapping can be customized on the returned builder before calling {@link
   * D1ClientBuilder#build()}.
   *
   * @return a client builder initialized from environment variables
   * @throws IllegalStateException if any required variable is missing or blank
   */
  public static D1ClientBuilder builderFromEnv() {
    return builderFromEnv(System::getenv);
  }

  static D1ClientBuilder builderFromEnv(Function<String, String> env) {
    Objects.requireNonNull(env, "env must not be null");
    return builder()
        .accountId(requireEnv(env, ENV_ACCOUNT_ID))
        .databaseId(requireEnv(env, ENV_DATABASE_ID))
        .apiToken(requireEnv(env, ENV_API_TOKEN));
  }

  /**
   * Creates a client from the standard environment variables.
   *
   * <p>The required variables are {@code CLOUDFLARE_ACCOUNT_ID}, {@code D1_DATABASE_ID}, and
   * {@code CLOUDFLARE_API_TOKEN}. This is equivalent to {@code builderFromEnv().build()}.
   *
   * @return a client configured from environment variables
   * @throws IllegalStateException if any required variable is missing or blank
   */
  public static D1Client fromEnv() {
    return builderFromEnv().build();
  }

  static D1Client fromEnv(Function<String, String> env) {
    return builderFromEnv(env).build();
  }

  /**
   * Runs a SQL query without positional parameters.
   *
   * @param sql SQL text to execute
   * @return parsed D1 result
   */
  public D1Result query(String sql) {
    return query(D1Query.of(sql));
  }

  /**
   * Runs a SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return parsed D1 result
   */
  public D1Result query(String sql, Object... params) {
    return query(D1Query.of(sql, params));
  }

  /**
   * Runs a SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return parsed D1 result
   */
  public D1Result query(String sql, List<?> params) {
    return query(D1Query.of(sql, params));
  }

  /**
   * Runs a prepared D1 query.
   *
   * @param query query object
   * @return parsed D1 result
   */
  public D1Result query(D1Query query) {
    return executeSingle(Objects.requireNonNull(query, "query must not be null"), D1Operation.QUERY);
  }

  /**
   * Runs a query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param <T> target row type
   * @return immutable list of mapped rows
   */
  public <T> List<T> query(String sql, Class<T> type) {
    return query(D1Query.of(sql), type);
  }

  /**
   * Runs a parameterized query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return immutable list of mapped rows
   */
  public <T> List<T> query(String sql, Class<T> type, Object... params) {
    return query(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return immutable list of mapped rows
   */
  public <T> List<T> query(String sql, Class<T> type, List<?> params) {
    return query(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps every returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @param type target row type
   * @param <T> target row type
   * @return immutable list of mapped rows
   * @deprecated Use {@link #query(String, Class, List)}, which matches the varargs parameter
   *     order; scheduled for removal in 0.3.0.
   */
  @Deprecated
  public <T> List<T> query(String sql, List<?> params, Class<T> type) {
    return query(D1Query.of(sql, params), type);
  }

  /**
   * Runs a prepared query and maps every returned row to the requested type.
   *
   * @param query query object
   * @param type target row type
   * @param <T> target row type
   * @return immutable list of mapped rows
   */
  public <T> List<T> query(D1Query query, Class<T> type) {
    Objects.requireNonNull(type, "type must not be null");
    return jsonMapper.mapRows(query(query).rows(), type);
  }

  /**
   * Runs a query and returns the first row, if present.
   *
   * @param sql SQL text to execute
   * @return first row or empty when no rows are returned
   */
  public Optional<Map<String, Object>> queryFirst(String sql) {
    return queryFirst(D1Query.of(sql));
  }

  /**
   * Runs a parameterized query and returns the first row, if present.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return first row or empty when no rows are returned
   */
  public Optional<Map<String, Object>> queryFirst(String sql, Object... params) {
    return queryFirst(D1Query.of(sql, params));
  }

  /**
   * Runs a parameterized query and returns the first row, if present.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return first row or empty when no rows are returned
   */
  public Optional<Map<String, Object>> queryFirst(String sql, List<?> params) {
    return queryFirst(D1Query.of(sql, params));
  }

  /**
   * Runs a prepared query and returns the first row, if present.
   *
   * @param query query object
   * @return first row or empty when no rows are returned
   */
  public Optional<Map<String, Object>> queryFirst(D1Query query) {
    return query(query).firstRow();
  }

  /**
   * Runs a query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param <T> target row type
   * @return mapped first row or empty when no rows are returned
   */
  public <T> Optional<T> queryFirst(String sql, Class<T> type) {
    return queryFirst(D1Query.of(sql), type);
  }

  /**
   * Runs a parameterized query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return mapped first row or empty when no rows are returned
   */
  public <T> Optional<T> queryFirst(String sql, Class<T> type, Object... params) {
    return queryFirst(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param type target row type
   * @param params positional parameter values
   * @param <T> target row type
   * @return mapped first row or empty when no rows are returned
   */
  public <T> Optional<T> queryFirst(String sql, Class<T> type, List<?> params) {
    return queryFirst(D1Query.of(sql, params), type);
  }

  /**
   * Runs a parameterized query and maps the first returned row to the requested type.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @param type target row type
   * @param <T> target row type
   * @return mapped first row or empty when no rows are returned
   * @deprecated Use {@link #queryFirst(String, Class, List)}, which matches the varargs parameter
   *     order; scheduled for removal in 0.3.0.
   */
  @Deprecated
  public <T> Optional<T> queryFirst(String sql, List<?> params, Class<T> type) {
    return queryFirst(D1Query.of(sql, params), type);
  }

  /**
   * Runs a prepared query and maps the first returned row to the requested type.
   *
   * @param query query object
   * @param type target row type
   * @param <T> target row type
   * @return mapped first row or empty when no rows are returned
   */
  public <T> Optional<T> queryFirst(D1Query query, Class<T> type) {
    Objects.requireNonNull(type, "type must not be null");
    D1Result result = query(query);
    return result.firstRow().map(row -> jsonMapper.mapRow(row, type, 0));
  }

  /**
   * Executes SQL without positional parameters.
   *
   * @param sql SQL text to execute
   * @return parsed D1 result
   */
  public D1Result execute(String sql) {
    return execute(D1Query.of(sql));
  }

  /**
   * Executes SQL with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return parsed D1 result
   */
  public D1Result execute(String sql, Object... params) {
    return execute(D1Query.of(sql, params));
  }

  /**
   * Executes SQL with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return parsed D1 result
   */
  public D1Result execute(String sql, List<?> params) {
    return execute(D1Query.of(sql, params));
  }

  /**
   * Executes a prepared D1 query.
   *
   * @param query query object
   * @return parsed D1 result
   */
  public D1Result execute(D1Query query) {
    return executeSingle(Objects.requireNonNull(query, "query must not be null"), D1Operation.EXECUTE);
  }

  /**
   * Executes a batch of D1 queries.
   *
   * @param queries non-empty query list
   * @return immutable list of result items
   */
  public List<D1Result> batch(List<D1Query> queries) {
    ensureOpen();
    List<D1Query> checked = validateBatch(queries);
    D1HttpResponse response = retryExecutor.execute(D1Operation.BATCH, () -> httpClient.sendBatch(checked));
    if (!isSuccessfulStatus(response.statusCode())) {
      throw exceptionFactory.httpException(response, D1Operation.BATCH, null);
    }
    D1ApiResponseDto apiResponse = parseApiResponse(response, D1Operation.BATCH);
    if (apiResponse.success != null && !apiResponse.success) {
      throw exceptionFactory.topLevelFailure(
          response.statusCode(), response.body(), D1Operation.BATCH, apiResponse, null);
    }
    List<D1Result> results = responseParser.parseBatch(apiResponse, response.body());
    if (results.stream().anyMatch(result -> !result.success())) {
      throw exceptionFactory.batchFailure(response.statusCode(), response.body(), results);
    }
    return results;
  }

  /**
   * Executes a batch of D1 queries.
   *
   * @param queries non-empty query array
   * @return immutable list of result items
   */
  public List<D1Result> batch(D1Query... queries) {
    Objects.requireNonNull(queries, "queries must not be null");
    return batch(Arrays.asList(queries));
  }

  /**
   * Runs a raw SQL query without positional parameters.
   *
   * @param sql SQL text to execute
   * @return parsed raw D1 result
   */
  public D1RawResult raw(String sql) {
    return raw(D1Query.of(sql));
  }

  /**
   * Runs a raw SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return parsed raw D1 result
   */
  public D1RawResult raw(String sql, Object... params) {
    return raw(D1Query.of(sql, params));
  }

  /**
   * Runs a raw SQL query with positional parameters.
   *
   * @param sql SQL text to execute
   * @param params positional parameter values
   * @return parsed raw D1 result
   */
  public D1RawResult raw(String sql, List<?> params) {
    return raw(D1Query.of(sql, params));
  }

  /**
   * Runs a prepared raw D1 query.
   *
   * @param query query object
   * @return parsed raw D1 result
   */
  public D1RawResult raw(D1Query query) {
    return executeRawSingle(Objects.requireNonNull(query, "query must not be null"));
  }

  /**
   * Executes a raw batch of D1 queries.
   *
   * @param queries non-empty query list
   * @return immutable list of raw result items
   */
  public List<D1RawResult> rawBatch(List<D1Query> queries) {
    ensureOpen();
    List<D1Query> checked = validateBatch(queries);
    D1HttpResponse response = retryExecutor.execute(D1Operation.RAW_BATCH, () -> httpClient.sendRawBatch(checked));
    if (!isSuccessfulStatus(response.statusCode())) {
      throw exceptionFactory.httpException(response, D1Operation.RAW_BATCH, null);
    }
    D1RawApiResponseDto apiResponse = parseRawApiResponse(response, D1Operation.RAW_BATCH);
    if (apiResponse.success != null && !apiResponse.success) {
      throw exceptionFactory.topLevelRawFailure(
          response.statusCode(), response.body(), D1Operation.RAW_BATCH, apiResponse, null);
    }
    List<D1RawResult> results = responseParser.parseRawBatch(apiResponse, response.body());
    if (results.stream().anyMatch(result -> !result.success())) {
      throw exceptionFactory.rawBatchFailure(response.statusCode(), response.body(), results);
    }
    return results;
  }

  /**
   * Executes a raw batch of D1 queries.
   *
   * @param queries non-empty query array
   * @return immutable list of raw result items
   */
  public List<D1RawResult> rawBatch(D1Query... queries) {
    Objects.requireNonNull(queries, "queries must not be null");
    return rawBatch(Arrays.asList(queries));
  }

  /**
   * Closes this client, prevents further requests, and closes the owned transport.
   *
   * <p>The transport {@link D1Transport#close()} hook is invoked exactly once even when this
   * method is called multiple times.
   */
  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      transport.close();
    }
  }

  private D1Result executeSingle(D1Query query, D1Operation operation) {
    ensureOpen();
    D1HttpResponse response = retryExecutor.execute(operation, () -> httpClient.sendQuery(query, operation));
    if (!isSuccessfulStatus(response.statusCode())) {
      throw exceptionFactory.httpException(response, operation, query.sql());
    }
    D1ApiResponseDto apiResponse = parseApiResponse(response, operation);
    if (apiResponse.success != null && !apiResponse.success) {
      throw exceptionFactory.topLevelFailure(
          response.statusCode(), response.body(), operation, apiResponse, query.sql());
    }
    D1Result result = responseParser.parseSingle(apiResponse, response.body());
    if (!result.success()) {
      throw exceptionFactory.queryFailure(response.statusCode(), response.body(), operation, result, query.sql());
    }
    return result;
  }

  private D1RawResult executeRawSingle(D1Query query) {
    ensureOpen();
    D1HttpResponse response = retryExecutor.execute(D1Operation.RAW, () -> httpClient.sendRawQuery(query));
    if (!isSuccessfulStatus(response.statusCode())) {
      throw exceptionFactory.httpException(response, D1Operation.RAW, query.sql());
    }
    D1RawApiResponseDto apiResponse = parseRawApiResponse(response, D1Operation.RAW);
    if (apiResponse.success != null && !apiResponse.success) {
      throw exceptionFactory.topLevelRawFailure(
          response.statusCode(), response.body(), D1Operation.RAW, apiResponse, query.sql());
    }
    D1RawResult result = responseParser.parseRawSingle(apiResponse, response.body());
    if (!result.success()) {
      throw exceptionFactory.rawFailure(response.statusCode(), response.body(), D1Operation.RAW, result, query.sql());
    }
    return result;
  }

  private D1ApiResponseDto parseApiResponse(
      D1HttpResponse response,
      D1Operation operation) {
    try {
      return responseParser.parseApiResponse(response.body());
    } catch (JsonProcessingException e) {
      throw new D1ApiException(
          "D1 API response was not valid JSON",
          operation,
          response.statusCode(),
          response.body(),
          Collections.<D1ResponseInfo>emptyList(),
          Collections.<D1ResponseInfo>emptyList());
    }
  }

  private D1RawApiResponseDto parseRawApiResponse(
      D1HttpResponse response,
      D1Operation operation) {
    try {
      return responseParser.parseRawApiResponse(response.body());
    } catch (JsonProcessingException e) {
      throw new D1ApiException(
          "D1 API response was not valid JSON",
          operation,
          response.statusCode(),
          response.body(),
          Collections.<D1ResponseInfo>emptyList(),
          Collections.<D1ResponseInfo>emptyList());
    }
  }

  private void ensureOpen() {
    if (closed.get()) {
      throw new IllegalStateException("D1Client is closed");
    }
  }

  private static boolean isSuccessfulStatus(int statusCode) {
    return statusCode >= 200 && statusCode < 300;
  }

  private static List<D1Query> validateBatch(List<D1Query> queries) {
    if (queries == null) {
      throw new IllegalArgumentException("queries must not be null");
    }
    if (queries.isEmpty()) {
      throw new IllegalArgumentException("batch must not be empty");
    }
    for (D1Query query : queries) {
      Objects.requireNonNull(query, "batch query must not be null");
    }
    return Collections.unmodifiableList(new ArrayList<>(queries));
  }

  private static String requireEnv(Function<String, String> env, String name) {
    String value = env.apply(name);
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalStateException(name + " environment variable must be set");
    }
    return value;
  }
}
