package com.github.twomenteam.disastertracker.jobs;

import com.github.twomenteam.disastertracker.kafka.WarningsKafkaConsumer;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WarningManagerJob {
  private static final String TOPIC = "new-warnings";

  private final WarningsKafkaConsumer warningsKafkaConsumer;
  private final WarningRepository warningRepository;
  private final CalendarEventRepository calendarEventRepository;

  @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
  public void listenToNewWarnings() {
    System.out.println("Entering warning manager");
    warningsKafkaConsumer
        .consumeMessages(TOPIC)
        .doOnNext(message -> System.out.println("Got new warning " + message))
        .flatMap(record -> {
          int calendarEventId = record.key();
          int disasterEventId = record.value();
          return warningRepository
              .findWarningByCalendarEventIdAndDisasterEventId(calendarEventId, disasterEventId)
              .switchIfEmpty(calendarEventRepository
                  .findById(calendarEventId)
                  .map(calendarEvent -> Warning.builder()
                      .calendarEventId(calendarEventId)
                      .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                      .disasterEventId(disasterEventId)
                      .userId(calendarEvent.getUserId())
                      .build())
                  .flatMap(warningRepository::save));
        })
        .blockLast();
    System.out.println("Exiting warning manager");
  }
}
