package com.github.twomenteam.disastertracker.repository;

import com.github.twomenteam.disastertracker.model.db.DisasterEvent;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DisasterEventRepository extends ReactiveCrudRepository<DisasterEvent, Integer> {
  Mono<DisasterEvent> findByExternalId(String externalId);
  Flux<DisasterEvent> findAllByActive(boolean isActive);
}
