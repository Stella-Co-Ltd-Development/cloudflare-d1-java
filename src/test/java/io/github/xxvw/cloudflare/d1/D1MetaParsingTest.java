package io.github.xxvw.cloudflare.d1;

import static io.github.xxvw.cloudflare.d1.testsupport.TestResponses.ok;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class D1MetaParsingTest {
  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void parsesExtendedMetaFieldsTimingsAndMessageSource() {
    server.enqueue(ok("{\"success\":true,\"result\":[{\"success\":true,\"results\":[],"
        + "\"meta\":{\"changed_db\":true,\"changes\":2,\"last_row_id\":7,\"rows_read\":5,"
        + "\"rows_written\":2,\"duration\":1.5,\"size_after\":8192,\"served_by_colo\":\"nrt\","
        + "\"served_by_region\":\"apac\",\"served_by_primary\":true,"
        + "\"timings\":{\"sql_duration_ms\":0.42,\"extra_timing\":1}},"
        + "\"messages\":[{\"code\":100,\"message\":\"item message\","
        + "\"documentation_url\":\"https://developers.cloudflare.com/d1/\","
        + "\"source\":{\"pointer\":\"/sql\",\"extra\":\"value\"}}]}],"
        + "\"errors\":[],\"messages\":[]}"));
    D1Client client = testClient();

    D1Result result = client.query("SELECT 1");

    D1Meta meta = result.meta();
    assertThat(meta.sizeAfter()).hasValue(8192);
    assertThat(meta.servedByColo()).contains("nrt");
    assertThat(meta.servedByRegion()).contains("apac");
    assertThat(meta.servedByPrimary()).contains(true);
    assertThat(meta.timings()).isPresent();
    D1Timings timings = meta.timings().get();
    assertThat(timings.sqlDurationMs()).isEqualTo(0.42);
    assertThat(timings.additionalProperties()).containsEntry("extra_timing", 1);

    assertThat(result.messages()).hasSize(1);
    D1ResponseInfo message = result.messages().get(0);
    assertThat(message.code()).isEqualTo(100);
    assertThat(message.message()).isEqualTo("item message");
    assertThat(message.documentationUrl()).contains("https://developers.cloudflare.com/d1/");
    Optional<D1ResponseSource> source = message.source();
    assertThat(source).isPresent();
    assertThat(source.get().pointer()).contains("/sql");
    assertThat(source.get().additionalProperties()).containsEntry("extra", "value");
  }

  @Test
  void missingExtendedMetaFieldsReturnEmptyValues() {
    server.enqueue(ok("{\"success\":true,\"result\":[{\"success\":true,\"results\":[],\"meta\":{},"
        + "\"messages\":[{\"code\":1,\"message\":\"plain\"}]}],\"errors\":[],\"messages\":[]}"));
    D1Client client = testClient();

    D1Result result = client.query("SELECT 1");

    D1Meta meta = result.meta();
    assertThat(meta.sizeAfter()).isEmpty();
    assertThat(meta.servedByColo()).isEmpty();
    assertThat(meta.servedByRegion()).isEmpty();
    assertThat(meta.servedByPrimary()).isEmpty();
    assertThat(meta.timings()).isEmpty();

    D1ResponseInfo message = result.messages().get(0);
    assertThat(message.documentationUrl()).isEmpty();
    assertThat(message.source()).isEmpty();
  }

  private D1Client testClient() {
    return D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .apiToken("test-token")
        .baseUrl(server.url("/client/v4").uri())
        .retryPolicy(D1RetryPolicy.none())
        .build();
  }
}
