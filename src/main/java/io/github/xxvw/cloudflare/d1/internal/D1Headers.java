package io.github.xxvw.cloudflare.d1.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class D1Headers {
  private D1Headers() {}

  public static String firstHeader(Map<String, List<String>> headers, String name) {
    if (name == null) {
      return null;
    }
    String expected = name.toLowerCase(Locale.ROOT);
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getKey() != null
          && entry.getKey().toLowerCase(Locale.ROOT).equals(expected)
          && !entry.getValue().isEmpty()) {
        return entry.getValue().get(0);
      }
    }
    return null;
  }
}
