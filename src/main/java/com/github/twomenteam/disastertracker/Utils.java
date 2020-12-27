package com.github.twomenteam.disastertracker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import reactor.core.publisher.Mono;

public final class Utils {
  private Utils() {
  }

  public static Mono<LocalDateTime> parseDateTime(String format, DateTimeFormatter formatter) {
    return Mono.fromCallable(() -> LocalDateTime.parse(format, formatter));
  }
}
