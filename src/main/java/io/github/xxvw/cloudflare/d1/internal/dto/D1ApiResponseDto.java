package io.github.xxvw.cloudflare.d1.internal.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class D1ApiResponseDto {
  public Boolean success;
  public List<D1QueryResultDto> result = new ArrayList<>();
  public List<D1ResponseInfoDto> errors = new ArrayList<>();
  public List<D1ResponseInfoDto> messages = new ArrayList<>();
  public Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnySetter
  void putAdditionalProperty(String name, Object value) {
    additionalProperties.put(name, value);
  }
}
