package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.model.dto.FetchedCalendarEvent;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;
import com.github.twomenteam.disastertracker.service.CalendarEventService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {
  private final WarningRepository warningRepository;
  private final CalendarEventRepository calendarEventRepository;

  @Override
  public Mono<Void> updateCalendarEvents(FetchedCalendarEvent event) {
    var newCalendarEvent = event.getCalendarEvent();

    if (event.getStatus() == FetchedCalendarEvent.Status.DELETE) {
      return calendarEventRepository.deleteAllByGoogleIdAndUserId(
          newCalendarEvent.getGoogleId(), newCalendarEvent.getUserId());
    }

    return calendarEventRepository
        .findByGoogleIdAndUserId(newCalendarEvent.getGoogleId(), newCalendarEvent.getUserId())
        .flatMap(oldEvent -> warningRepository
            .deleteAllByCalendarEventId(oldEvent.getId())
            .thenReturn(oldEvent))
        .map(oldEvent -> oldEvent.toBuilder()
            .start(newCalendarEvent.getStart())
            .end(newCalendarEvent.getEnd())
            .summary(newCalendarEvent.getSummary())
            .location(newCalendarEvent.getLocation())
            .build()
            .withCoordinates(newCalendarEvent.getCoordinates()))
        .switchIfEmpty(Mono.just(newCalendarEvent))
        .flatMap(calendarEventRepository::save)
        .then();
  }
}
