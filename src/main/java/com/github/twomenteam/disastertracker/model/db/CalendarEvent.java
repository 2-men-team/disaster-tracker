package com.github.twomenteam.disastertracker.model.db;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@Table
public class CalendarEvent {
  @Id
  int id;
  int userId;
  String googleId;
  String summary;
  LocalDateTime start;
  LocalDateTime end;
  String location;
  @Embedded.Nullable
  Coordinates coordinates;
}
