package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.model.dto.RegisterResponseBody;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.CalendarEventService;
import com.github.twomenteam.disastertracker.service.GoogleApiService;
import com.google.api.services.calendar.CalendarScopes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private static final List<String> AUTH_SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

  private final AuthService authService;
  private final GoogleApiService googleApiService;
  private final CalendarEventService calendarEventService;

  @GetMapping("/register")
  public Mono<RegisterResponseBody> register(@RequestParam(required = false) String webhook) {
    return authService
        .createNewUser(webhook)
        .map(user -> RegisterResponseBody.builder()
            .apiKey(user.getApiKey())
            .googleAuthUrl(googleApiService.getAuthUrl(user.getApiKey(), AUTH_SCOPES))
            .build());
  }

  @GetMapping("/code/{apiKey}")
  public Mono<Void> receiveAuthCode(@PathVariable String apiKey,
                                    @RequestParam String code, @RequestParam String scope) {
    var scopes = Collections.singletonList(scope);

    return googleApiService
        .getTokenFromCode(code, scopes)
        .flatMap(token -> authService
            .saveAuthToken(apiKey, token)
            .flatMap(user -> googleApiService
                .fetchAllEvents(token, scopes, user.getId())
                .flatMap(calendarEvents -> authService
                    .saveNextSyncToken(user, calendarEvents.getNextSyncToken())
                    .and(calendarEventService.upsertCalendarEvents(calendarEvents.getEvents()))))
            .and(googleApiService.watchCalendarEvents(token, apiKey, scopes)));
  }
}
