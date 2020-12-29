package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.Utils;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.WarningService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/warning")
@RequiredArgsConstructor
public class WarningController {
  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final WarningService warningService;
  private final AuthService authService;

  @GetMapping("/get")
  public Flux<Warning> getWarnings(@RequestParam String apiKey, @RequestParam String from, @RequestParam String to) {
    return authService
        .findUserByApiKey(apiKey)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Api key is invalid")))
        .flatMapMany(user -> Flux.zip(
            Utils.parseDateTime(from, FORMATTER),
            Utils.parseDateTime(to, FORMATTER))
            .flatMap(tuple -> {
              var fromDateTime = tuple.getT1();
              var toDateTime = tuple.getT2();

              if (fromDateTime.isAfter(toDateTime)) {
                return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be before 'to'"));
              }

              return warningService.retrieve(user, fromDateTime, toDateTime);
            })
            .onErrorMap(DateTimeParseException.class,
                e -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Date parameters have invalid format")));
  }
}
