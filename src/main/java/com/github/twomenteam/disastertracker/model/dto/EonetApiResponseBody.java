package com.github.twomenteam.disastertracker.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
public class EonetApiResponseBody {
  String title;
  String description;
  String link;
  List<Event> events;
  
  @Data
  @NoArgsConstructor
  public static class Event {
    String id;
    String title;
    String link;
    String closed;
    List<Category> categories;
    List<Source> sources;
    List<Geometry> geometry;
  }

  @Data
  @NoArgsConstructor
  public static class Category {
    String id;
    String title;
  }

  @Data
  @NoArgsConstructor
  public static class Source {
    String id;
    String url;
  }

  @Data
  @NoArgsConstructor
  public static class Geometry {
    String magnitudeValue;
    String magnitudeUnit;
    String date;
    String type;
    List<Object> coordinates;
  }
}
