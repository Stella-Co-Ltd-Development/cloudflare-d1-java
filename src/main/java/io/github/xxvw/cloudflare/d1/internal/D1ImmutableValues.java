package io.github.xxvw.cloudflare.d1.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates immutable defensive copies of JSON-shaped maps and lists.
 */
public final class D1ImmutableValues {
  private static final String CYCLIC_VALUES_MESSAGE =
      "cyclic map/list values are not supported";

  private D1ImmutableValues() {}

  public static Map<String, Object> immutableMap(Map<String, ?> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyMap();
    }
    return immutableStringMap(values, new IdentityHashMap<Object, Boolean>());
  }

  public static List<Object> immutableList(List<?> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    return immutableObjectList(values, new IdentityHashMap<Object, Boolean>());
  }

  private static Map<String, Object> immutableStringMap(
      Map<String, ?> values, IdentityHashMap<Object, Boolean> visiting) {
    enter(values, visiting);
    try {
      Map<String, Object> copy = new LinkedHashMap<>();
      for (Map.Entry<String, ?> entry : values.entrySet()) {
        copy.put(entry.getKey(), immutableValue(entry.getValue(), visiting));
      }
      return Collections.unmodifiableMap(copy);
    } finally {
      visiting.remove(values);
    }
  }

  private static Map<Object, Object> immutableObjectMap(
      Map<?, ?> values, IdentityHashMap<Object, Boolean> visiting) {
    enter(values, visiting);
    try {
      Map<Object, Object> copy = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : values.entrySet()) {
        copy.put(entry.getKey(), immutableValue(entry.getValue(), visiting));
      }
      return Collections.unmodifiableMap(copy);
    } finally {
      visiting.remove(values);
    }
  }

  private static List<Object> immutableObjectList(
      List<?> values, IdentityHashMap<Object, Boolean> visiting) {
    enter(values, visiting);
    try {
      List<Object> copy = new ArrayList<>(values.size());
      for (Object value : values) {
        copy.add(immutableValue(value, visiting));
      }
      return Collections.unmodifiableList(copy);
    } finally {
      visiting.remove(values);
    }
  }

  private static Object immutableValue(
      Object value, IdentityHashMap<Object, Boolean> visiting) {
    if (value instanceof Map<?, ?>) {
      return immutableObjectMap((Map<?, ?>) value, visiting);
    }
    if (value instanceof List<?>) {
      return immutableObjectList((List<?>) value, visiting);
    }
    return value;
  }

  private static void enter(Object value, IdentityHashMap<Object, Boolean> visiting) {
    if (visiting.put(value, Boolean.TRUE) != null) {
      throw new IllegalArgumentException(CYCLIC_VALUES_MESSAGE);
    }
  }
}
