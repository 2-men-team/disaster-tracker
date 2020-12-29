package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.dto.FetchedCalendarEvent;
import com.google.api.services.calendar.CalendarScopes;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GoogleApiService {
  List<String> DEFAULT_SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

  Mono<Void> watchCalendarEvents(AuthToken token, String apiKey, List<String> scopes);
  String getAuthUrl(String apiKey, List<String> scopes);
  Mono<AuthToken> getTokenFromCode(String authCode, List<String> scopes);
  Mono<AuthToken> refreshToken(AuthToken token, List<String> scopes);
  Flux<FetchedCalendarEvent> fetchAllEvents(AuthToken token, List<String> scopes, int userId);
  Flux<FetchedCalendarEvent> fetchLatestEvents(AuthToken token, Instant updateMin, List<String> scopes, int userId);
}
