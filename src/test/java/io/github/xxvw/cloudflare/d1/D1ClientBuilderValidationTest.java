package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class D1ClientBuilderValidationTest {

  @Test
  void rejectsBlankRequiredValues() {
    assertThatThrownBy(() -> D1Client.builder().accountId(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("accountId");
    assertThatThrownBy(() -> D1Client.builder().databaseId(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("databaseId");
    assertThatThrownBy(() -> D1Client.builder().apiToken(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("apiToken");
  }

  @Test
  void rejectsMissingRequiredValuesAtBuildTime() {
    assertThatThrownBy(() -> D1Client.builder()
        .accountId("test-account-id")
        .apiToken("test-token")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("databaseId");
    assertThatThrownBy(() -> D1Client.builder()
        .accountId("test-account-id")
        .databaseId("test-database-id")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("apiToken");
  }

  @Test
  void rejectsNullOrBlankBaseUrls() {
    assertThatThrownBy(() -> D1Client.builder().baseUrl((String) null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.builder().baseUrl(" "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.builder().baseUrl((URI) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void rejectsInvalidTimeoutsAndNullOptionalSettings() {
    assertThatThrownBy(() -> D1Client.builder().connectTimeout(Duration.ofSeconds(-1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.builder().requestTimeout(Duration.ofSeconds(-1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.builder().connectTimeout(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1Client.builder().requestTimeout(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1Client.builder().userAgent(" "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Client.builder().retryPolicy(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1Client.builder().transport(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> D1Client.builder().objectMapper(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void asyncBuilderDelegatesValidationToTheClientBuilder() {
    assertThatThrownBy(() -> D1AsyncClient.builder().databaseId(" "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1AsyncClient.builder().apiToken(" "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1AsyncClient.builder().connectTimeout(Duration.ofSeconds(-1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1AsyncClient.builder().executor(null))
        .isInstanceOf(NullPointerException.class);
  }
}
