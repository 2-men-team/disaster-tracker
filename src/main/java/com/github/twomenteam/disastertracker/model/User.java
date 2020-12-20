package com.github.twomenteam.disastertracker.model;

import lombok.Value;
import org.springframework.data.annotation.Id;

@Value
public class User {
  @Id
  public int id;
  
  public String apiKey;
  
  public String accessToken;
  
  public String refreshToken;
  
  public String notificationWebhookUrl;

  @Override
  public String toString() {
    return String.format("User[id=%d, apiKey=%s, accessToken=%s, refreshToken=%s]", id, apiKey, accessToken, refreshToken);
  }
}
