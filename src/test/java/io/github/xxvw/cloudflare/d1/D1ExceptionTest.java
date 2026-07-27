package io.github.xxvw.cloudflare.d1;

import static io.github.xxvw.cloudflare.d1.testsupport.TestRows.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xxvw.cloudflare.d1.testsupport.UserRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  @Test
  void rawBatchExceptionCopiesPartialResultsAndUsesSanitizedMessage() {
    D1ResponseInfo error =
        new D1ResponseInfo(7500, "sensitive-param", null, null, Collections.emptyMap());
    D1RawResult failed = new D1RawResult(
        false,
        Collections.<String>emptyList(),
        Collections.<List<Object>>emptyList(),
        D1Meta.empty(),
        Collections.<D1ResponseInfo>emptyList(),
        Collections.singletonList(error),
        "test-token sensitive-param");
    List<D1RawResult> partialResults = new ArrayList<>();
    partialResults.add(failed);

    D1RawBatchException exception = new D1RawBatchException(
        200,
        "test-token sensitive-param",
        Collections.singletonList(error),
        Collections.<D1ResponseInfo>emptyList(),
        0,
        partialResults);
    partialResults.clear();

    assertThat(exception.operation()).contains(D1Operation.RAW_BATCH);
    assertThat(exception.failedIndex()).isZero();
    assertThat(exception.partialResults()).containsExactly(failed);
    assertThat(exception.getMessage())
        .isEqualTo("D1 raw batch failed")
        .doesNotContain("test-token")
        .doesNotContain("sensitive-param");
    assertThatThrownBy(() -> exception.partialResults().add(failed))
        .isInstanceOf(UnsupportedOperationException.class);
  }

}
