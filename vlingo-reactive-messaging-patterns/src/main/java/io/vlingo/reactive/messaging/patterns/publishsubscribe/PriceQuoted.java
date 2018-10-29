// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.reactive.messaging.patterns.publishsubscribe;


import io.vlingo.actors.pubsub.Message;

public class PriceQuoted implements Message {

    private final String ticker;
    private final Money money;

    public PriceQuoted(final String ticker, final Money money) {
        this.ticker = ticker;
        this.money = money;
    }

    public String toString() {
        return "PriceQuoted[ticker=" + ticker + " money=" + money +"]";
    }
}
