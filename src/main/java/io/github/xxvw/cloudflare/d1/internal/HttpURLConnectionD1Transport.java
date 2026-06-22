package io.github.xxvw.cloudflare.d1.internal;

import io.github.xxvw.cloudflare.d1.D1Transport;
import io.github.xxvw.cloudflare.d1.D1TransportRequest;
import io.github.xxvw.cloudflare.d1.D1TransportResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class HttpURLConnectionD1Transport implements D1Transport {
  private final Duration connectTimeout;

  public HttpURLConnectionD1Transport(Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  @Override
  public D1TransportResponse send(D1TransportRequest request) throws IOException {
    URLConnection connection = request.uri().toURL().openConnection();
    if (!(connection instanceof HttpURLConnection)) {
      throw new IOException("D1 transport requires an HTTP URL");
    }
    HttpURLConnection http = (HttpURLConnection) connection;
    try {
      http.setConnectTimeout(timeoutMillis(connectTimeout));
      http.setReadTimeout(timeoutMillis(request.timeout()));
      http.setRequestMethod(request.method());
      for (Map.Entry<String, String> header : request.headers().entrySet()) {
        http.setRequestProperty(header.getKey(), header.getValue());
      }
      http.setDoOutput(true);
      byte[] body = request.body().getBytes(StandardCharsets.UTF_8);
      http.setFixedLengthStreamingMode(body.length);
      try (OutputStream output = http.getOutputStream()) {
        output.write(body);
      }

      int statusCode = http.getResponseCode();
      String responseBody = readBody(statusCode >= 400 ? http.getErrorStream() : http.getInputStream());
      Map<String, List<String>> headers = http.getHeaderFields();
      return new D1TransportResponse(statusCode, headers, responseBody);
    } finally {
      http.disconnect();
    }
  }

  private static int timeoutMillis(Duration timeout) {
    if (timeout == null) {
      return 0;
    }
    long millis = timeout.toMillis();
    if (millis > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return (int) millis;
  }

  private static String readBody(InputStream input) throws IOException {
    if (input == null) {
      return "";
    }
    try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192];
      int read;
      while ((read = stream.read(buffer)) != -1) {
        output.write(buffer, 0, read);
      }
      return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
