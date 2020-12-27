package com.github.twomenteam.disastertracker.model.dto;

import java.util.List;

import lombok.Value;

@Value
public class EonetApiResponseBody {
  String title;
  String description;
  String link;
  List<Event> events;
  
  @Value
  public static class Event {
    String id;
    String title;
    String link;
    String closed;
    List<Category> categories;
    List<Source> sources;
    List<Geometry> geometry;
  }

  @Value
  public static class Category {
    String id;
    String title;
  }

  @Value
  public static class Source {
    String id;
    String url;
  }

  @Value
  public static class Geometry {
    String magnitudeValue;
    String magnitudeUnit;
    String date;
    String type;
    List<Double> coordinates;
  }
}
