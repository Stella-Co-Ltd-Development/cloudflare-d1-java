package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Metadata returned with a D1 query result.
 */
public final class D1Meta {
  private static final D1Meta EMPTY = new D1Meta(
      false, 0, null, 0, 0, 0.0, null, null, null, null, null, Map.of());

  private final boolean changedDb;
  private final long changes;
  private final Long lastRowId;
  private final long rowsRead;
  private final long rowsWritten;
  private final double duration;
  private final Long sizeAfter;
  private final String servedByColo;
  private final String servedByRegion;
  private final Boolean servedByPrimary;
  private final D1Timings timings;
  private final Map<String, Object> additionalProperties;

  /**
   * Creates a metadata model.
   *
   * @param changedDb whether the database changed
   * @param changes number of changed rows
   * @param lastRowId last inserted row ID, if present
   * @param rowsRead rows read by D1
   * @param rowsWritten rows written by D1
   * @param duration D1 duration value
   * @param sizeAfter database size after execution, if present
   * @param servedByColo serving colo, if present
   * @param servedByRegion serving region, if present
   * @param servedByPrimary whether served by primary, if present
   * @param timings timing metadata, if present
   * @param additionalProperties unknown metadata fields
   */
  public D1Meta(
      boolean changedDb,
      long changes,
      Long lastRowId,
      long rowsRead,
      long rowsWritten,
      double duration,
      Long sizeAfter,
      String servedByColo,
      String servedByRegion,
      Boolean servedByPrimary,
      D1Timings timings,
      Map<String, Object> additionalProperties) {
    this.changedDb = changedDb;
    this.changes = changes;
    this.lastRowId = lastRowId;
    this.rowsRead = rowsRead;
    this.rowsWritten = rowsWritten;
    this.duration = duration;
    this.sizeAfter = sizeAfter;
    this.servedByColo = servedByColo;
    this.servedByRegion = servedByRegion;
    this.servedByPrimary = servedByPrimary;
    this.timings = timings;
    this.additionalProperties = additionalProperties == null
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(additionalProperties));
  }

  /**
   * Returns an empty metadata instance with safe defaults.
   *
   * @return empty metadata
   */
  public static D1Meta empty() {
    return EMPTY;
  }

  /**
   * Whether the database was changed.
   *
   * @return true when the database changed
   */
  public boolean changedDb() {
    return changedDb;
  }

  /**
   * Number of changed rows.
   *
   * @return changed row count
   */
  public long changes() {
    return changes;
  }

  /**
   * Last inserted row ID.
   *
   * @return last row ID or empty
   */
  public OptionalLong lastRowId() {
    return lastRowId == null ? OptionalLong.empty() : OptionalLong.of(lastRowId);
  }

  /**
   * Number of rows read.
   *
   * @return rows read
   */
  public long rowsRead() {
    return rowsRead;
  }

  /**
   * Number of rows written.
   *
   * @return rows written
   */
  public long rowsWritten() {
    return rowsWritten;
  }

  /**
   * Duration reported by D1.
   *
   * @return duration value
   */
  public double duration() {
    return duration;
  }

  /**
   * Database size after execution.
   *
   * @return size after execution or empty
   */
  public OptionalLong sizeAfter() {
    return sizeAfter == null ? OptionalLong.empty() : OptionalLong.of(sizeAfter);
  }

  /**
   * Serving colo.
   *
   * @return serving colo or empty
   */
  public Optional<String> servedByColo() {
    return Optional.ofNullable(servedByColo);
  }

  /**
   * Serving region.
   *
   * @return serving region or empty
   */
  public Optional<String> servedByRegion() {
    return Optional.ofNullable(servedByRegion);
  }

  /**
   * Whether the request was served by the primary database.
   *
   * @return served-by-primary flag or empty
   */
  public Optional<Boolean> servedByPrimary() {
    return Optional.ofNullable(servedByPrimary);
  }

  /**
   * Timing metadata.
   *
   * @return timing metadata or empty
   */
  public Optional<D1Timings> timings() {
    return Optional.ofNullable(timings);
  }

  /**
   * Unknown metadata fields preserved from the API response.
   *
   * @return immutable additional properties
   */
  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
