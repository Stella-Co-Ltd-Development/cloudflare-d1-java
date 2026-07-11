package io.github.xxvw.cloudflare.d1;

import static io.github.xxvw.cloudflare.d1.testsupport.TestRows.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xxvw.cloudflare.d1.testsupport.UserRow;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class D1ExceptionTest {

  @Test
  void exceptionMetadataCollectionsAreImmutableAndMessagesDoNotExposeSensitiveValues() {
    D1ResponseInfo error = new D1ResponseInfo(7500, "failed", null, null, Collections.emptyMap());
    D1QueryException exception = new D1QueryException(
        D1Operation.QUERY,
        400,
        "{\"success\":false}",
        Collections.singletonList(error),
        Collections.emptyList(),
        "SELECT * FROM users WHERE token = ?");

    assertThat(exception.operation()).contains(D1Operation.QUERY);
    assertThat(exception.statusCode()).hasValue(400);
    assertThat(exception.rawBody()).contains("{\"success\":false}");
    assertThat(exception.sql()).contains("SELECT * FROM users WHERE token = ?");
    assertThat(exception.getMessage()).doesNotContain("SELECT * FROM users");
    assertThat(exception.getMessage()).doesNotContain("test-token");
    assertThatThrownBy(() -> exception.errors().add(error)).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> exception.messages().add(error)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void mappingExceptionRowIsImmutableAndMessageDoesNotExposeRowContents() {
    D1MappingException exception = new D1MappingException(
        UserRow.class,
        2,
        row("name", "Taro"),
        new IllegalArgumentException("bad row"));

    assertThat(exception.targetType()).isEqualTo(UserRow.class);
    assertThat(exception.rowIndex()).isEqualTo(2);
    assertThat(exception.row()).containsEntry("name", "Taro");
    assertThat(exception.getMessage()).doesNotContain("Taro");
    assertThatThrownBy(() -> exception.row().put("id", 1)).isInstanceOf(UnsupportedOperationException.class);
  }

}
