package io.github.xxvw.cloudflare.d1.internal.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public final class D1MetaDto {
  @JsonProperty("changed_db")
  public Boolean changedDb;

  public Long changes;

  @JsonProperty("last_row_id")
  public Long lastRowId;

  @JsonProperty("rows_read")
  public Long rowsRead;

  @JsonProperty("rows_written")
  public Long rowsWritten;

  public Double duration;

  @JsonProperty("size_after")
  public Long sizeAfter;

  @JsonProperty("served_by_colo")
  public String servedByColo;

  @JsonProperty("served_by_region")
  public String servedByRegion;

  @JsonProperty("served_by_primary")
  public Boolean servedByPrimary;

  public D1TimingsDto timings;
  public Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnySetter
  void putAdditionalProperty(String name, Object value) {
    additionalProperties.put(name, value);
  }
}
