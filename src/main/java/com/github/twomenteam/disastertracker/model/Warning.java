package com.github.twomenteam.disastertracker.model;

import lombok.Value;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
public class Warning {
  @Id
  public int id;
  
  public int userId;
  
  public int calendarId;
  
  public LocalDateTime createdAt;
}
