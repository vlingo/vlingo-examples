// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package com.skyharbor.aircraftmonitoring.infrastructure;

public class AircraftData {

  public final String tailNumber;
  public final String carrier;

  public AircraftData(final String tailNumber, final String carrier) {
    this.tailNumber = tailNumber;
    this.carrier = carrier;
  }
}
