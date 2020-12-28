package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.db.Coordinates;
import com.github.twomenteam.disastertracker.model.dto.CalendarEvents;
import com.github.twomenteam.disastertracker.service.GoogleApiService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Channel;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class GoogleApiServiceImpl implements GoogleApiService {
  private static final String CLIENT_SECRETS_FILE = "/credentials.json";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String THIS_SERVER_PROTOCOL_AND_DOMAIN = "https://techsheet.dev:8080";
  private static final String REDIRECT_URI = THIS_SERVER_PROTOCOL_AND_DOMAIN + "/auth/code/";
  private static final String WEB_HOOK_URI = THIS_SERVER_PROTOCOL_AND_DOMAIN + "/event/receive";
  private static final NetHttpTransport NET_HTTP_TRANSPORT;
  private static final String APPLICATION_NAME = "Disaster tracker";
  private static final GoogleClientSecrets CLIENT_SECRETS;
  private static final String CALENDAR_ID = "primary";
  private static final String DEFAULT_TIME_ZONE = "UTC";
  private static final GeoApiContext GEO_API_CONTEXT = new GeoApiContext.Builder()
      .apiKey("AIzaSyC4sC4XLIV2HDAh24yIvd9nwmYeLbjeVNc")
      .build();

  static {
    try {
      var stream = GoogleApiServiceImpl.class.getResourceAsStream(CLIENT_SECRETS_FILE);
      Objects.requireNonNull(stream, "Credentials file is not available");
      CLIENT_SECRETS = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(stream));
      NET_HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static LocalDateTime eventDateTimeToLocalDateTime(EventDateTime eventDateTime) {
    if (eventDateTime == null) {
      return null;
    }

    long value = -1;
    if (eventDateTime.getDateTime() != null) {
      value = eventDateTime.getDateTime().getValue();
    } else if (eventDateTime.getDate() != null) {
      value = eventDateTime.getDate().getValue();
    }

    if (value == -1) {
      return null;
    }

    return Instant.ofEpochMilli(value).atZone(ZoneId.of(DEFAULT_TIME_ZONE)).toLocalDateTime();
  }

  private CalendarEvents buildCalendarEvents(Events events, int userId) {
    var result = CalendarEvents.builder()
        .nextSyncToken(events.getNextSyncToken());

    var calendarEvents = Flux.fromIterable(events.getItems())
        .filter(event -> event.getLocation() != null)
        .flatMap(event ->
            getCoordinateFromAddress(event.getLocation())
                .map(coordinates -> CalendarEvent.builder()
                    .userId(userId)
                    .googleId(event.getId())
                    .summary(event.getSummary())
                    .start(eventDateTimeToLocalDateTime(event.getStart()))
                    .end(eventDateTimeToLocalDateTime(event.getEnd()))
                    .location(event.getLocation())
                    .coordinates(coordinates)
                    .build()));

    return result.events(calendarEvents).build();
  }

  @Override
  public Mono<CalendarEvents> fetchAllEvents(AuthToken authToken, List<String> scopes, int userId) {
    return Mono.fromCallable(() ->
        getCalendarApi(credentialFromToken(authToken, scopes))
            .events()
            .list(CALENDAR_ID)
            .setTimeMin(new DateTime(System.currentTimeMillis()))
            .setTimeZone(DEFAULT_TIME_ZONE)
            .setSingleEvents(true)
            .execute())
        .subscribeOn(Schedulers.boundedElastic())
        .publishOn(Schedulers.parallel())
        .map(events -> buildCalendarEvents(events, userId));
  }

  @Override
  public Mono<CalendarEvents> fetchLatestEvents(AuthToken token, String syncToken, List<String> scopes, int userId) {
    return Mono.fromCallable(() ->
        getCalendarApi(credentialFromToken(token, scopes))
            .events()
            .list(CALENDAR_ID)
            .setSyncToken(syncToken)
            .setTimeZone(DEFAULT_TIME_ZONE)
            .setSingleEvents(true)
            .execute())
        .subscribeOn(Schedulers.boundedElastic())
        .publishOn(Schedulers.parallel())
        .map(events -> buildCalendarEvents(events, userId));
  }

  @Override
  public Mono<Void> watchCalendarEvents(AuthToken token, String apiKey, List<String> scopes) {
    return Mono.fromCallable(() ->
        getCalendarApi(credentialFromToken(token, scopes))
            .events()
            .watch(CALENDAR_ID, new Channel()
                .setAddress(WEB_HOOK_URI)
                .setId(UUID.randomUUID().toString())
                .setType("web_hook")
                .setToken(apiKey)
                .setExpiration(Long.MAX_VALUE))
            .execute())
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  private Mono<Coordinates> getCoordinateFromAddress(String address) {
    return Mono.fromCallable(() -> GeocodingApi.geocode(GEO_API_CONTEXT, address).await())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(response -> {
          if (response.length == 0) {
            return Mono.empty();
          }

          return Mono.just(Coordinates.fromLatLng(response[0].geometry.location));
        });
  }

  @Override
  public String getAuthUrl(String apiKey, List<String> scopes) {
    return getAuthFlow(scopes)
        .newAuthorizationUrl()
        .setState(apiKey)
        .setRedirectUri(REDIRECT_URI)
        .build();
  }

  @Override
  public Mono<AuthToken> getTokenFromCode(String authCode, List<String> scopes) {
    var flow = getAuthFlow(scopes);
    return Mono.fromCallable(() -> flow.newTokenRequest(authCode).setRedirectUri(REDIRECT_URI).execute())
        .subscribeOn(Schedulers.boundedElastic())
        .map(googleTokenResponse -> newCredential(flow)
            .setFromTokenResponse(googleTokenResponse))
        .map(credential -> AuthToken.builder()
            .accessToken(credential.getAccessToken())
            .refreshToken(credential.getRefreshToken())
            .expirationTimeInMillis(credential.getExpirationTimeMilliseconds())
            .build());
  }

  @Override
  public Mono<AuthToken> refreshToken(AuthToken authToken, List<String> scopes) {
    if (authToken.getExpirationTimeInMillis() == null || authToken.getRefreshToken() == null) {
      return Mono.empty();
    }

    var expires = Instant
        .ofEpochMilli(authToken.getExpirationTimeInMillis())
        .atZone(ZoneOffset.systemDefault())
        .toLocalDateTime();

    if (LocalDateTime.now().isBefore(expires.minusMinutes(2))) {
      return Mono.empty();
    }

    return Mono.fromCallable(() -> {
      var credential = credentialFromToken(authToken, scopes);

      if (!credential.refreshToken()) {
        throw new IllegalStateException("Can't refresh oauth token");
      }

      return credential;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .map(credential -> AuthToken.builder()
            .expirationTimeInMillis(credential.getExpirationTimeMilliseconds())
            .refreshToken(credential.getRefreshToken())
            .accessToken(credential.getAccessToken())
            .build());
  }

  private Credential credentialFromToken(AuthToken authToken, List<String> scopes) {
    return newCredential(getAuthFlow(scopes))
        .setExpirationTimeMilliseconds(authToken.getExpirationTimeInMillis())
        .setRefreshToken(authToken.getRefreshToken())
        .setAccessToken(authToken.getAccessToken());
  }

  private Credential newCredential(GoogleAuthorizationCodeFlow flow) {
    return new Credential.Builder(flow.getMethod())
        .setTransport(flow.getTransport())
        .setClock(flow.getClock())
        .setJsonFactory(flow.getJsonFactory())
        .setClientAuthentication(flow.getClientAuthentication())
        .setRequestInitializer(flow.getRequestInitializer())
        .setTokenServerEncodedUrl(flow.getTokenServerEncodedUrl())
        .build();
  }

  private GoogleAuthorizationCodeFlow getAuthFlow(List<String> scopes) {
    return new GoogleAuthorizationCodeFlow.Builder(
        NET_HTTP_TRANSPORT, JSON_FACTORY, CLIENT_SECRETS, scopes)
        .setAccessType("offline")
        .build();
  }

  private Calendar getCalendarApi(Credential credential) {
    return new Calendar.Builder(NET_HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
