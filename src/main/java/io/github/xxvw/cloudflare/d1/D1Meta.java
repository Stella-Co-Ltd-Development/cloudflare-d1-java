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

  public static D1Meta empty() {
    return EMPTY;
  }

  public boolean changedDb() {
    return changedDb;
  }

  public long changes() {
    return changes;
  }

  public OptionalLong lastRowId() {
    return lastRowId == null ? OptionalLong.empty() : OptionalLong.of(lastRowId);
  }

  public long rowsRead() {
    return rowsRead;
  }

  public long rowsWritten() {
    return rowsWritten;
  }

  public double duration() {
    return duration;
  }

  public OptionalLong sizeAfter() {
    return sizeAfter == null ? OptionalLong.empty() : OptionalLong.of(sizeAfter);
  }

  public Optional<String> servedByColo() {
    return Optional.ofNullable(servedByColo);
  }

  public Optional<String> servedByRegion() {
    return Optional.ofNullable(servedByRegion);
  }

  public Optional<Boolean> servedByPrimary() {
    return Optional.ofNullable(servedByPrimary);
  }

  public Optional<D1Timings> timings() {
    return Optional.ofNullable(timings);
  }

  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
