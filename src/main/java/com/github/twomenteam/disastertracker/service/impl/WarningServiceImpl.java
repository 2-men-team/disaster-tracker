package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.model.dto.WarningMessage;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.DisasterEventRepository;
import com.github.twomenteam.disastertracker.repository.UserRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;
import com.github.twomenteam.disastertracker.service.WarningService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {
  private final WarningRepository warningRepository;
  private final DisasterEventRepository disasterEventRepository;
  private final CalendarEventRepository calendarEventRepository;

  @Override
  public Flux<WarningMessage> retrieve(User user, LocalDateTime from, LocalDateTime to) {
    return warningRepository
        .findWarningsByUserIdAndCreatedAtBetween(user.getId(), from, to)
        .flatMap(this::getWarningMessageFromWarning);
  }

  @Override
  public Mono<WarningMessage> getByUuid(String uuid) {
    return warningRepository
        .findAllByUuid(uuid)
        .flatMap(this::getWarningMessageFromWarning);
  }

  private Mono<WarningMessage> getWarningMessageFromWarning(Warning warning) {
    return disasterEventRepository
        .findById(warning.getDisasterEventId())
        .zipWith(calendarEventRepository.findById(warning.getCalendarEventId()))
        .map(tuple -> {
          var disasterEvent = tuple.getT1();
          var calendarEvent = tuple.getT2();
          return WarningMessage.builder()
              .calendarEventGoogleId(calendarEvent.getGoogleId())
              .calendarEventSummary(calendarEvent.getSummary())
              .warningUuid(warning.getUuid())
              .disasterEventId(disasterEvent.getExternalId())
              .disasterEventSummary(disasterEvent.getDescription())
              .build();
        });
  }
}
