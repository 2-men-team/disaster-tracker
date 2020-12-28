package com.github.twomenteam.disastertracker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import reactor.core.publisher.Mono;

public final class Utils {
  public static final Duration TIMEOUT_DURATION = Duration.ofSeconds(2);

  private Utils() {
  }

  public static Mono<LocalDateTime> parseDateTime(String format, DateTimeFormatter formatter) {
    return Mono.fromCallable(() -> LocalDateTime.parse(format, formatter));
  }
}
