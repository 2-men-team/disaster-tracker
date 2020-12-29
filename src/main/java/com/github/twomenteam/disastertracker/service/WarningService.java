package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.model.db.User;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarningService {
  Flux<Warning> retrieve(User user, LocalDateTime from, LocalDateTime to);
  Mono<Warning> getByUuid(String uuid);
}
