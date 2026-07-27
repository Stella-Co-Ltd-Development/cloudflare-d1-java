package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class D1NestedImmutabilityTest {

  @Test
  void resultRowsAreRecursivelyImmutableAndDefensivelyCopied() {
    NestedValue input = nestedValue();
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("profile", input.map);
    D1Result result = new D1Result(
        true,
        Collections.singletonList(row),
        D1Meta.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        "{}");

    input.list.add("changed");
    input.map.put("changed", true);
    row.put("changed", true);

    Map<String, Object> exposedRow = result.rows().get(0);
    Map<String, Object> exposedProfile = nestedMap(exposedRow.get("profile"));
    List<Object> exposedTags = nestedList(exposedProfile.get("tags"));

    assertThat(exposedRow).doesNotContainKey("changed");
    assertThat(exposedProfile).doesNotContainKey("changed");
    assertThat(exposedTags).containsExactly("one");
    assertThatThrownBy(() -> exposedProfile.put("added", true))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> exposedTags.add("two"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rawRowsAreRecursivelyImmutableAndDefensivelyCopied() {
    NestedValue input = nestedValue();
    List<Object> row = new ArrayList<>();
    row.add(input.map);
    D1RawResult result = new D1RawResult(
        true,
        Collections.singletonList("profile"),
        Collections.singletonList(row),
        D1Meta.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        "{}");

    input.list.add("changed");
    input.map.put("changed", true);
    row.add("changed");

    List<Object> exposedRow = result.rows().get(0);
    Map<String, Object> exposedProfile = nestedMap(exposedRow.get(0));
    List<Object> exposedTags = nestedList(exposedProfile.get("tags"));

    assertThat(exposedRow).hasSize(1);
    assertThat(exposedProfile).doesNotContainKey("changed");
    assertThat(exposedTags).containsExactly("one");
    assertThatThrownBy(() -> exposedTags.add("two"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void additionalPropertiesAreRecursivelyImmutableAndDefensivelyCopied() {
    NestedValue input = nestedValue();
    D1Meta meta = new D1Meta(
        false, 0, null, 0, 0, 0.0, null, null, null, null, null, input.map);
    D1Timings timings = new D1Timings(1.0, input.map);
    D1ResponseInfo info = new D1ResponseInfo(1, "message", null, null, input.map);
    D1ResponseSource source = new D1ResponseSource("/result", input.map);

    input.list.add("changed");
    input.map.put("changed", true);

    for (Map<String, Object> properties : Arrays.asList(
        meta.additionalProperties(),
        timings.additionalProperties(),
        info.additionalProperties(),
        source.additionalProperties())) {
      assertThat(properties).doesNotContainKey("changed");
      assertThat(nestedList(properties.get("tags"))).containsExactly("one");
      assertThatThrownBy(() -> nestedList(properties.get("tags")).add("two"))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Test
  void mappingExceptionRowIsRecursivelyImmutableAndDefensivelyCopied() {
    NestedValue input = nestedValue();
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("profile", input.map);
    D1MappingException exception =
        new D1MappingException(String.class, 2, row, new IllegalArgumentException("mapping"));

    input.list.add("changed");
    input.map.put("changed", true);
    row.put("changed", true);

    Map<String, Object> exposedRow = exception.row();
    Map<String, Object> exposedProfile = nestedMap(exposedRow.get("profile"));

    assertThat(exposedRow).doesNotContainKey("changed");
    assertThat(exposedProfile).doesNotContainKey("changed");
    assertThat(nestedList(exposedProfile.get("tags"))).containsExactly("one");
    assertThatThrownBy(() -> nestedList(exposedProfile.get("tags")).add("two"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void cyclicMapAndListValuesAreRejected() {
    Map<String, Object> cyclicMap = new LinkedHashMap<>();
    cyclicMap.put("self", cyclicMap);
    List<Object> cyclicList = new ArrayList<>();
    cyclicList.add(cyclicList);

    assertThatThrownBy(() -> new D1Result(
        true,
        Collections.singletonList(cyclicMap),
        D1Meta.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        "{}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("cyclic map/list values are not supported");
    assertThatThrownBy(() -> new D1RawResult(
        true,
        Collections.singletonList("value"),
        Collections.singletonList(cyclicList),
        D1Meta.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        "{}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("cyclic map/list values are not supported");
  }

  private static NestedValue nestedValue() {
    List<Object> list = new ArrayList<>();
    list.add("one");
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("tags", list);
    return new NestedValue(map, list);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> nestedMap(Object value) {
    return (Map<String, Object>) value;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> nestedList(Object value) {
    return (List<Object>) value;
  }

  private static final class NestedValue {
    private final Map<String, Object> map;
    private final List<Object> list;

    private NestedValue(Map<String, Object> map, List<Object> list) {
      this.map = map;
      this.list = list;
    }
  }
}
