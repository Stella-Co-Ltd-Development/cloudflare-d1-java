package io.github.xxvw.cloudflare.d1;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable SQL statement and positional parameter list.
 *
 * <p>Parameters may be strings, numbers, booleans, or null values. Other types should be converted
 * by callers before creating a query.
 */
public final class D1Query {
  private static final int MAX_SQL_BYTES = 100 * 1024;
  private static final int MAX_PARAMS = 100;

  private final String sql;
  private final List<Object> params;

  private D1Query(String sql, List<?> params) {
    this.sql = validateSql(sql);
    this.params = validateParams(params);
  }

  /**
   * Creates a query without positional parameters.
   *
   * @param sql SQL text
   * @return query object
   */
  public static D1Query of(String sql) {
    return new D1Query(sql, Collections.emptyList());
  }

  /**
   * Creates a query with positional parameters.
   *
   * @param sql SQL text
   * @param params positional parameter values
   * @return query object
   */
  public static D1Query of(String sql, Object... params) {
    Objects.requireNonNull(params, "params must not be null");
    return new D1Query(sql, Arrays.asList(params));
  }

  /**
   * Creates a query with positional parameters.
   *
   * @param sql SQL text
   * @param params positional parameter values
   * @return query object
   */
  public static D1Query of(String sql, List<?> params) {
    return new D1Query(sql, params);
  }

  /**
   * Returns the SQL text.
   *
   * @return SQL text
   */
  public String sql() {
    return sql;
  }

  /**
   * Returns immutable positional parameter values.
   *
   * @return immutable parameter list
   */
  public List<Object> params() {
    return params;
  }

  private static String validateSql(String sql) {
    if (sql == null) {
      throw new IllegalArgumentException("sql must not be null");
    }
    if (sql.trim().isEmpty()) {
      throw new IllegalArgumentException("sql must not be blank");
    }
    if (sql.getBytes(StandardCharsets.UTF_8).length > MAX_SQL_BYTES) {
      throw new IllegalArgumentException("sql must not exceed 100 KB encoded as UTF-8");
    }
    return sql;
  }

  private static List<Object> validateParams(List<?> params) {
    if (params == null) {
      throw new IllegalArgumentException("params must not be null");
    }
    if (params.size() > MAX_PARAMS) {
      throw new IllegalArgumentException("params count must not exceed 100");
    }
    List<Object> copy = new ArrayList<>(params.size());
    for (Object param : params) {
      if (param != null
          && !(param instanceof String)
          && !(param instanceof Number)
          && !(param instanceof Boolean)) {
        throw new IllegalArgumentException("unsupported D1 parameter type: " + param.getClass().getName());
      }
      copy.add(param);
    }
    return Collections.unmodifiableList(copy);
  }
}
