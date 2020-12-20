package com.github.twomenteam.disastertracker.model;

import lombok.Value;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
public class CalendarEvent {
  @Id
  public int id;
  
  public int userId;
  
  public String googleId;
  
  public String summary;
  
  public LocalDateTime start;
  
  public LocalDateTime end;
  
  public String location;
  
  public double latitude;
  
  public double longitude;
}
