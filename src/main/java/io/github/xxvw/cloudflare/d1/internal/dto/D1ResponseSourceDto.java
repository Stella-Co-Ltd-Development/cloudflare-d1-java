package io.github.xxvw.cloudflare.d1.internal.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class D1ResponseSourceDto {
  public String pointer;
  public Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnySetter
  void putAdditionalProperty(String name, Object value) {
    additionalProperties.put(name, value);
  }
}
