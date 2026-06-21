package io.github.xxvw.cloudflare.d1.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public final class D1RetryAfterParser {
  private final Clock clock;

  public D1RetryAfterParser() {
    this(Clock.systemUTC());
  }

  public D1RetryAfterParser(Clock clock) {
    this.clock = clock;
  }

  public Optional<Duration> parse(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    String trimmed = value.trim();
    try {
      long seconds = Long.parseLong(trimmed);
      if (seconds < 0) {
        return Optional.empty();
      }
      return Optional.of(Duration.ofSeconds(seconds));
    } catch (NumberFormatException ignored) {
      return parseHttpDate(trimmed);
    }
  }

  private Optional<Duration> parseHttpDate(String value) {
    try {
      Instant retryAt = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
      Duration duration = Duration.between(clock.instant(), retryAt);
      return Optional.of(duration.isNegative() ? Duration.ZERO : duration);
    } catch (DateTimeParseException ignored) {
      return Optional.empty();
    }
  }
}
