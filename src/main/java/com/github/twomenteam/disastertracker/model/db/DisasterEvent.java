package com.github.twomenteam.disastertracker.model.db;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@Table
public class DisasterEvent {
  @Id
  int id;
  String externalId;
  String description;
  boolean active;
  LocalDateTime start;
  LocalDateTime end;
  double latitude;
  double longitude;
}
