package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xxvw.cloudflare.d1.D1RawResult;
import io.github.xxvw.cloudflare.d1.D1ResponseInfo;
import io.github.xxvw.cloudflare.d1.internal.dto.D1RawApiResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class D1RawResponseParserTest {

  @Test
  void parsesRawSingleResultColumnsRowsMetaAndInfo() throws Exception {
    String body = "{\"success\":true,"
        + "\"messages\":[{\"code\":1000,\"message\":\"top message\"}],"
        + "\"errors\":[],"
        + "\"result\":[{\"success\":true,"
        + "\"results\":{\"columns\":[\"id\",\"name\"],\"rows\":[[1,\"Taro\"],[2,null]]},"
        + "\"meta\":{\"changed_db\":false,\"rows_read\":2,\"duration\":1.5},"
        + "\"messages\":[{\"code\":1001,\"message\":\"item message\"}],"
        + "\"errors\":[]}]}";
    D1ResponseParser parser = new D1ResponseParser(new D1JsonMapper(null));
    D1RawApiResponseDto response = parser.parseRawApiResponse(body);

    D1RawResult result = parser.parseRawSingle(response, body);

    assertThat(result.success()).isTrue();
    assertThat(result.columns()).containsExactly("id", "name");
    assertThat(result.rows()).containsExactly(
        java.util.Arrays.<Object>asList(1, "Taro"),
        java.util.Arrays.<Object>asList(2, null));
    assertThat(result.meta().rowsRead()).isEqualTo(2);
    assertThat(result.meta().duration()).isEqualTo(1.5);
    assertThat(result.messages()).extracting(D1ResponseInfo::code).containsExactly(1000, 1001);
    assertThat(result.errors()).isEmpty();
    assertThat(result.rawBody()).isEqualTo(body);
  }

  @Test
  void parsesRawBatchAndUsesSafeDefaultsForMissingResults() throws Exception {
    String body = "{\"success\":true,\"result\":["
        + "{\"success\":true,\"results\":{\"columns\":[\"value\"],\"rows\":[[1]]},\"meta\":{}},"
        + "{\"success\":true,\"meta\":{}}],\"errors\":[],\"messages\":[]}";
    D1ResponseParser parser = new D1ResponseParser(new D1JsonMapper(null));
    D1RawApiResponseDto response = parser.parseRawApiResponse(body);

    List<D1RawResult> results = parser.parseRawBatch(response, body);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).columns()).containsExactly("value");
    assertThat(results.get(0).rows()).containsExactly(java.util.Collections.<Object>singletonList(1));
    assertThat(results.get(1).columns()).isEmpty();
    assertThat(results.get(1).rows()).isEmpty();
    assertThatThrownBy(() -> results.add(results.get(0))).isInstanceOf(UnsupportedOperationException.class);
  }
}
