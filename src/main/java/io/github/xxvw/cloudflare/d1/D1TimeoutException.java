package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.List;

/**
 * D1 request timeout.
 */
public final class D1TimeoutException extends D1Exception {
  /**
   * Creates a timeout exception.
   *
   * @param operation operation being performed
   * @param cause timeout cause
   */
  public D1TimeoutException(D1Operation operation, Throwable cause) {
    super("D1 request timed out", cause, operation, null, null,
        Collections.<D1ResponseInfo>emptyList(), Collections.<D1ResponseInfo>emptyList());
  }
}
