package io.github.xxvw.cloudflare.d1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Base unchecked exception for D1 client failures.
 */
public class D1Exception extends RuntimeException {
  /** Operation being performed when the failure happened. */
  private final D1Operation operation;
  /** HTTP status code returned by the API. */
  private final Integer statusCode;
  /** Raw HTTP response body. */
  private final String rawBody;
  /** API error details. */
  private final List<D1ResponseInfo> errors;
  /** API informational messages. */
  private final List<D1ResponseInfo> messages;

  /**
   * Creates a D1 exception.
   *
   * @param message exception message
   * @param cause root cause
   * @param operation operation being performed, if available
   * @param statusCode HTTP status code, if available
   * @param rawBody raw HTTP response body, if available
   * @param errors API errors
   * @param messages API messages
   */
  public D1Exception(
      String message,
      Throwable cause,
      D1Operation operation,
      Integer statusCode,
      String rawBody,
      List<D1ResponseInfo> errors,
      List<D1ResponseInfo> messages) {
    super(message, cause);
    this.operation = operation;
    this.statusCode = statusCode;
    this.rawBody = rawBody;
    this.errors = errors == null
        ? Collections.<D1ResponseInfo>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(errors));
    this.messages = messages == null
        ? Collections.<D1ResponseInfo>emptyList()
        : Collections.unmodifiableList(new ArrayList<>(messages));
  }

  /**
   * Operation being performed when the failure happened.
   *
   * @return operation or empty
   */
  public Optional<D1Operation> operation() {
    return Optional.ofNullable(operation);
  }

  /**
   * HTTP status code returned by the API.
   *
   * @return status code or empty
   */
  public OptionalInt statusCode() {
    return statusCode == null ? OptionalInt.empty() : OptionalInt.of(statusCode);
  }

  /**
   * Raw HTTP response body.
   *
   * @return raw body or empty
   */
  public Optional<String> rawBody() {
    return Optional.ofNullable(rawBody);
  }

  /**
   * API error details.
   *
   * @return immutable error list
   */
  public List<D1ResponseInfo> errors() {
    return errors;
  }

  /**
   * API informational messages.
   *
   * @return immutable message list
   */
  public List<D1ResponseInfo> messages() {
    return messages;
  }
}
