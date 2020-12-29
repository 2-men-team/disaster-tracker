package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.Utils;
import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.repository.UserRepository;
import com.github.twomenteam.disastertracker.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthService.class)
public class AuthServiceTest {
  @InjectMocks
  private AuthServiceImpl authService;

  @Mock
  private UserRepository userRepository;

  private static final User USER = User.builder()
      .apiKey("some api key")
      .build();

  @Test
  void findUserByApiKeySimple() {
    var duration = Duration.ofSeconds(2);
    var mono = Mono.delay(duration).thenReturn(USER);

    when(userRepository.findUserByApiKey(USER.getApiKey()))
        .thenReturn(mono);

    StepVerifier.create(authService.findUserByApiKey(USER.getApiKey()))
        .expectSubscription()
        .expectNoEvent(duration)
        .thenAwait(duration)
        .expectNext(USER)
        .verifyComplete();

    verify(userRepository, only()).findUserByApiKey(USER.getApiKey());
  }

  @Test
  void findUserByApiKeyTimeout() {
    when(userRepository.findUserByApiKey(USER.getApiKey()))
        .thenReturn(Mono.never());

    StepVerifier.withVirtualTime(() ->
        authService
            .findUserByApiKey(USER.getApiKey()))
        .expectSubscription()
        .expectNoEvent(Utils.TIMEOUT_DURATION)
        .thenAwait(Utils.TIMEOUT_DURATION)
        .expectTimeout(Utils.TIMEOUT_DURATION)
        .verify();

    verify(userRepository, only()).findUserByApiKey(USER.getApiKey());
  }

  @Test
  void findUserByApiKeyRetry() {
    var attempt = new AtomicInteger(0);

    var mono = Mono.<User>create(sink -> {
      if (attempt.getAndIncrement() <= 0) {
        sink.error(new RuntimeException("Some error"));
      } else {
        sink.success(USER);
      }
    });

    when(userRepository.findUserByApiKey(USER.getApiKey()))
        .thenReturn(mono);

    StepVerifier.withVirtualTime(() ->
        authService
            .findUserByApiKey(USER.getApiKey()))
        .expectSubscription()
        .expectNoEvent(Utils.DEFAULT_RETRY.minBackoff)
        .thenAwait(Utils.DEFAULT_RETRY.minBackoff)
        .expectNext(USER)
        .verifyComplete();

    verify(userRepository, only()).findUserByApiKey(USER.getApiKey());
  }
}
