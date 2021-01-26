package com.vgoairlines.flightplanning.model.flight;

import io.vlingo.symbio.store.object.StateObject;

public final class FlightState extends StateObject {

  public final String id;
  public final AircraftId aircraftId;
  public final Schedule schedule;
  public final boolean cancelled;

  public static FlightState identifiedBy(final String id) {
    return new FlightState(id, null, null);
  }

  public FlightState (final String id, final AircraftId aircraftId, final Schedule schedule) {
    this(id, aircraftId, schedule, false);
  }

  public FlightState (final String id, final AircraftId aircraftId, final Schedule schedule, final boolean cancelled) {
    this.id = id;
    this.aircraftId = aircraftId;
    this.schedule = schedule;
    this.cancelled = cancelled;
  }

  public FlightState schedule(final AircraftId aircraftId, final Schedule schedule) {
    return new FlightState(this.id, aircraftId, schedule);
  }

  public FlightState reschedule(final Schedule schedule) {
    return new FlightState(this.id, this.aircraftId, schedule);
  }

  public FlightState cancel() {
    return new FlightState(this.id, this.aircraftId, this.schedule, true);
  }

}
