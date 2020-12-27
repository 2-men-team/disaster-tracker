package com.github.twomenteam.disastertracker.model.dto;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
@Builder(toBuilder = true)
public class CalendarEvents {
  String nextSyncToken;
  Flux<CalendarEvent> events;
}
