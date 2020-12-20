package com.github.twomenteam.disastertracker.model;

import lombok.Value;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
public class DisasterEvent {
  @Id
  public int id;
  
  public String description;
  
  public boolean isActive;
  
  public LocalDateTime start;
  
  public LocalDateTime end;
  
  public double latitude;
  
  public double longitude;
}
