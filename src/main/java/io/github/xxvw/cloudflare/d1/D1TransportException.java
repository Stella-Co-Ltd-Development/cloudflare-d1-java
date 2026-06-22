package io.github.xxvw.cloudflare.d1;

import java.util.List;

/**
 * D1 transport failure.
 */
public final class D1TransportException extends D1Exception {
  public D1TransportException(D1Operation operation, Throwable cause) {
    super("D1 transport failed", cause, operation, null, null, List.of(), List.of());
  }
}
