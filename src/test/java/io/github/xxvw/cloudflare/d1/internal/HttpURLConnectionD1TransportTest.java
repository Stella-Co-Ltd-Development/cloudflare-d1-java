package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xxvw.cloudflare.d1.D1TransportRequest;
import io.github.xxvw.cloudflare.d1.D1TransportResponse;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpURLConnectionD1TransportTest {
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
  void sendsRequestAndReturnsStatusHeadersAndBody() throws Exception {
    server.enqueue(new MockResponse.Builder()
        .code(200)
        .setHeader("X-Test", "value")
        .body("{\"ok\":true}")
        .build());
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(Duration.ofSeconds(5));
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");

    D1TransportResponse response = transport.send(
        request(server.url("/query").uri(), "{\"sql\":\"SELECT 1\"}", Duration.ofSeconds(10), headers));
    RecordedRequest recorded = server.takeRequest();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("{\"ok\":true}");
    assertThat(response.firstHeader("X-Test")).isEqualTo("value");
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getHeaders().get("Content-Type")).isEqualTo("application/json");
    assertThat(recorded.getBody().utf8()).isEqualTo("{\"sql\":\"SELECT 1\"}");
  }

  @Test
  void rejectsNonHttpUrls() {
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(Duration.ofSeconds(5));

    assertThatThrownBy(() -> transport.send(
        request(URI.create("file:///tmp/d1"), "{}", Duration.ofSeconds(1),
            Collections.<String, String>emptyMap())))
        .isInstanceOf(IOException.class)
        .hasMessage("D1 transport requires an HTTP URL");
  }

  @Test
  void clampsOversizedTimeoutsAndAcceptsNullConnectTimeout() throws Exception {
    server.enqueue(new MockResponse.Builder().code(200).body("{}").build());
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(null);

    D1TransportResponse response = transport.send(
        request(server.url("/query").uri(), "{}", Duration.ofMillis(Long.MAX_VALUE / 2),
            Collections.<String, String>emptyMap()));

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void convertsConnectAndRequestTimeoutsWithoutLosingPositiveDurations() {
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(null)).isZero();
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(Duration.ZERO)).isZero();
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(Duration.ofNanos(1))).isOne();
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(Duration.ofNanos(999_999))).isOne();
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(Duration.ofMillis(250))).isEqualTo(250);
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(
        Duration.ofMillis(Integer.MAX_VALUE - 1L))).isEqualTo(Integer.MAX_VALUE - 1);
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(
        Duration.ofMillis(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
    assertThat(HttpURLConnectionD1Transport.timeoutMillis(
        Duration.ofSeconds(Long.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void emptyErrorBodiesReturnAnEmptyString() throws Exception {
    server.enqueue(new MockResponse.Builder().code(500).build());
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(Duration.ofSeconds(5));

    D1TransportResponse response = transport.send(
        request(server.url("/query").uri(), "{}", Duration.ofSeconds(10),
            Collections.<String, String>emptyMap()));

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).isEmpty();
  }

  @Test
  void readTimeoutsSurfaceAsSocketTimeoutException() {
    server.enqueue(new MockResponse.Builder()
        .code(200)
        .body("{}")
        .headersDelay(2, TimeUnit.SECONDS)
        .build());
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(Duration.ofSeconds(5));

    assertThatThrownBy(() -> transport.send(
        request(server.url("/query").uri(), "{}", Duration.ofMillis(200),
            Collections.<String, String>emptyMap())))
        .isInstanceOf(SocketTimeoutException.class);
  }

  @Test
  void reusesKeepAliveConnectionsAcrossSequentialRequests() throws Exception {
    server.enqueue(new MockResponse.Builder().code(200).body("{}").build());
    server.enqueue(new MockResponse.Builder().code(200).body("{}").build());
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(Duration.ofSeconds(5));

    transport.send(request(server.url("/query").uri(), "{}", Duration.ofSeconds(10),
        Collections.<String, String>emptyMap()));
    transport.send(request(server.url("/query").uri(), "{}", Duration.ofSeconds(10),
        Collections.<String, String>emptyMap()));

    RecordedRequest first = server.takeRequest();
    RecordedRequest second = server.takeRequest();
    assertThat(second.getConnectionIndex()).isEqualTo(first.getConnectionIndex());
    assertThat(second.getExchangeIndex()).isEqualTo(first.getExchangeIndex() + 1);
  }

  @Test
  void readsBodiesLargerThanTheInternalReadBuffer() throws Exception {
    StringBuilder body = new StringBuilder(100_000);
    for (int i = 0; i < 100_000; i++) {
      body.append((char) ('a' + (i % 26)));
    }
    server.enqueue(new MockResponse.Builder().code(200).body(body.toString()).build());
    HttpURLConnectionD1Transport transport = new HttpURLConnectionD1Transport(Duration.ofSeconds(5));

    D1TransportResponse response = transport.send(
        request(server.url("/query").uri(), "{}", Duration.ofSeconds(10),
            Collections.<String, String>emptyMap()));

    assertThat(response.body()).hasSize(100_000).isEqualTo(body.toString());
  }

  private static D1TransportRequest request(
      URI uri, String body, Duration timeout, Map<String, String> headers) {
    return new D1TransportRequest(uri, "POST", headers, body, timeout);
  }
}
