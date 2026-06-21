package io.github.xxvw.cloudflare.d1.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class D1RetryAfterParserTest {

  private final D1RetryAfterParser parser =
      new D1RetryAfterParser(Clock.fixed(Instant.parse("2026-06-21T00:00:00Z"), ZoneOffset.UTC));

  @Test
  void parsesSecondsAndZeroSeconds() {
    assertThat(parser.parse("3")).contains(Duration.ofSeconds(3));
    assertThat(parser.parse("0")).contains(Duration.ZERO);
  }

  @Test
  void parsesFutureHttpDateAndPastHttpDate() {
    assertThat(parser.parse("Sun, 21 Jun 2026 00:00:03 GMT")).contains(Duration.ofSeconds(3));
    assertThat(parser.parse("Sun, 21 Jun 2020 00:00:00 GMT")).contains(Duration.ZERO);
  }

  @Test
  void ignoresInvalidNullAndNegativeValues() {
    assertThat(parser.parse("invalid")).isEmpty();
    assertThat(parser.parse(null)).isEmpty();
    assertThat(parser.parse("-1")).isEmpty();
  }
}
