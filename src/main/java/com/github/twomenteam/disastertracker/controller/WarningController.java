package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.Utils;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.WarningService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

import reactor.core.publisher.Flux;

@RestController("/warning")
public class WarningController {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final WarningService warningService;
  private final AuthService authService;

  @Autowired
  public WarningController(WarningService warningService, AuthService authService) {
    this.warningService = warningService;
    this.authService = authService;
  }

  @GetMapping("/get")
  public Flux<Warning> getWarnings(@RequestParam String apiKey, @RequestParam String from, @RequestParam String to) {
    return authService
        .findUserByApiKey(apiKey)
        .flatMapMany(user -> Flux.zip(
            Utils.parseDateTime(from, FORMATTER),
            Utils.parseDateTime(to, FORMATTER))
            .flatMap(tuple -> {
              var fromDateTime = tuple.getT1();
              var toDateTime = tuple.getT2();

              if (fromDateTime.isAfter(toDateTime)) {
                return Flux.error(new IllegalArgumentException("'from' must be before 'to'"));
              }

              return warningService.retrieve(user, fromDateTime, toDateTime);
            }));
  }
}
