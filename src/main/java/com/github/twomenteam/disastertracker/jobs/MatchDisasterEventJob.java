package com.github.twomenteam.disastertracker.jobs;

import com.github.twomenteam.disastertracker.kafka.WarningsKafkaProducer;
import com.github.twomenteam.disastertracker.model.db.CalendarEvent;
import com.github.twomenteam.disastertracker.model.db.DisasterEvent;
import com.github.twomenteam.disastertracker.repository.CalendarEventRepository;
import com.github.twomenteam.disastertracker.repository.DisasterEventRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class MatchDisasterEventJob {
  private static final int LOOKAHEAD_DAYS = 7;
  private static final int DISASTER_RADIUS = 20;

  private final CalendarEventRepository calendarEventRepository;
  private final DisasterEventRepository disasterEventRepository;
  private final WarningsKafkaProducer warningsKafkaProducer;

  @Autowired
  public MatchDisasterEventJob(CalendarEventRepository calendarEventRepository,
                               DisasterEventRepository disasterEventRepository,
                               WarningsKafkaProducer warningsKafkaProducer) {
    this.calendarEventRepository = calendarEventRepository;
    this.disasterEventRepository = disasterEventRepository;
    this.warningsKafkaProducer = warningsKafkaProducer;
  }

  public boolean matchEvent(CalendarEvent calendarEvent, DisasterEvent disasterEvent) {
    double distance = distance(
        calendarEvent.getCoordinates().getLatitude(),
        calendarEvent.getCoordinates().getLongitude(),
        disasterEvent.getLatitude(), disasterEvent.getLongitude());
    return distance < DISASTER_RADIUS;
  }
  
  @Scheduled(fixedDelay = 1000*60*60)
  public void matchDisasterEvents() {
    var from = LocalDateTime.now(ZoneOffset.UTC);
    var to   = from.plusDays(LOOKAHEAD_DAYS);
    var calendarEvents = calendarEventRepository.findAllByStartBetween(from, to).collectList().block();
    var disasterEvents = disasterEventRepository.findAllByStartBetween(from, to).collectList().block();
    var matchedEvents = new ArrayList<Tuple2<Integer, Integer>>();
    for (var calendarEvent : calendarEvents) {
      for (var disasterEvent : disasterEvents) {
        if (matchEvent(calendarEvent, disasterEvent)) {
          matchedEvents.add(Tuples.of(calendarEvent.getId(), disasterEvent.getId()));
        }
      }
    }

    warningsKafkaProducer.sendMessages(WarningsKafkaProducer.TOPIC, Flux.fromIterable(matchedEvents)).blockLast();
  }

  private static double distance(double lat1, double lon1, double lat2, double lon2) {
    double theta = lon1 - lon2;
    double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
    dist = Math.acos(dist);
    dist = Math.toDegrees(dist);
    dist = dist * 60 * 1.1515;
    return dist * 1.609344;
  }
}
