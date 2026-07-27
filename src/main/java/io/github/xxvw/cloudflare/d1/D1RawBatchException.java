package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * D1 raw batch failure.
 */
public final class D1RawBatchException extends D1ApiException {
  /** Index of the first failed raw batch item. */
  private final int failedIndex;
  /** Raw result items returned by the API. */
  private final List<D1RawResult> partialResults;

  /**
   * Creates a raw batch exception.
   *
   * @param statusCode HTTP status code
   * @param rawBody raw HTTP response body
   * @param errors API errors for the failed item
   * @param messages API messages for the failed item
   * @param failedIndex index of the first failed raw batch item
   * @param partialResults raw result items returned before failure handling
   */
  public D1RawBatchException(
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages,
      int failedIndex,
      List<D1RawResult> partialResults) {
    super("D1 raw batch failed", D1Operation.RAW_BATCH, statusCode, rawBody, errors, messages);
    this.failedIndex = failedIndex;
    this.partialResults = partialResults == null
        ? Collections.<D1RawResult>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(partialResults));
  }

  /**
   * Index of the first failed raw batch item.
   *
   * @return failed item index
   */
  public int failedIndex() {
    return failedIndex;
  }

  /**
   * Raw result items returned by the API.
   *
   * @return immutable partial results
   */
  public List<D1RawResult> partialResults() {
    return partialResults;
  }
}
