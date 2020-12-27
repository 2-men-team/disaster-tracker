package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.db.Coordinates;
import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.dto.CalendarEvents;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Mono;

public interface GoogleApiService {
  List<String> DEFAULT_SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

  Mono<Void> watchCalendarEvents(AuthToken token, String apiKey, List<String> scopes);
  String getAuthUrl(String apiKey, List<String> scopes);
  Mono<AuthToken> getTokenFromCode(String authCode, List<String> scopes);
  Mono<AuthToken> refreshToken(AuthToken token, List<String> scopes);
  Mono<CalendarEvents> fetchAllEvents(AuthToken token, List<String> scopes, int userId);
  Mono<CalendarEvents> fetchLatestEvents(AuthToken token, String syncToken, List<String> scopes, int userId);
}
