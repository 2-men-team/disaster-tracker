package com.github.twomenteam.disastertracker.model.db;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@Table
public class Warning {
  @Id
  int id;
  int userId;
  int calendarEventId;
  int disasterEventId;
  LocalDateTime createdAt;
  String uuid;
}
