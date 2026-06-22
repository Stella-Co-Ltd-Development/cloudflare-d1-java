package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class D1TransportModelTest {

  @Test
  void requestDefensivelyCopiesHeaders() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Accept", "application/json");

    D1TransportRequest request = new D1TransportRequest(
        URI.create("https://example.com"),
        "POST",
        headers,
        "{}",
        Duration.ofSeconds(1));
    headers.put("Accept", "text/plain");

    assertThat(request.headers()).containsEntry("Accept", "application/json");
    assertThatThrownBy(() -> request.headers().put("X-Test", "value"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void responseDefensivelyCopiesHeadersAndFindsHeadersCaseInsensitively() {
    Map<String, List<String>> headers = new LinkedHashMap<>();
    headers.put("Retry-After", Collections.singletonList("3"));

    D1TransportResponse response = new D1TransportResponse(429, headers, "{}");
    headers.clear();

    assertThat(response.firstHeader("retry-after")).isEqualTo("3");
    assertThatThrownBy(() -> response.headers().put("X-Test", Collections.singletonList("value")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> response.headers().get("Retry-After").add("4"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
