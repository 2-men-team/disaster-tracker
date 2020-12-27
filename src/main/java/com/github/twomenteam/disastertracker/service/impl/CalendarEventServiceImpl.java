package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;
import com.github.twomenteam.disastertracker.service.CalendarEventService;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CalendarEventServiceImpl implements CalendarEventService {
  private final WarningRepository warningRepository;
  private final CalendarEventRepository calendarEventRepository;

  public CalendarEventServiceImpl(WarningRepository warningRepository, CalendarEventRepository calendarEventRepository) {
    this.warningRepository = warningRepository;
    this.calendarEventRepository = calendarEventRepository;
  }

  @Override
  public Mono<Void> upsertCalendarEvents(Flux<CalendarEvent> events) {
    return events
        .flatMap(newEvent -> calendarEventRepository
            .findByGoogleId(newEvent.getGoogleId())
            .flatMap(oldEvent -> warningRepository
                .deleteAllByCalendarEventId(oldEvent.getId())
                .then(Mono.just(oldEvent)))
            .map(oldEvent -> oldEvent.toBuilder()
                .coordinates(newEvent.getCoordinates())
                .start(newEvent.getStart())
                .end(newEvent.getEnd())
                .summary(newEvent.getSummary())
                .location(newEvent.getLocation())
                .build())
            .switchIfEmpty(Mono.just(newEvent))
            .flatMap(calendarEventRepository::save))
        .then();
  }
}
