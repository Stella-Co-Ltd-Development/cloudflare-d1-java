package io.github.xxvw.cloudflare.d1;

import java.io.IOException;

/**
 * HTTP transport used by {@link D1Client}.
 *
 * <p>Implement this interface to customize request execution without depending on a specific HTTP
 * client library.
 */
@FunctionalInterface
public interface D1Transport {
  /**
   * Sends a D1 HTTP request and returns the response.
   *
   * @param request transport request
   * @return transport response
   * @throws IOException when request execution fails
   */
  D1TransportResponse send(D1TransportRequest request) throws IOException;
}
