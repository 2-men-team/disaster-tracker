package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.CalendarEventService;
import com.github.twomenteam.disastertracker.service.GoogleApiService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class CalendarEventController {
  public static final String TOKEN_HEADER_NAME = "X-Goog-Channel-Token";
  public static final String STATE_HEADER_NAME = "X-Goog-Resource-State";
  public static final String SYNC_STATE = "sync";
  public static final String EXISTS_STATE = "exists";

  private final CalendarEventService calendarEventService;
  private final AuthService authService;
  private final GoogleApiService googleApiService;

  @PostMapping("/receive")
  public Mono<Void> receiveCalendarEvent(@RequestHeader(TOKEN_HEADER_NAME) String apiKey,
                                         @RequestHeader(STATE_HEADER_NAME) String state) {
    System.out.println("Received new state: " + state);

    if (SYNC_STATE.equals(state)) {
      return Mono.empty();
    }

    var updateMin = Instant.now().minusSeconds(120);
    return authService
        .findUserByApiKey(apiKey)
        .flatMap(user -> googleApiService
            .refreshToken(user.getAuthToken(), GoogleApiService.DEFAULT_SCOPES)
            .flatMap(newToken -> authService.saveAuthToken(apiKey, newToken))
            .switchIfEmpty(Mono.just(user)))
        .flatMap(user -> googleApiService
            .fetchLatestEvents(user.getAuthToken(), updateMin, GoogleApiService.DEFAULT_SCOPES, user.getId())
            .doOnNext(event -> System.out.println("Got event: " + event))
            .flatMap(calendarEventService::updateCalendarEvents)
            .then());
  }
}
