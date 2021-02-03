// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package com.vgoairlines.flightplanning.model.flight;

import java.time.LocalDateTime;

public class Arrival {

    public final Airport airport;
    public final LocalDateTime plannedFor;

    public static Arrival on(final Airport airport, final LocalDateTime planedFor) {
        return new Arrival(airport, planedFor);
    }

    private Arrival(final Airport airport, final LocalDateTime plannedFor) {
        this.airport = airport;
        this.plannedFor = plannedFor;
    }

}
