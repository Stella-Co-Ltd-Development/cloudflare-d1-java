package io.github.xxvw.cloudflare.d1.internal;

import io.github.xxvw.cloudflare.d1.D1Meta;
import io.github.xxvw.cloudflare.d1.D1ResponseInfo;
import io.github.xxvw.cloudflare.d1.D1ResponseSource;
import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.D1Timings;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ApiResponseDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1MetaDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1QueryResultDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ResponseInfoDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ResponseSourceDto;
import io.github.xxvw.cloudflare.d1.internal.dto.D1TimingsDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class D1ResponseParser {
  private final D1JsonMapper jsonMapper;

  public D1ResponseParser(D1JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public D1ApiResponseDto parseApiResponse(String rawBody) throws com.fasterxml.jackson.core.JsonProcessingException {
    return jsonMapper.readApiResponse(rawBody);
  }

  public D1Result parseSingle(D1ApiResponseDto apiResponse, String rawBody) {
    D1QueryResultDto result = firstResult(apiResponse);
    return toResult(result, rawBody, infoList(apiResponse.messages), infoList(apiResponse.errors));
  }

  public List<D1Result> parseBatch(D1ApiResponseDto apiResponse, String rawBody) {
    List<D1Result> results = new ArrayList<>();
    List<D1ResponseInfo> topMessages = infoList(apiResponse.messages);
    List<D1ResponseInfo> topErrors = infoList(apiResponse.errors);
    if (apiResponse.result == null) {
      return List.of();
    }
    for (D1QueryResultDto result : apiResponse.result) {
      results.add(toResult(result, rawBody, topMessages, topErrors));
    }
    return List.copyOf(results);
  }

  public List<D1ResponseInfo> topErrors(D1ApiResponseDto apiResponse) {
    return infoList(apiResponse == null ? null : apiResponse.errors);
  }

  public List<D1ResponseInfo> topMessages(D1ApiResponseDto apiResponse) {
    return infoList(apiResponse == null ? null : apiResponse.messages);
  }

  private static D1QueryResultDto firstResult(D1ApiResponseDto apiResponse) {
    if (apiResponse.result == null || apiResponse.result.isEmpty()) {
      return null;
    }
    return apiResponse.result.get(0);
  }

  private static D1Result toResult(
      D1QueryResultDto result,
      String rawBody,
      List<D1ResponseInfo> topMessages,
      List<D1ResponseInfo> topErrors) {
    if (result == null) {
      return new D1Result(true, List.of(), D1Meta.empty(), topMessages, topErrors, rawBody);
    }
    List<D1ResponseInfo> messages = new ArrayList<>(topMessages);
    messages.addAll(infoList(result.messages));
    List<D1ResponseInfo> errors = new ArrayList<>(topErrors);
    errors.addAll(infoList(result.errors));
    return new D1Result(
        result.success == null || result.success,
        result.results == null ? List.of() : result.results,
        meta(result.meta),
        messages,
        errors,
        rawBody);
  }

  private static D1Meta meta(D1MetaDto dto) {
    if (dto == null) {
      return D1Meta.empty();
    }
    return new D1Meta(
        dto.changedDb != null && dto.changedDb,
        dto.changes == null ? 0 : dto.changes,
        dto.lastRowId,
        dto.rowsRead == null ? 0 : dto.rowsRead,
        dto.rowsWritten == null ? 0 : dto.rowsWritten,
        dto.duration == null ? 0.0 : dto.duration,
        dto.sizeAfter,
        dto.servedByColo,
        dto.servedByRegion,
        dto.servedByPrimary,
        timings(dto.timings),
        dto.additionalProperties);
  }

  private static D1Timings timings(D1TimingsDto dto) {
    if (dto == null) {
      return null;
    }
    return new D1Timings(dto.sqlDurationMs == null ? 0.0 : dto.sqlDurationMs, dto.additionalProperties);
  }

  static List<D1ResponseInfo> infoList(List<D1ResponseInfoDto> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return List.of();
    }
    List<D1ResponseInfo> infos = new ArrayList<>(dtos.size());
    for (D1ResponseInfoDto dto : dtos) {
      infos.add(info(dto));
    }
    return List.copyOf(infos);
  }

  private static D1ResponseInfo info(D1ResponseInfoDto dto) {
    if (dto == null) {
      return new D1ResponseInfo(0, null, null, null, Map.of());
    }
    return new D1ResponseInfo(
        dto.code == null ? 0 : dto.code,
        dto.message,
        dto.documentationUrl,
        source(dto.source),
        dto.additionalProperties);
  }

  private static D1ResponseSource source(D1ResponseSourceDto dto) {
    if (dto == null) {
      return null;
    }
    return new D1ResponseSource(dto.pointer, dto.additionalProperties);
  }
}
