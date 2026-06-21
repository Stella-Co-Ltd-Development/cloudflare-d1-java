package io.github.xxvw.cloudflare.d1;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Base unchecked exception for D1 client failures.
 */
public class D1Exception extends RuntimeException {
  private final D1Operation operation;
  private final Integer statusCode;
  private final String rawBody;
  private final List<D1ResponseInfo> errors;
  private final List<D1ResponseInfo> messages;

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
    this.errors = errors == null ? List.of() : List.copyOf(errors);
    this.messages = messages == null ? List.of() : List.copyOf(messages);
  }

  public Optional<D1Operation> operation() {
    return Optional.ofNullable(operation);
  }

  public OptionalInt statusCode() {
    return statusCode == null ? OptionalInt.empty() : OptionalInt.of(statusCode);
  }

  public Optional<String> rawBody() {
    return Optional.ofNullable(rawBody);
  }

  public List<D1ResponseInfo> errors() {
    return errors;
  }

  public List<D1ResponseInfo> messages() {
    return messages;
  }
}
