package com.github.twomenteam.disastertracker.jobs;

import com.github.twomenteam.disastertracker.kafka.WarningsKafkaConsumer;
import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.UserRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WarningManagerJob {
  private static final String TOPIC = "new-warnings";

  private final WarningsKafkaConsumer warningsKafkaConsumer;
  private final WarningRepository warningRepository;
  private final CalendarEventRepository calendarEventRepository;
  private final UserRepository userRepository;

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
                        .uuid(UUID.randomUUID().toString())
                        .calendarEventId(calendarEventId)
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                        .disasterEventId(disasterEventId)
                        .userId(calendarEvent.getUserId())
                        .build())
                  .flatMap(warning -> warningRepository
                      .save(warning)
                      .and(userRepository
                          .findById(warning.getUserId())
                          .map(User::getNotificationWebhookUrl)
                          .filter(Objects::nonNull)
                          .flatMap(webhook -> WebClient.create(webhook)
                              .get()
                              .uri(builder -> builder
                                  .queryParam("id", warning.getUuid())
                                  .build())
                              .retrieve()
                              .toBodilessEntity()
                              .onErrorResume(e -> Mono.empty())))
                      .thenReturn(warning)));
        })
        .blockLast();
    System.out.println("Exiting warning manager");
  }
}
