package com.github.twomenteam.disastertracker.service.impl;

import com.github.twomenteam.disastertracker.model.db.User;
import com.github.twomenteam.disastertracker.model.db.Warning;
import com.github.twomenteam.disastertracker.repository.UserRepository;
import com.github.twomenteam.disastertracker.repository.WarningRepository;
import com.github.twomenteam.disastertracker.service.WarningService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;

@Service
public class WarningServiceImpl implements WarningService {
  private final WarningRepository warningRepository;

  @Autowired
  public WarningServiceImpl(WarningRepository warningRepository) {
    this.warningRepository = warningRepository;
  }

  @Override
  public Flux<Warning> retrieve(User user, LocalDateTime from, LocalDateTime to) {
    return warningRepository.findWarningsByUserIdAndCreatedAtBetween(user.getId(), from, to);
  }
}
