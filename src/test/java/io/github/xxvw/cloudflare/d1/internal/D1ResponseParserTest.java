package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.D1ResponseInfo;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ApiResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class D1ResponseParserTest {
  private final D1ResponseParser parser = new D1ResponseParser(new D1JsonMapper(null));

  @Test
  void parsesSingleResultRowsMetaAndAggregatedMessages() throws Exception {
    String body = "{\"success\":true,"
        + "\"messages\":[{\"code\":1,\"message\":\"top\"}],"
        + "\"errors\":[],"
        + "\"result\":[{\"success\":true,"
        + "\"results\":[{\"id\":1,\"name\":\"Taro\"}],"
        + "\"meta\":{\"rows_read\":1,\"duration\":2.5},"
        + "\"messages\":[{\"code\":2,\"message\":\"item\"}]}]}";
    D1ApiResponseDto response = parser.parseApiResponse(body);

    D1Result result = parser.parseSingle(response, body);

    assertThat(result.success()).isTrue();
    assertThat(result.rows()).hasSize(1);
    assertThat(result.firstRow().get()).containsEntry("id", 1).containsEntry("name", "Taro");
    assertThat(result.meta().rowsRead()).isEqualTo(1);
    assertThat(result.meta().duration()).isEqualTo(2.5);
    assertThat(result.messages()).extracting(D1ResponseInfo::code).containsExactly(1, 2);
    assertThat(result.rawBody()).isEqualTo(body);
  }

  @Test
  void parseSingleUsesSafeDefaultsWhenResultIsMissing() throws Exception {
    String body = "{\"success\":true,\"errors\":[],\"messages\":[]}";
    D1ApiResponseDto response = parser.parseApiResponse(body);

    D1Result result = parser.parseSingle(response, body);

    assertThat(result.rows()).isEmpty();
    assertThat(result.meta().rowsRead()).isZero();
    assertThat(result.errors()).isEmpty();
  }

  @Test
  void parsesBatchItemsIndividuallyAndReturnsImmutableList() throws Exception {
    String body = "{\"success\":true,\"errors\":[],\"messages\":[],\"result\":["
        + "{\"success\":true,\"results\":[{\"value\":1}],\"meta\":{}},"
        + "{\"success\":false,\"results\":[],\"meta\":{},"
        + "\"errors\":[{\"code\":7500,\"message\":\"failed\"}]}]}";
    D1ApiResponseDto response = parser.parseApiResponse(body);

    List<D1Result> results = parser.parseBatch(response, body);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).success()).isTrue();
    assertThat(results.get(0).rows()).hasSize(1);
    assertThat(results.get(1).success()).isFalse();
    assertThat(results.get(1).errors()).extracting(D1ResponseInfo::code).containsExactly(7500);
    assertThatThrownBy(() -> results.add(results.get(0)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void parseBatchReturnsEmptyListWhenResultIsMissing() throws Exception {
    String body = "{\"success\":true,\"errors\":[],\"messages\":[]}";
    D1ApiResponseDto response = parser.parseApiResponse(body);

    assertThat(parser.parseBatch(response, body)).isEmpty();
  }
}
