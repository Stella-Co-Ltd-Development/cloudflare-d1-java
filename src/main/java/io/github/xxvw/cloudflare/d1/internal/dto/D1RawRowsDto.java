package io.github.xxvw.cloudflare.d1.internal.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class D1RawRowsDto {
  public List<String> columns = new ArrayList<>();
  public List<List<Object>> rows = new ArrayList<>();
  public Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnySetter
  void putAdditionalProperty(String name, Object value) {
    additionalProperties.put(name, value);
  }
}
