package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xxvw.cloudflare.d1.D1ApiException;
import io.github.xxvw.cloudflare.d1.D1AuthenticationException;
import io.github.xxvw.cloudflare.d1.D1AuthorizationException;
import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1QueryException;
import io.github.xxvw.cloudflare.d1.D1RateLimitException;
import io.github.xxvw.cloudflare.d1.D1ResponseInfo;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class D1ExceptionFactoryTest {
  private final D1JsonMapper jsonMapper = new D1JsonMapper(null);
  private final D1ExceptionFactory factory =
      new D1ExceptionFactory(jsonMapper, new D1ResponseParser(jsonMapper));

  @Test
  void mapsKnownStatusCodesToDedicatedExceptionTypes() {
    assertThat(httpException(400, "SELECT 1"))
        .isInstanceOfSatisfying(D1QueryException.class, exception -> {
          assertThat(exception.statusCode()).hasValue(400);
          assertThat(exception.sql()).contains("SELECT 1");
        });
    assertThat(httpException(401, null)).isInstanceOf(D1AuthenticationException.class);
    assertThat(httpException(403, null)).isInstanceOf(D1AuthorizationException.class);
    assertThat(httpException(429, null)).isInstanceOf(D1RateLimitException.class);
  }

  @Test
  void mapsUnknownStatusCodesToTheBaseApiException() {
    assertThat(httpException(404, null)).isExactlyInstanceOf(D1ApiException.class);
    assertThat(httpException(409, null)).isExactlyInstanceOf(D1ApiException.class);
    assertThat(httpException(500, null))
        .isExactlyInstanceOf(D1ApiException.class)
        .hasMessage("D1 API request failed");
  }

  @Test
  void rateLimitExceptionExposesParsedRetryAfterHeader() {
    D1HttpResponse response = new D1HttpResponse(
        429,
        Collections.singletonMap("Retry-After", Collections.singletonList("3")),
        "{\"success\":false}");

    D1ApiException exception = factory.httpException(response, D1Operation.QUERY, null);

    assertThat(exception).isInstanceOfSatisfying(D1RateLimitException.class,
        rateLimit -> assertThat(rateLimit.retryAfter()).contains(Duration.ofSeconds(3)));
  }

  @Test
  void jsonErrorBodiesExposeParsedErrorsAndMessages() {
    String body = "{\"success\":false,"
        + "\"errors\":[{\"code\":7500,\"message\":\"query limit\"}],"
        + "\"messages\":[{\"code\":10,\"message\":\"info\"}]}";

    D1ApiException exception = factory.httpException(
        new D1HttpResponse(500, Collections.<String, java.util.List<String>>emptyMap(), body),
        D1Operation.QUERY,
        null);

    assertThat(exception.errors()).extracting(D1ResponseInfo::code).containsExactly(7500);
    assertThat(exception.messages()).extracting(D1ResponseInfo::code).containsExactly(10);
    assertThat(exception.rawBody()).contains(body);
  }

  @Test
  void nonJsonErrorBodiesYieldEmptyErrorsAndSanitizedMessages() {
    D1ApiException exception = factory.httpException(
        new D1HttpResponse(502, Collections.<String, java.util.List<String>>emptyMap(),
            "gateway error sensitive-value"),
        D1Operation.QUERY,
        "SELECT sensitive FROM users");

    assertThat(exception.errors()).isEmpty();
    assertThat(exception.messages()).isEmpty();
    assertThat(exception.rawBody()).contains("gateway error sensitive-value");
    assertThat(exception.getMessage())
        .doesNotContain("sensitive-value")
        .doesNotContain("SELECT sensitive");
  }

  private D1ApiException httpException(int statusCode, String sql) {
    D1HttpResponse response = new D1HttpResponse(
        statusCode,
        Collections.<String, java.util.List<String>>emptyMap(),
        "{\"success\":false,\"errors\":[],\"messages\":[]}");
    return factory.httpException(response, D1Operation.QUERY, sql);
  }
}
