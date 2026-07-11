package io.github.xxvw.cloudflare.d1.testsupport;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared row helpers for asserting query results.
 */
public final class TestRows {
  private TestRows() {}

  public static Map<String, Object> row(Object... values) {
    Map<String, Object> row = new LinkedHashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      row.put((String) values[i], values[i + 1]);
    }
    return row;
  }
}
