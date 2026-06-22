package io.github.xxvw.cloudflare.d1;

/**
 * D1 operation type used for retry and exception metadata.
 */
public enum D1Operation {
  /** Read/query operation. */
  QUERY,
  /** Execute/write operation. */
  EXECUTE,
  /** Batch operation. */
  BATCH
}
