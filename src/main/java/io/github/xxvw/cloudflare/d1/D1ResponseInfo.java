package io.github.xxvw.cloudflare.d1;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Message or error returned by the D1 API.
 */
public final class D1ResponseInfo {
  private final int code;
  private final String message;
  private final String documentationUrl;
  private final D1ResponseSource source;
  private final Map<String, Object> additionalProperties;

  public D1ResponseInfo(
      int code,
      String message,
      String documentationUrl,
      D1ResponseSource source,
      Map<String, Object> additionalProperties) {
    this.code = code;
    this.message = message;
    this.documentationUrl = documentationUrl;
    this.source = source;
    this.additionalProperties = additionalProperties == null
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(additionalProperties));
  }

  public int code() {
    return code;
  }

  public String message() {
    return message;
  }

  public Optional<String> documentationUrl() {
    return Optional.ofNullable(documentationUrl);
  }

  public Optional<D1ResponseSource> source() {
    return Optional.ofNullable(source);
  }

  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
