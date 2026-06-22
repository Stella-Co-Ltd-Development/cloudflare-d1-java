package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Result of a D1 query, execute, or batch item.
 */
public final class D1Result {
  private final boolean success;
  private final List<Map<String, Object>> rows;
  private final D1Meta meta;
  private final List<D1ResponseInfo> messages;
  private final List<D1ResponseInfo> errors;
  private final String rawBody;

  public D1Result(
      boolean success,
      List<Map<String, Object>> rows,
      D1Meta meta,
      List<D1ResponseInfo> messages,
      List<D1ResponseInfo> errors,
      String rawBody) {
    this.success = success;
    this.rows = immutableRows(rows);
    this.meta = meta == null ? D1Meta.empty() : meta;
    this.messages = messages == null ? List.of() : List.copyOf(messages);
    this.errors = errors == null ? List.of() : List.copyOf(errors);
    this.rawBody = rawBody;
  }

  public boolean success() {
    return success;
  }

  public List<Map<String, Object>> rows() {
    return rows;
  }

  public D1Meta meta() {
    return meta;
  }

  public List<D1ResponseInfo> messages() {
    return messages;
  }

  public List<D1ResponseInfo> errors() {
    return errors;
  }

  public String rawBody() {
    return rawBody;
  }

  public boolean isEmpty() {
    return rows.isEmpty();
  }

  public int rowCount() {
    return rows.size();
  }

  public Optional<Map<String, Object>> firstRow() {
    return rows.stream().findFirst();
  }

  private static List<Map<String, Object>> immutableRows(List<Map<String, Object>> rows) {
    if (rows == null || rows.isEmpty()) {
      return List.of();
    }
    List<Map<String, Object>> copy = new ArrayList<>(rows.size());
    for (Map<String, Object> row : rows) {
      copy.add(Collections.unmodifiableMap(new LinkedHashMap<>(row)));
    }
    return List.copyOf(copy);
  }
}
