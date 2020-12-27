package com.github.twomenteam.disastertracker.model.db;

import com.google.maps.model.LatLng;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Coordinates {
  double latitude;
  double longitude;

  public static Coordinates fromLatLng(LatLng latLng) {
    return new Coordinates(latLng.lat, latLng.lng);
  }
}
