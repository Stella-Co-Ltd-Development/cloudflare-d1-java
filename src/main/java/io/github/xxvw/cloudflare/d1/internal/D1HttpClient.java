package io.github.xxvw.cloudflare.d1.internal;

import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1Query;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1Transport;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import io.github.xxvw.cloudflare.d1.D1TransportRequest;
import io.github.xxvw.cloudflare.d1.D1TransportResponse;
import java.io.IOException;
import java.net.URI;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class D1HttpClient {
  private final D1Transport transport;
  private final URI endpoint;
  private final String apiToken;
  private final String userAgent;
  private final Duration requestTimeout;
  private final D1JsonMapper jsonMapper;

  public D1HttpClient(
      D1Transport transport,
      D1Endpoint endpoint,
      String apiToken,
      String userAgent,
      Duration requestTimeout,
      D1JsonMapper jsonMapper) {
    this.transport = transport;
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
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Authorization", "Bearer " + apiToken);
    headers.put("Content-Type", "application/json");
    headers.put("Accept", "application/json");
    headers.put("User-Agent", userAgent);
    D1TransportRequest request = new D1TransportRequest(endpoint, "POST", headers, body, requestTimeout);
    try {
      D1TransportResponse response = transport.send(request);
      return new D1HttpResponse(response.statusCode(), response.headers(), response.body());
    } catch (IOException e) {
      if (e instanceof SocketTimeoutException) {
        throw new D1TimeoutException(operation, e);
      }
      throw new D1TransportException(operation, e);
    }
  }

}
