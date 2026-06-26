package io.github.xxvw.cloudflare.d1.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxvw.cloudflare.d1.D1MappingException;
import io.github.xxvw.cloudflare.d1.D1Query;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ApiResponseDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1RawApiResponseDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class D1JsonMapper {
  private final ObjectMapper internalObjectMapper;
  private final ObjectMapper mappingObjectMapper;

  public D1JsonMapper(ObjectMapper mappingObjectMapper) {
    this.internalObjectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    this.mappingObjectMapper = mappingObjectMapper == null
        ? new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        : mappingObjectMapper;
  }

  public String writeQuery(D1Query query) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("sql", query.sql());
    body.put("params", query.params());
    return write(body);
  }

  public String writeBatch(List<D1Query> queries) {
    List<Map<String, Object>> batch = new ArrayList<>(queries.size());
    for (D1Query query : queries) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("sql", query.sql());
      item.put("params", query.params());
      batch.add(item);
    }
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("batch", batch);
    return write(body);
  }

  public D1ApiResponseDto readApiResponse(String rawBody) throws JsonProcessingException {
    return internalObjectMapper.readValue(rawBody, D1ApiResponseDto.class);
  }

  public D1RawApiResponseDto readRawApiResponse(String rawBody) throws JsonProcessingException {
    return internalObjectMapper.readValue(rawBody, D1RawApiResponseDto.class);
  }

  public <T> List<T> mapRows(List<Map<String, Object>> rows, Class<T> type) {
    List<T> mapped = new ArrayList<>(rows.size());
    for (int i = 0; i < rows.size(); i++) {
      Map<String, Object> row = rows.get(i);
      try {
        mapped.add(mappingObjectMapper.convertValue(row, type));
      } catch (IllegalArgumentException e) {
        throw new D1MappingException(type, i, row, e);
      }
    }
    return Collections.unmodifiableList(mapped);
  }

  public <T> T mapRow(Map<String, Object> row, Class<T> type, int rowIndex) {
    try {
      return mappingObjectMapper.convertValue(row, type);
    } catch (IllegalArgumentException e) {
      throw new D1MappingException(type, rowIndex, row, e);
    }
  }

  private String write(Object value) {
    try {
      return internalObjectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("failed to serialize D1 request body", e);
    }
  }
}
