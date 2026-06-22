package io.github.xxvw.cloudflare.d1.internal;

import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1Query;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public final class D1HttpClient {
  private final HttpClient httpClient;
  private final URI endpoint;
  private final String apiToken;
  private final String userAgent;
  private final Duration requestTimeout;
  private final D1JsonMapper jsonMapper;

  public D1HttpClient(
      HttpClient httpClient,
      D1Endpoint endpoint,
      String apiToken,
      String userAgent,
      Duration requestTimeout,
      D1JsonMapper jsonMapper) {
    this.httpClient = httpClient;
    this.endpoint = endpoint.queryUri();
    this.apiToken = apiToken;
    this.userAgent = userAgent;
    this.requestTimeout = requestTimeout;
    this.jsonMapper = jsonMapper;
  }

  public D1HttpResponse sendQuery(D1Query query, D1Operation operation) {
    return send(jsonMapper.writeQuery(query), operation);
  }

  public D1HttpResponse sendBatch(List<D1Query> queries) {
    return send(jsonMapper.writeBatch(queries), D1Operation.BATCH);
  }

  private D1HttpResponse send(String body, D1Operation operation) {
    HttpRequest request = HttpRequest.newBuilder(endpoint)
        .timeout(requestTimeout)
        .header("Authorization", "Bearer " + apiToken)
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("User-Agent", userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return new D1HttpResponse(response.statusCode(), response.headers(), response.body());
    } catch (IOException e) {
      if (e instanceof java.net.http.HttpTimeoutException) {
        throw new D1TimeoutException(operation, e);
      }
      throw new D1TransportException(operation, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new D1TransportException(operation, e);
    }
  }

}
