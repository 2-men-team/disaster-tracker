package com.github.twomenteam.disastertracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import reactor.blockhound.BlockHound;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
@EnableScheduling
public class DisasterTrackerApplication {
  public static void main(String[] args) {
    BlockHound.install();
    ReactorDebugAgent.init();
    ReactorDebugAgent.processExistingClasses();
    SpringApplication.run(DisasterTrackerApplication.class, args);
  }
}
