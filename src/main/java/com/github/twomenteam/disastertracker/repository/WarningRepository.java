package com.github.twomenteam.disastertracker.repository;

import com.github.twomenteam.disastertracker.model.db.Warning;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarningRepository extends ReactiveCrudRepository<Warning, Integer> {
  Flux<Warning> findWarningsByUserIdAndCreatedAtBetween(int userId, LocalDateTime from, LocalDateTime to);
  Mono<Warning> findWarningByCalendarEventIdAndDisasterEventId(int calendarEventId, int disasterEventId);
  Mono<Void> deleteAllByCalendarEventId(int calendarEventId);
  Mono<Warning> findAllByUuid(String uuid);
}
