package com.github.twomenteam.disastertracker.controller;

import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.github.twomenteam.disastertracker.service.WarningService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(WarningController.class)
public class WarningControllerTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private WarningService warningService;

  @MockBean
  private AuthService authService;

  @Test
  void noApiKey() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("from", "2020-01-01 00:00:00")
            .queryParam("to", "2021-01-01 00:00:00")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void noFromParameter() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("apiKey", "some api key")
            .queryParam("to", "2021-01-01 00:00:00")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void noToParameter() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("apiKey", "some api key")
            .queryParam("from", "2021-01-01 00:00:00")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void fromIsAfterTo() {
    var apiKey = "some api key";
    when(authService.findUserByApiKey(apiKey))
        .thenReturn(Mono.just(User.builder().apiKey(apiKey).build()));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("apiKey", apiKey)
            .queryParam("from", "2021-01-01 00:00:00")
            .queryParam("to", "2020-01-01 00:00:00")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();

    verify(authService).findUserByApiKey(apiKey);
    verifyNoMoreInteractions(authService);
  }

  @Test
  void fromHasInvalidFormat() {
    var apiKey = "some api key";
    when(authService.findUserByApiKey(apiKey))
        .thenReturn(Mono.just(User.builder().apiKey(apiKey).build()));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("apiKey", apiKey)
            .queryParam("from", "2020-01-01")
            .queryParam("to", "2021-01-01 00:00:00")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();

    verify(authService).findUserByApiKey(apiKey);
    verifyNoMoreInteractions(authService);
  }

  @Test
  void toHasInvalidFormat() {
    var apiKey = "some api key";
    when(authService.findUserByApiKey(apiKey))
        .thenReturn(Mono.just(User.builder().apiKey(apiKey).build()));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("apiKey", apiKey)
            .queryParam("from", "2020-01-01 00:00:00")
            .queryParam("to", "2021-01-01")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();

    verify(authService).findUserByApiKey(apiKey);
    verifyNoMoreInteractions(authService, warningService);
  }

  @Test
  void success() {
    var warnings = new Warning[]{
        Warning.builder().build(),
        Warning.builder().build(),
        Warning.builder().build(),
        Warning.builder().build()
    };
    var apiKey = "some api key";
    var user = User.builder().apiKey(apiKey).build();
    var fromDateTime = LocalDate.of(2020, 1, 1).atStartOfDay();
    var toDateTime = LocalDate.of(2021, 1, 1).atStartOfDay();

    when(authService.findUserByApiKey(apiKey))
        .thenReturn(Mono.just(user));
    when(warningService.retrieve(user, fromDateTime, toDateTime))
        .thenReturn(Flux.fromArray(warnings));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/warning/get")
            .queryParam("apiKey", apiKey)
            .queryParam("from", fromDateTime.format(WarningController.FORMATTER))
            .queryParam("to", toDateTime.format(WarningController.FORMATTER))
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Warning.class).contains(warnings).hasSize(warnings.length);

    verify(authService).findUserByApiKey(apiKey);
    verify(warningService).retrieve(user, fromDateTime, toDateTime);
    verifyNoMoreInteractions(authService, warningService);
  }
}
