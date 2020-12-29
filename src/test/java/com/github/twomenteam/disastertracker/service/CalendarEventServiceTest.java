package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.dto.FetchedCalendarEvent;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;
import com.github.twomenteam.disastertracker.service.impl.CalendarEventServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CalendarEventService.class)
public class CalendarEventServiceTest {
  @InjectMocks
  private CalendarEventServiceImpl calendarEventService;

  @Mock
  private WarningRepository warningRepository;

  @Mock
  private CalendarEventRepository calendarEventRepository;

  private static final CalendarEvent OLD_EVENT = CalendarEvent.builder()
      .id(1)
      .googleId("update")
      .userId(1)
      .location("old location")
      .summary("old summary")
      .build();

  private static final FetchedCalendarEvent UPDATE_EVENT = FetchedCalendarEvent.builder()
      .calendarEvent(OLD_EVENT.toBuilder()
          .location("new location")
          .summary("new summary")
          .build())
      .status(FetchedCalendarEvent.Status.UPDATE)
      .build();

  private static final FetchedCalendarEvent INSERT_EVENT = FetchedCalendarEvent.builder()
      .calendarEvent(CalendarEvent.builder()
          .id(2)
          .googleId("new")
          .userId(3)
          .build())
      .build();

  private static final FetchedCalendarEvent DELETE_EVENT = FetchedCalendarEvent.builder()
      .calendarEvent(OLD_EVENT)
      .status(FetchedCalendarEvent.Status.DELETE)
      .build();

  @BeforeEach
  void setUpMocks() {
    var deleteEvent = DELETE_EVENT.getCalendarEvent();
    var updateEvent = UPDATE_EVENT.getCalendarEvent();
    var insertEvent = INSERT_EVENT.getCalendarEvent();

    // Mock deletion for |deleteEvent|.
    when(calendarEventRepository.deleteAllByGoogleIdAndUserId(
        deleteEvent.getGoogleId(), deleteEvent.getUserId()))
        .thenReturn(Mono.empty());

    // Mock lookup for non-existing event.
    when(calendarEventRepository.findByGoogleIdAndUserId(
        insertEvent.getGoogleId(), insertEvent.getUserId()))
        .thenReturn(Mono.empty());

    // Mock lookup for existing event.
    when(calendarEventRepository.findByGoogleIdAndUserId(
        updateEvent.getGoogleId(), updateEvent.getUserId()))
        .thenReturn(Mono.just(OLD_EVENT));

    // Mock update.
    when(calendarEventRepository.save(eq(updateEvent)))
        .thenReturn(Mono.just(updateEvent));

    // Mock insert.
    when(calendarEventRepository.save(insertEvent))
        .thenReturn(Mono.just(insertEvent));

    when(warningRepository.deleteAllByCalendarEventId(updateEvent.getId()))
        .thenReturn(Mono.empty());
  }

  @Test
  void update() {
    var event = UPDATE_EVENT.getCalendarEvent();

    StepVerifier.create(calendarEventService.updateCalendarEvents(UPDATE_EVENT))
        .expectSubscription()
        .verifyComplete();

    verify(calendarEventRepository).findByGoogleIdAndUserId(
        event.getGoogleId(), event.getUserId());
    verify(warningRepository).deleteAllByCalendarEventId(event.getId());
    verify(calendarEventRepository).save(eq(event));
    verifyNoMoreInteractions(calendarEventRepository, warningRepository);
  }

  @Test
  void insert() {
    var event = INSERT_EVENT.getCalendarEvent();

    StepVerifier.create(calendarEventService.updateCalendarEvents(INSERT_EVENT))
        .expectSubscription()
        .verifyComplete();

    verify(calendarEventRepository).findByGoogleIdAndUserId(
        event.getGoogleId(), event.getUserId());
    verify(calendarEventRepository).save(event);
    verifyNoMoreInteractions(calendarEventRepository, warningRepository);
  }

  @Test
  void delete() {
    var event = DELETE_EVENT.getCalendarEvent();

    StepVerifier.create(calendarEventService.updateCalendarEvents(DELETE_EVENT))
        .expectSubscription()
        .verifyComplete();

    verify(calendarEventRepository).deleteAllByGoogleIdAndUserId(
        event.getGoogleId(), event.getUserId());
    verifyNoMoreInteractions(calendarEventRepository, warningRepository);
  }
}
