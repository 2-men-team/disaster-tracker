package com.github.twomenteam.disastertracker.model.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Table
public class User {
  @Id
  int id;
  String apiKey;

  @Embedded.Nullable
  AuthToken authToken;

  String notificationWebhookUrl;
  String nextSyncToken;
}
