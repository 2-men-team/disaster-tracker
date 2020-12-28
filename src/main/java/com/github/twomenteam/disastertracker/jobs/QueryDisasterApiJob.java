package com.github.twomenteam.disastertracker.jobs;

import com.github.twomenteam.disastertracker.model.db.DisasterEvent;
import com.github.twomenteam.disastertracker.model.dto.EonetApiResponseBody;
import com.github.twomenteam.disastertracker.repository.DisasterEventRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueryDisasterApiJob {
  private static final String EONET_REQUEST_URL = "https://eonet.sci.gsfc.nasa.gov/api/v3/events";

  private final DisasterEventRepository disasterEventRepository;

  @Scheduled(fixedRate = 1000*60*60*12)
  public void queryDisasterApi() {
    EonetApiResponseBody responseBody = new RestTemplate().getForObject(EONET_REQUEST_URL, EonetApiResponseBody.class);

    List<DisasterEvent> disasterEvents = responseBody.getEvents().stream().map(event -> {
      boolean isActive = event.getClosed() == null;
      EonetApiResponseBody.Geometry geometry = event.getGeometry().get(event.getGeometry().size() - 1); // get the most latest
      return DisasterEvent.builder()
          .description(event.getTitle())
          .externalId(event.getId())
          .isActive(isActive)
          .start(LocalDateTime.parse(geometry.getDate()))
          .end(isActive ? null : LocalDateTime.parse(event.getClosed()))
          .latitude(geometry.getCoordinates().get(0))
          .longitude(geometry.getCoordinates().get(1))
          .build();
    }).collect(Collectors.toList());

    disasterEvents.forEach((disasterEvent) -> {
      DisasterEvent event = disasterEventRepository.findByExternalId(disasterEvent.getExternalId()).block();
      if (event == null) {
        disasterEventRepository.save(disasterEvent);
      }
    });
  }
}