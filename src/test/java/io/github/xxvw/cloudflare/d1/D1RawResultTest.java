package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class D1RawResultTest {

  @Test
  void rawResultCollectionsAreImmutableAndExposeRowCount() {
    D1RawResult result = new D1RawResult(
        true,
        Arrays.asList("id", "name"),
        Collections.singletonList(Arrays.<Object>asList(1, "Taro")),
        D1Meta.empty(),
        Collections.singletonList(new D1ResponseInfo(1, "message", null, null, Collections.emptyMap())),
        Collections.singletonList(new D1ResponseInfo(2, "error", null, null, Collections.emptyMap())),
        "{}");

    assertThat(result.success()).isTrue();
    assertThat(result.isEmpty()).isFalse();
    assertThat(result.rowCount()).isEqualTo(1);
    assertThat(result.columns()).containsExactly("id", "name");
    assertThat(result.rows()).containsExactly(Arrays.<Object>asList(1, "Taro"));
    assertThatThrownBy(() -> result.columns().add("email")).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.rows().add(Collections.emptyList())).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.rows().get(0).add("extra")).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.messages().add(null)).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.errors().add(null)).isInstanceOf(UnsupportedOperationException.class);
  }
}
