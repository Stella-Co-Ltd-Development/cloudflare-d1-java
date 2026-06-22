package io.github.xxvw.cloudflare.d1.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.xxvw.cloudflare.d1.D1ApiException;
import io.github.xxvw.cloudflare.d1.D1AuthenticationException;
import io.github.xxvw.cloudflare.d1.D1AuthorizationException;
import io.github.xxvw.cloudflare.d1.D1BatchException;
import io.github.xxvw.cloudflare.d1.D1Operation;
import io.github.xxvw.cloudflare.d1.D1QueryException;
import io.github.xxvw.cloudflare.d1.D1RateLimitException;
import io.github.xxvw.cloudflare.d1.D1ResponseInfo;
import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.internal.dto.D1ApiResponseDto;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class D1ExceptionFactory {
  private final D1JsonMapper jsonMapper;
  private final D1ResponseParser responseParser;
  private final D1RetryAfterParser retryAfterParser;

  public D1ExceptionFactory(D1JsonMapper jsonMapper, D1ResponseParser responseParser) {
    this.jsonMapper = jsonMapper;
    this.responseParser = responseParser;
    this.retryAfterParser = new D1RetryAfterParser();
  }

  public D1ApiException httpException(D1HttpResponse response, D1Operation operation, String sql) {
    ParsedError parsed = parseError(response.body());
    Optional<Duration> retryAfter = parseRetryAfter(response.firstHeader("Retry-After"));
    return statusException(
        response.statusCode(),
        operation,
        response.body(),
        parsed.errors(),
        parsed.messages(),
        sql,
        retryAfter.orElse(null));
  }

  public D1ApiException topLevelFailure(
      int statusCode,
      String rawBody,
      D1Operation operation,
      D1ApiResponseDto apiResponse,
      String sql) {
    return statusException(
        statusCode,
        operation,
        rawBody,
        responseParser.topErrors(apiResponse),
        responseParser.topMessages(apiResponse),
        sql,
        null);
  }

  public D1QueryException queryFailure(
      int statusCode,
      String rawBody,
      D1Operation operation,
      D1Result result,
      String sql) {
    return new D1QueryException(operation, statusCode, rawBody, result.errors(), result.messages(), sql);
  }

  public D1BatchException batchFailure(int statusCode, String rawBody, List<D1Result> results) {
    int failedIndex = 0;
    for (int i = 0; i < results.size(); i++) {
      if (!results.get(i).success()) {
        failedIndex = i;
        break;
      }
    }
    D1Result failed = results.isEmpty() ? null : results.get(failedIndex);
    return new D1BatchException(
        statusCode,
        rawBody,
        failed == null ? Collections.<D1ResponseInfo>emptyList() : failed.errors(),
        failed == null ? Collections.<D1ResponseInfo>emptyList() : failed.messages(),
        failedIndex,
        results);
  }

  private D1ApiException statusException(
      int statusCode,
      D1Operation operation,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages,
      String sql,
      Duration retryAfter) {
    switch (statusCode) {
      case 400:
        return new D1QueryException(operation, statusCode, rawBody, errors, messages, sql);
      case 401:
        return new D1AuthenticationException(operation, statusCode, rawBody, errors, messages);
      case 403:
        return new D1AuthorizationException(operation, statusCode, rawBody, errors, messages);
      case 429:
        return new D1RateLimitException(operation, statusCode, rawBody, errors, messages, retryAfter);
      default:
        return new D1ApiException("D1 API request failed", operation, statusCode, rawBody, errors, messages);
    }
  }

  private ParsedError parseError(String rawBody) {
    try {
      D1ApiResponseDto apiResponse = jsonMapper.readApiResponse(rawBody);
      return new ParsedError(responseParser.topErrors(apiResponse), responseParser.topMessages(apiResponse));
    } catch (JsonProcessingException e) {
      return new ParsedError(
          Collections.<D1ResponseInfo>emptyList(),
          Collections.<D1ResponseInfo>emptyList());
    }
  }

  private Optional<Duration> parseRetryAfter(String value) {
    if (value == null) {
      return Optional.empty();
    }
    return retryAfterParser.parse(value);
  }

  private static final class ParsedError {
    private final List<D1ResponseInfo> errors;
    private final List<D1ResponseInfo> messages;

    private ParsedError(List<D1ResponseInfo> errors, List<D1ResponseInfo> messages) {
      this.errors = errors;
      this.messages = messages;
    }

    private List<D1ResponseInfo> errors() {
      return errors;
    }

    private List<D1ResponseInfo> messages() {
      return messages;
    }
  }
}
