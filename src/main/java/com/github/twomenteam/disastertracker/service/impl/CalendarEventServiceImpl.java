package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;
import com.github.twomenteam.disastertracker.service.CalendarEventService;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {
  private final WarningRepository warningRepository;
  private final CalendarEventRepository calendarEventRepository;

  @Override
  public Mono<Void> upsertCalendarEvents(Flux<CalendarEvent> events) {
    return events
        .flatMap(newEvent -> calendarEventRepository
            .findByGoogleIdAndUserId(newEvent.getGoogleId(), newEvent.getUserId())
            .flatMap(oldEvent -> warningRepository
                .deleteAllByCalendarEventId(oldEvent.getId())
                .then(Mono.just(oldEvent)))
            .map(oldEvent -> oldEvent.toBuilder()
                .start(newEvent.getStart())
                .end(newEvent.getEnd())
                .summary(newEvent.getSummary())
                .location(newEvent.getLocation())
                .build()
                .withCoordinates(newEvent.getCoordinates()))
            .switchIfEmpty(Mono.just(newEvent))
            .flatMap(calendarEventRepository::save))
        .then();
  }

  @Override
  public Mono<Void> removeCalendarEvents(Flux<CalendarEvent> events) {
    return events
        .flatMap(event -> calendarEventRepository
            .deleteAllByGoogleIdAndUserId(event.getGoogleId(), event.getUserId()))
        .then();
  }
}
