package com.github.twomenteam.disastertracker.model.db;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AuthToken {
  String accessToken;
  String refreshToken;
  Long expirationTimeInMillis;
}
