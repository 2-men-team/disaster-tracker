package com.github.twomenteam.disastertracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcAuditing
public class DisasterTrackerApplication {
  public static void main(String[] args) {
    SpringApplication.run(DisasterTrackerApplication.class, args);
  }
}
