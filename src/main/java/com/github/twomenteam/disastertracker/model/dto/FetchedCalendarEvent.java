package com.github.twomenteam.disastertracker.model.dto;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class FetchedCalendarEvent {
  Status status;
  CalendarEvent calendarEvent;

  public enum Status {
    UPDATE, DELETE
  }
}
