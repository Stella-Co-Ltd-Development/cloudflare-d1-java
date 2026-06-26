package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;

class D1EndpointTest {

  @Test
  void buildsUrlWithAndWithoutTrailingSlash() {
    assertThat(D1Endpoint.of("https://example.com/client/v4", "account", "database").queryUri())
        .isEqualTo(URI.create("https://example.com/client/v4/accounts/account/d1/database/database/query"));
    assertThat(D1Endpoint.of("https://example.com/client/v4/", "account", "database").queryUri())
        .isEqualTo(URI.create("https://example.com/client/v4/accounts/account/d1/database/database/query"));
    assertThat(D1Endpoint.of("https://example.com/client/v4", "account", "database").rawUri())
        .isEqualTo(URI.create("https://example.com/client/v4/accounts/account/d1/database/database/raw"));
  }

  @Test
  void encodesAccountAndDatabaseAsPathSegments() {
    assertThat(D1Endpoint.of("https://example.com", "a b", "db/1").queryUri().toString())
        .isEqualTo("https://example.com/accounts/a%20b/d1/database/db%2F1/query");
  }

  @Test
  void rejectsInvalidEndpointInputs() {
    assertThatThrownBy(() -> new D1Endpoint(null, "account", "database"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Endpoint.of(" ", "account", "database"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Endpoint.of("not a uri", "account", "database"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> D1Endpoint.of("https://example.com", " ", "database"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
