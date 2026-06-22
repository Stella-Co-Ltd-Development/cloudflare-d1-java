package io.github.xxvw.cloudflare.d1.internal;

import java.time.Duration;

@FunctionalInterface
interface D1Sleeper {
  void sleep(Duration duration) throws InterruptedException;
}
