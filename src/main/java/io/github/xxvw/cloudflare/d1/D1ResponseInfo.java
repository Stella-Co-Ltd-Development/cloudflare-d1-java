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

  /**
   * Creates a response information model.
   *
   * @param code response code
   * @param message response message
   * @param documentationUrl documentation URL, if present
   * @param source source location, if present
   * @param additionalProperties unknown response fields
   */
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
        ? Collections.<String, Object>emptyMap()
        : Collections.unmodifiableMap(new LinkedHashMap<>(additionalProperties));
  }

  /**
   * Response code.
   *
   * @return response code
   */
  public int code() {
    return code;
  }

  /**
   * Response message.
   *
   * @return response message
   */
  public String message() {
    return message;
  }

  /**
   * Documentation URL supplied by the API.
   *
   * @return documentation URL or empty
   */
  public Optional<String> documentationUrl() {
    return Optional.ofNullable(documentationUrl);
  }

  /**
   * Source location supplied by the API.
   *
   * @return source location or empty
   */
  public Optional<D1ResponseSource> source() {
    return Optional.ofNullable(source);
  }

  /**
   * Unknown response fields preserved from the API response.
   *
   * @return immutable additional properties
   */
  public Map<String, Object> additionalProperties() {
    return additionalProperties;
  }
}
