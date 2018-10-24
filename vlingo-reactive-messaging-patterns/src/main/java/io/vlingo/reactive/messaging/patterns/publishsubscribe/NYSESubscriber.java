// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.reactive.messaging.patterns.publishsubscribe;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;

public class NYSESubscriber extends Actor implements QuotationProcessor {

    private final TestUntil until;

    public NYSESubscriber(final TestUntil until) {
        this.until = until;
    }

    @Override
    public void process(final PriceQuoted priceQuoted) {
        System.out.println("NYSESubscriber received " + priceQuoted);
        until.happened();
    }
}