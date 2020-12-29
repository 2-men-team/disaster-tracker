package com.github.twomenteam.disastertracker.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class WarningMessage {
  String calendarEventGoogleId;
  String disasterEventId;
  String warningUuid;
  String calendarEventSummary;
  String disasterEventSummary;
}
