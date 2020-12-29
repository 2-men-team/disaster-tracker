package com.github.twomenteam.disastertracker.service;

import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.dto.WarningMessage;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarningService {
  Flux<WarningMessage> retrieve(User user, LocalDateTime from, LocalDateTime to);
  Mono<WarningMessage> getByUuid(String uuid);
}
