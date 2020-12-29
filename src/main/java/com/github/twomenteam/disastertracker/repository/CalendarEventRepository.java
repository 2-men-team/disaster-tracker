package com.github.twomenteam.disastertracker.repository;

import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.db.DisasterEvent;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CalendarEventRepository extends ReactiveCrudRepository<CalendarEvent, Integer> {
  Flux<CalendarEvent> findAllByStartBetween(LocalDateTime from, LocalDateTime to);
  Mono<CalendarEvent> findByGoogleIdAndUserId(String googleId, int userId);
  Mono<Void> deleteAllByGoogleIdAndUserId(String googleId, int userId);
}
