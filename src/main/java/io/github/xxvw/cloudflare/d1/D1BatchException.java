package io.github.xxvw.cloudflare.d1;

import java.util.List;

/**
 * D1 batch failure.
 */
public final class D1BatchException extends D1ApiException {
  private final int failedIndex;
  private final List<D1Result> partialResults;

  public D1BatchException(
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages,
      int failedIndex,
      List<D1Result> partialResults) {
    super("D1 batch failed", D1Operation.BATCH, statusCode, rawBody, errors, messages);
    this.failedIndex = failedIndex;
    this.partialResults = partialResults == null ? List.of() : List.copyOf(partialResults);
  }

  public int failedIndex() {
    return failedIndex;
  }

  public List<D1Result> partialResults() {
    return partialResults;
  }
}
