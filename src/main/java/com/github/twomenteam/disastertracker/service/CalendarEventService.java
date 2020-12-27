package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CalendarEventService {
  Mono<Void> upsertCalendarEvents(Flux<CalendarEvent> events);
}
