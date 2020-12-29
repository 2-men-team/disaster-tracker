package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.dto.FetchedCalendarEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CalendarEventService {
  Mono<Void> updateCalendarEvents(FetchedCalendarEvent event);
}
