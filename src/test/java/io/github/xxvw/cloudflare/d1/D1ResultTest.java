package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class D1ResultTest {

  @Test
  void resultCollectionsAreImmutableAndPreserveNullValues() {
    D1Result result = new D1Result(
        true,
        Collections.singletonList(row("id", 1)),
        D1Meta.empty(),
        Collections.singletonList(new D1ResponseInfo(1, "message", null, null, Collections.emptyMap())),
        Collections.singletonList(new D1ResponseInfo(2, "error", null, null, Collections.emptyMap())),
        "{}");

    assertThatThrownBy(() -> result.rows().add(Collections.emptyMap())).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.rows().get(0).put("name", "Taro"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.messages().add(null)).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.errors().add(null)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rowHelpersExposeEmptyAndFirstRow() {
    D1Result empty = new D1Result(true, Collections.emptyList(), D1Meta.empty(), Collections.emptyList(), Collections.emptyList(), "{}");
    D1Result nonEmpty = new D1Result(
        true,
        Arrays.asList(row("id", 1), row("id", 2)),
        D1Meta.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        "{}");

    assertThat(empty.isEmpty()).isTrue();
    assertThat(empty.rowCount()).isZero();
    assertThat(empty.firstRow()).isEmpty();
    assertThat(nonEmpty.isEmpty()).isFalse();
    assertThat(nonEmpty.rowCount()).isEqualTo(2);
    assertThat(nonEmpty.firstRow()).contains(row("id", 1));
  }

  @Test
  void emptyMetaHasSafeDefaultsAndAdditionalPropertiesAreImmutable() {
    D1Meta meta = new D1Meta(
        false, 0, null, 0, 0, 0.0, null, null, null, null, null, row("unknown", "value"));

    assertThat(D1Meta.empty().changedDb()).isFalse();
    assertThat(D1Meta.empty().lastRowId()).isEmpty();
    assertThat(meta.additionalProperties()).containsEntry("unknown", "value");
    assertThatThrownBy(() -> meta.additionalProperties().put("x", "y"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  private static Map<String, Object> row(String name, Object value) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put(name, value);
    return row;
  }
}
