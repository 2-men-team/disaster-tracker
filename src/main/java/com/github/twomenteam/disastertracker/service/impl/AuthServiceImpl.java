package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.Utils;
import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.repository.UserRepository;
import com.github.twomenteam.disastertracker.service.AuthService;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.CalendarScopes;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;

  @Override
  public Mono<User> findUserByApiKey(String apiKey) {
    return userRepository
        .findUserByApiKey(apiKey)
        .timeout(Utils.TIMEOUT_DURATION)
        .retryWhen(Utils.DEFAULT_RETRY);
  }

  @Override
  public Mono<User> createNewUser(String webhook) {
    return userRepository.save(
        User.builder()
            .apiKey(UUID.randomUUID().toString())
            .notificationWebhookUrl(webhook)
            .build());
  }

  @Override
  public Mono<User> saveAuthToken(String apiKey, AuthToken authToken) {
    return findUserByApiKey(apiKey)
        .flatMap(user -> userRepository.save(user.withAuthToken(authToken)));
  }
}
