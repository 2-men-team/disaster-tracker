package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.AuthToken;
import com.github.twomenteam.disastertracker.model.dto.RegisterResponseBody;
import com.github.twomenteam.disastertracker.model.db.User;
import com.google.api.client.auth.oauth2.Credential;

import reactor.core.publisher.Mono;

public interface AuthService {
  Mono<User> findUserByApiKey(String apiKey);
  Mono<User> createNewUser(String webhook);
  Mono<User> saveAuthToken(String apiKey, AuthToken authToken);
}
