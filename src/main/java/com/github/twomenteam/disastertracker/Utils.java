package com.github.twomenteam.disastertracker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

public final class Utils {
  public static final Duration TIMEOUT_DURATION = Duration.ofSeconds(5);
  public static final RetryBackoffSpec DEFAULT_RETRY = Retry.backoff(3, Duration.ofMillis(100));

  private Utils() {
  }

  public static Mono<LocalDateTime> parseDateTime(String format, DateTimeFormatter formatter) {
    return Mono.fromCallable(() -> LocalDateTime.parse(format, formatter));
  }
}
