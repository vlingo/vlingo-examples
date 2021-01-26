package com.skyharbor.fleetcrew.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.skyharbor.fleetcrew.model.aircraft.AircraftState;

public class AircraftData {
  public final String id;
  public final String carrier;
  public final String flightNumber;
  public final String tailNumber;
  public final String gate;
  public final String fleetAgent;

  public static AircraftData from(final AircraftState state) {
    return new AircraftData(state);
  }

  public static List<AircraftData> from(final List<AircraftState> states) {
    return states.stream().map(AircraftData::from).collect(Collectors.toList());
  }

  public static AircraftData empty() {
    return new AircraftData(AircraftState.identifiedBy(null));
  }

  private AircraftData (final AircraftState state) {
    this.id = state.id;
    this.carrier = state.carrier;
    this.flightNumber = state.flightNumber;
    this.tailNumber = state.tailNumber;
    this.gate = state.gate;
    this.fleetAgent = state.fleetAgent;
  }

}
