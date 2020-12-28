package com.github.twomenteam.disastertracker.model.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Table
public class User {
  @Id
  int id;
  String apiKey;
  String accessToken;
  String refreshToken;
  Long expirationTimeInMillis;
  String notificationWebhookUrl;
  String nextSyncToken;

  public AuthToken getAuthToken() {
    return AuthToken.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expirationTimeInMillis(expirationTimeInMillis)
        .build();
  }

  public User withAuthToken(@NonNull AuthToken authToken) {
    return toBuilder()
        .accessToken(authToken.getAccessToken())
        .refreshToken(authToken.getRefreshToken())
        .expirationTimeInMillis(authToken.getExpirationTimeInMillis())
        .build();
  }
}
