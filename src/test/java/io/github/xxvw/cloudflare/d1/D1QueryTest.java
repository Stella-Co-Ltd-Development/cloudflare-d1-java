package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class D1QueryTest {

  @Test
  void createsQueryWithoutParams() {
    D1Query query = D1Query.of("SELECT 1");

    assertThat(query.sql()).isEqualTo("SELECT 1");
    assertThat(query.params()).isEmpty();
  }

  @Test
  void createsQueryWithVarargsParamsIncludingNull() {
    D1Query query = D1Query.of("SELECT ?", "a", 1, true, (Object) null);

    assertThat(query.params()).containsExactly("a", 1, true, null);
  }

  @Test
  void defensivelyCopiesListParamsAndReturnsImmutableList() {
    List<Object> params = new ArrayList<>();
    params.add("a");

    D1Query query = D1Query.of("SELECT ?", params);
    params.add("b");

    assertThat(query.params()).containsExactly("a");
    assertThatThrownBy(() -> query.params().add("c")).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rejectsInvalidSqlAndParams() {
    assertThatThrownBy(() -> D1Query.of(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Query.of("   ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Query.of("SELECT ?", (List<?>) null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Query.of("SELECT ?", LocalDateTime.now()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Query.of("SELECT ?", Collections.singletonMap("a", "b")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Query.of("SELECT ?", (Object) Collections.singletonList("nested")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Query.of("SELECT ?", new Object()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsMoreThanOneHundredParams() {
    List<Object> params = new ArrayList<>();
    for (int i = 0; i < 101; i++) {
      params.add(i);
    }

    assertThatThrownBy(() -> D1Query.of("SELECT ?", params)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void allowsSqlExactlyOneHundredKilobytesAndRejectsLargerSql() {
    String exact = repeat("x", 100 * 1024);
    String tooLarge = exact + "x";

    assertThat(D1Query.of(exact).sql()).isEqualTo(exact);
    assertThatThrownBy(() -> D1Query.of(tooLarge)).isInstanceOf(IllegalArgumentException.class);
  }

  private static String repeat(String value, int count) {
    StringBuilder builder = new StringBuilder(value.length() * count);
    for (int i = 0; i < count; i++) {
      builder.append(value);
    }
    return builder.toString();
  }
}
