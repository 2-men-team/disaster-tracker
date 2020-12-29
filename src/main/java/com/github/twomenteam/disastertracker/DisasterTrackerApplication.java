package com.github.twomenteam.disastertracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
@EnableScheduling
public class DisasterTrackerApplication {
  public static void main(String[] args) {
    ReactorDebugAgent.init();
    ReactorDebugAgent.processExistingClasses();
    SpringApplication.run(DisasterTrackerApplication.class, args);
  }
}
