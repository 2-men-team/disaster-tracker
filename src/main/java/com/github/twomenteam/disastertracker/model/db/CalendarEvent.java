package com.github.twomenteam.disastertracker.model.db;

import lombok.Builder;
import lombok.NonNull;
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
  double latitude;
  double longitude;

  public Coordinates getCoordinates() {
    return Coordinates.builder()
        .latitude(latitude)
        .longitude(longitude)
        .build();
  }

  public CalendarEvent withCoordinates(@NonNull Coordinates coordinates) {
    return toBuilder()
        .latitude(coordinates.getLatitude())
        .longitude(coordinates.getLongitude())
        .build();
  }
}
