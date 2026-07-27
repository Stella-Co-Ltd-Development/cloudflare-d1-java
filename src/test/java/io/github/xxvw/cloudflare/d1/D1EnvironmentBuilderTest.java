package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class D1EnvironmentBuilderTest {

  @Test
  void builderFromEnvRejectsMissingAndBlankRequiredValues() {
    Map<String, String> missingAccountEnv = validEnv();
    missingAccountEnv.remove("CLOUDFLARE_ACCOUNT_ID");

    assertThatThrownBy(() -> D1Client.builderFromEnv(missingAccountEnv::get))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("CLOUDFLARE_ACCOUNT_ID environment variable must be set");

    Map<String, String> blankDatabaseEnv = validEnv();
    blankDatabaseEnv.put("D1_DATABASE_ID", " ");

    assertThatThrownBy(() -> D1AsyncClient.builderFromEnv(blankDatabaseEnv::get))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("D1_DATABASE_ID environment variable must be set");
  }

  @Test
  void syncEnvironmentBuilderAllowsTransportRetryTimeoutAndMapperConfiguration() {
    AtomicInteger calls = new AtomicInteger();
    AtomicReference<D1TransportRequest> captured = new AtomicReference<>();
    D1Transport transport = request -> {
      captured.set(request);
      if (calls.incrementAndGet() == 1) {
        return new D1TransportResponse(
            500,
            Collections.<String, List<String>>emptyMap(),
            "{\"success\":false,\"errors\":[],\"messages\":[]}");
      }
      return new D1TransportResponse(
          200,
          Collections.<String, List<String>>emptyMap(),
          successfulRows("[{\"user_id\":7}]"));
    };
    D1RetryPolicy retryPolicy = D1RetryPolicy.builder()
        .maxRetries(1)
        .baseDelay(Duration.ZERO)
        .maxDelay(Duration.ZERO)
        .jitter(false)
        .build();
    ObjectMapper mapper =
        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    try (D1Client client = D1Client.builderFromEnv(validEnv()::get)
        .baseUrl("https://example.com/client/v4")
        .connectTimeout(Duration.ofNanos(1))
        .requestTimeout(Duration.ofSeconds(12))
        .retryPolicy(retryPolicy)
        .transport(transport)
        .objectMapper(mapper)
        .build()) {
      List<EnvironmentRow> rows = client.query("SELECT user_id FROM users", EnvironmentRow.class);

      assertThat(rows).extracting(row -> row.userId).containsExactly(7);
    }

    assertThat(calls).hasValue(2);
    assertThat(captured.get().uri().toString())
        .isEqualTo(
            "https://example.com/client/v4/accounts/test-account-id/d1/database/"
                + "test-database-id/query");
    assertThat(captured.get().headers())
        .containsEntry("Authorization", "Bearer test-token");
    assertThat(captured.get().timeout()).isEqualTo(Duration.ofSeconds(12));
  }

  @Test
  void asyncEnvironmentBuilderAllowsExecutorConfiguration() throws Exception {
    AtomicInteger executions = new AtomicInteger();
    AtomicReference<D1TransportRequest> captured = new AtomicReference<>();
    Executor executor = command -> {
      executions.incrementAndGet();
      command.run();
    };
    D1Transport transport = request -> {
      captured.set(request);
      return new D1TransportResponse(
          200,
          Collections.<String, List<String>>emptyMap(),
          successfulRows("[]"));
    };

    try (D1AsyncClient client = D1AsyncClient.builderFromEnv(validEnv()::get)
        .baseUrl("https://example.com/client/v4")
        .requestTimeout(Duration.ofSeconds(9))
        .retryPolicy(D1RetryPolicy.none())
        .transport(transport)
        .executor(executor)
        .build()) {
      D1Result result = client.queryAsync("SELECT 1").get(1, TimeUnit.SECONDS);

      assertThat(result.success()).isTrue();
    }

    assertThat(executions).hasValue(1);
    assertThat(captured.get().headers())
        .containsEntry("Authorization", "Bearer test-token");
    assertThat(captured.get().timeout()).isEqualTo(Duration.ofSeconds(9));
  }

  private static Map<String, String> validEnv() {
    Map<String, String> env = new LinkedHashMap<>();
    env.put("CLOUDFLARE_ACCOUNT_ID", "test-account-id");
    env.put("D1_DATABASE_ID", "test-database-id");
    env.put("CLOUDFLARE_API_TOKEN", "test-token");
    return env;
  }

  private static String successfulRows(String rows) {
    return "{\"success\":true,\"result\":[{\"success\":true,\"results\":" + rows
        + ",\"meta\":{}}],\"errors\":[],\"messages\":[]}";
  }

  static final class EnvironmentRow {
    public int userId;
  }
}
