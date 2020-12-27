package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.CalendarEventService;
import com.github.twomenteam.disastertracker.service.GoogleApiService;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController("/event")
public class CalendarEventController {
  private final CalendarEventService calendarEventService;
  private final AuthService authService;
  private final GoogleApiService googleApiService;

  public CalendarEventController(CalendarEventService calendarEventService, AuthService authService,
                                 GoogleApiService googleApiService) {
    this.calendarEventService = calendarEventService;
    this.authService = authService;
    this.googleApiService = googleApiService;
  }

  @PostMapping("/receive")
  public Mono<Void> receiveCalendarEvent(
      @Header("X-Goog-Channel-Token") String apiKey,
      @Header("X-Goog-Resource-State") String state) {
    if ("sync".equals(state)) {
      return Mono.empty();
    }

    return authService
        .findUserByApiKey(apiKey)
        .flatMap(user -> googleApiService
            .refreshToken(user.getAuthToken(), GoogleApiService.DEFAULT_SCOPES)
            .flatMap(newToken -> authService.saveAuthToken(apiKey, newToken))
            .switchIfEmpty(Mono.just(user)))
        .flatMap(user -> googleApiService
            .fetchLatestEvents(user.getAuthToken(), user.getNextSyncToken(),
                GoogleApiService.DEFAULT_SCOPES, user.getId())
            .flatMap(calendarEvents -> authService
                .saveNextSyncToken(user, calendarEvents.getNextSyncToken())
                .and(calendarEventService.upsertCalendarEvents(calendarEvents.getEvents()))));
  }
}
