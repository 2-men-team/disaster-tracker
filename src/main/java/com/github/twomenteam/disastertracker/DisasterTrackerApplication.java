package com.github.twomenteam.disastertracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DisasterTrackerApplication {
  public static void main(String[] args) {
    SpringApplication.run(DisasterTrackerApplication.class, args);
  }
}
