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

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
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
    var mono = Mono.delay(Duration.ofSeconds(1))
        .thenReturn(USER);

    when(userRepository.findUserByApiKey(USER.getApiKey()))
        .thenReturn(mono);

    StepVerifier.create(authService.findUserByApiKey(USER.getApiKey()))
        .expectSubscription()
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(USER)
        .expectComplete()
        .verify();

    verify(userRepository, only()).findUserByApiKey(USER.getApiKey());
  }

  @Test
  void findUserByApiKeyTimeout() {
    when(userRepository.findUserByApiKey(USER.getApiKey()))
        .thenReturn(Mono.never());

    StepVerifier.create(authService.findUserByApiKey(USER.getApiKey()))
        .expectSubscription()
        .expectTimeout(Utils.TIMEOUT_DURATION)
        .verify();

    verify(userRepository, only()).findUserByApiKey(USER.getApiKey());
  }


}
