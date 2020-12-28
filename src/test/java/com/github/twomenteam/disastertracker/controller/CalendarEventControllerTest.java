package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.CalendarEventService;
import com.github.twomenteam.disastertracker.service.GoogleApiService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(CalendarEventController.class)
public class CalendarEventControllerTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private AuthService authService;

  @MockBean
  private GoogleApiService googleApiService;

  @MockBean
  private CalendarEventService calendarEventService;

  @Test
  void receiveCalendarEventSyncState() {
    webTestClient.post()
        .uri("/event/receive")
        .header(CalendarEventController.STATE_HEADER_NAME, CalendarEventController.SYNC_STATE)
        .header(CalendarEventController.TOKEN_HEADER_NAME, "some api key")
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();

    verifyNoInteractions(calendarEventService, authService, googleApiService);
  }

  @Test
  void receiveCalendarEventNoTokenRefresh() {
    var events = Flux.just(
        CalendarEvent.builder()
            .googleId("a")
            .build(),
        CalendarEvent.builder()
            .googleId("b")
            .build(),
        CalendarEvent.builder()
            .googleId("c")
            .build());
    var user = User.builder()
        .apiKey("some api key")
        .id(34)
        .build()
        .withAuthToken(AuthToken.builder().build());

    when(authService.findUserByApiKey(user.getApiKey()))
        .thenReturn(Mono.just(user));

    when(googleApiService.refreshToken(eq(user.getAuthToken()), anyList()))
        .thenReturn(Mono.empty());
    when(googleApiService.fetchLatestEvents(
        eq(user.getAuthToken()), notNull(), anyList(), eq(user.getId())))
        .thenReturn(events);

    when(calendarEventService.upsertCalendarEvents(events))
        .thenReturn(Mono.empty());
    when(calendarEventService.removeCalendarEvents(events))
        .thenReturn(Mono.empty());

    // Test |exists| state.
    webTestClient.post()
        .uri("/event/receive")
        .header(CalendarEventController.TOKEN_HEADER_NAME, user.getApiKey())
        .header(CalendarEventController.STATE_HEADER_NAME, CalendarEventController.EXISTS_STATE)
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();

    verify(authService).findUserByApiKey(user.getApiKey());

    verify(googleApiService).refreshToken(eq(user.getAuthToken()), anyList());
    verify(googleApiService).fetchLatestEvents(eq(user.getAuthToken()), notNull(), anyList(), eq(user.getId()));

    verify(calendarEventService).upsertCalendarEvents(events);

    verifyNoMoreInteractions(authService, googleApiService, calendarEventService);
    clearInvocations(authService, googleApiService, calendarEventService);

    // Test |not_exists| state.
    webTestClient.post()
        .uri("/event/receive")
        .header(CalendarEventController.TOKEN_HEADER_NAME, user.getApiKey())
        .header(CalendarEventController.STATE_HEADER_NAME, CalendarEventController.NOT_EXISTS_STATE)
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();

    verify(authService).findUserByApiKey(user.getApiKey());

    verify(googleApiService).refreshToken(eq(user.getAuthToken()), anyList());
    verify(googleApiService).fetchLatestEvents(eq(user.getAuthToken()), notNull(), anyList(), eq(user.getId()));

    verify(calendarEventService).removeCalendarEvents(events);

    verifyNoMoreInteractions(authService, googleApiService, calendarEventService);
  }

  @Test
  void receiveCalendarEventWithTokenRefresh() {
    var events = Flux.just(
        CalendarEvent.builder()
            .googleId("a")
            .build(),
        CalendarEvent.builder()
            .googleId("b")
            .build(),
        CalendarEvent.builder()
            .googleId("c")
            .build());
    var user = User.builder()
        .apiKey("some api key")
        .id(34)
        .build()
        .withAuthToken(AuthToken.builder().build());
    var newAuthToken = AuthToken.builder()
        .refreshToken("some new refresh token")
        .accessToken("some new access token")
        .build();
    var newUser = user.withAuthToken(newAuthToken);

    when(authService.findUserByApiKey(user.getApiKey()))
        .thenReturn(Mono.just(user));
    when(authService.saveAuthToken(user.getApiKey(), newAuthToken))
        .thenReturn(Mono.just(newUser));

    when(googleApiService.refreshToken(eq(user.getAuthToken()), anyList()))
        .thenReturn(Mono.just(newAuthToken));
    when(googleApiService.fetchLatestEvents(eq(newAuthToken), notNull(), anyList(), eq(user.getId())))
        .thenReturn(events);

    when(calendarEventService.upsertCalendarEvents(events))
        .thenReturn(Mono.empty());
    when(calendarEventService.removeCalendarEvents(events))
        .thenReturn(Mono.empty());

    // Test |exists| state.
    webTestClient.post()
        .uri("/event/receive")
        .header(CalendarEventController.TOKEN_HEADER_NAME, user.getApiKey())
        .header(CalendarEventController.STATE_HEADER_NAME, CalendarEventController.EXISTS_STATE)
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();

    verify(authService).findUserByApiKey(user.getApiKey());
    verify(authService).saveAuthToken(user.getApiKey(), newAuthToken);

    verify(googleApiService).refreshToken(eq(user.getAuthToken()), anyList());
    verify(googleApiService).fetchLatestEvents(eq(newAuthToken), notNull(), anyList(), eq(user.getId()));

    verify(calendarEventService).upsertCalendarEvents(events);

    verifyNoMoreInteractions(authService, googleApiService, calendarEventService);
    clearInvocations(authService, googleApiService, calendarEventService);

    // Test |not_exists| state.
    webTestClient.post()
        .uri("/event/receive")
        .header(CalendarEventController.TOKEN_HEADER_NAME, user.getApiKey())
        .header(CalendarEventController.STATE_HEADER_NAME, CalendarEventController.NOT_EXISTS_STATE)
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();

    verify(authService).findUserByApiKey(user.getApiKey());
    verify(authService).saveAuthToken(newUser.getApiKey(), newAuthToken);

    verify(googleApiService).refreshToken(eq(user.getAuthToken()), anyList());
    verify(googleApiService).fetchLatestEvents(eq(newAuthToken), notNull(), anyList(), eq(user.getId()));

    verify(calendarEventService).removeCalendarEvents(events);

    verifyNoMoreInteractions(authService, googleApiService, calendarEventService);
  }
}
