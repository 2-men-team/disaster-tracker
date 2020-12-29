package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.dto.FetchedCalendarEvent;
import com.github.twomenteam.disastertracker.model.dto.RegisterResponseBody;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.CalendarEventService;
import com.github.twomenteam.disastertracker.service.GoogleApiService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
public class AuthControllerTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private AuthService authService;

  @MockBean
  private GoogleApiService googleApiService;

  @MockBean
  private CalendarEventService calendarEventService;

  @Test
  void successfulRegistration() {
    var webhook = "some webhook";
    var authUrl = "some auth url";
    var user = User.builder()
        .apiKey("some api key")
        .id(23)
        .notificationWebhookUrl(webhook)
        .build();
    var responseBody = RegisterResponseBody.builder()
        .apiKey(user.getApiKey())
        .googleAuthUrl(authUrl)
        .build();

    when(authService.createNewUser(webhook))
        .thenReturn(Mono.just(user));

    when(googleApiService.getAuthUrl(eq(user.getApiKey()), anyList()))
        .thenReturn(authUrl);

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/auth/register")
            .queryParam("webhook", webhook)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(RegisterResponseBody.class).isEqualTo(responseBody);

    verify(authService).createNewUser(webhook);
    verify(googleApiService).getAuthUrl(eq(user.getApiKey()), anyList());
    verifyNoMoreInteractions(authService, googleApiService, calendarEventService);
  }

  @Test
  void webHookIsNotSpecified() {
    var user = User.builder()
        .apiKey("some api key")
        .notificationWebhookUrl(null)
        .id(45)
        .build();
    var authUrl = "auth url";
    var expectedResponse = RegisterResponseBody.builder()
        .apiKey(user.getApiKey())
        .googleAuthUrl(authUrl)
        .build();

    when(authService.createNewUser(isNull()))
        .thenReturn(Mono.just(user));
    when(googleApiService.getAuthUrl(eq(user.getApiKey()), anyList()))
        .thenReturn(authUrl);

    webTestClient.get()
        .uri("/auth/register")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(RegisterResponseBody.class).isEqualTo(expectedResponse);

    verify(authService).createNewUser(isNull());
    verify(googleApiService).getAuthUrl(eq(user.getApiKey()), anyList());
    verifyNoMoreInteractions(authService, googleApiService, calendarEventService);
  }

  @Test
  void successfulReceiveCode() {
    var apiKey = "some api key";
    var code = "some code";
    var scope = "some scope";
    var scopes = Collections.singletonList(scope);
    var authToken = AuthToken.builder()
        .accessToken("some access token")
        .refreshToken("some refresh token")
        .expirationTimeInMillis(45L)
        .build();
    var user = User.builder()
        .id(45)
        .build()
        .withAuthToken(authToken);
    var calendarEvents = Flux.<FetchedCalendarEvent>empty();

    when(googleApiService.getTokenFromCode(code, scopes))
        .thenReturn(Mono.just(authToken));
    when(googleApiService.fetchAllEvents(authToken, scopes, user.getId()))
        .thenReturn(calendarEvents);
    when(googleApiService.watchCalendarEvents(authToken, apiKey, scopes))
        .thenReturn(Mono.empty());

    when(authService.saveAuthToken(apiKey, authToken))
        .thenReturn(Mono.just(user));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/auth/code")
            .queryParam("code", code)
            .queryParam("scope", scope)
            .queryParam("state", apiKey)
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();

    verify(googleApiService).getTokenFromCode(code, scopes);
    verify(googleApiService).fetchAllEvents(authToken, scopes, user.getId());
    verify(googleApiService).watchCalendarEvents(authToken, apiKey, scopes);

    verify(authService).saveAuthToken(apiKey, authToken);

    verifyNoMoreInteractions(authService, calendarEventService, googleApiService);
  }
}
