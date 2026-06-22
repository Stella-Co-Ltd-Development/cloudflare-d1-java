package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * D1 batch failure.
 */
public final class D1BatchException extends D1ApiException {
  /** Index of the first failed batch item. */
  private final int failedIndex;
  /** Result items returned by the API. */
  private final List<D1Result> partialResults;

  /**
   * Creates a batch exception.
   *
   * @param statusCode HTTP status code
   * @param rawBody raw HTTP response body
   * @param errors API errors for the failed item
   * @param messages API messages for the failed item
   * @param failedIndex index of the first failed batch item
   * @param partialResults result items returned before failure handling
   */
  public D1BatchException(
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages,
      int failedIndex,
      List<D1Result> partialResults) {
    super("D1 batch failed", D1Operation.BATCH, statusCode, rawBody, errors, messages);
    this.failedIndex = failedIndex;
    this.partialResults = partialResults == null
        ? Collections.<D1Result>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(partialResults));
  }

  /**
   * Index of the first failed batch item.
   *
   * @return failed item index
   */
  public int failedIndex() {
    return failedIndex;
  }

  /**
   * Result items returned by the API.
   *
   * @return immutable partial results
   */
  public List<D1Result> partialResults() {
    return partialResults;
  }
}
