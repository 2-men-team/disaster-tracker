package com.github.twomenteam.disastertracker.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterResponseBody {
  String apiKey;
  String googleAuthUrl;
}
