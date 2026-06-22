package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.List;

/**
 * D1 transport failure.
 */
public final class D1TransportException extends D1Exception {
  /**
   * Creates a transport exception.
   *
   * @param operation operation being performed
   * @param cause transport cause
   */
  public D1TransportException(D1Operation operation, Throwable cause) {
    super("D1 transport failed", cause, operation, null, null,
        Collections.<D1ResponseInfo>emptyList(), Collections.<D1ResponseInfo>emptyList());
  }
}
