package com.github.twomenteam.disastertracker;

import com.github.twomenteam.disastertracker.controller.AuthController;
import com.github.twomenteam.disastertracker.controller.CalendarEventController;
import com.github.twomenteam.disastertracker.controller.WarningController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DisasterTrackerApplicationTests {
  @Autowired AuthController authController;
  @Autowired CalendarEventController calendarEventController;
  @Autowired WarningController warningController;

  @Test
  void contextLoads() {
    assertThat(authController).isNotNull();
    assertThat(calendarEventController).isNotNull();
    assertThat(warningController).isNotNull();
  }
}
