// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.reactive.messaging.patterns.pointtopointchannel;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;

/**
 * PeerNodeActor represents point-to-point messaging that underlies all {@link Actor} communication where
 * messages between two peers will be received in the order that they are sent.
 */
public class PeerNodeActor 
extends Actor
implements PointToPointProcessor
{
    private final TestUntil testUntil;
    private int lastOrderedMessageId = 0;
    
    public PeerNodeActor( TestUntil testUntil )
    {
        this.testUntil = testUntil;
    }

    /* @see io.vlingo.reactive.messaging.patterns.pointtopointchannel.PointToPointProcessor#peerMessage(java.lang.String) */
    @Override
    public void process( Integer messageId )
    {
        logger().log( String.format( "peerMessage %d received", messageId ));
        if ( messageId < lastOrderedMessageId ) throw new IllegalStateException( "Message id out of order" );
        lastOrderedMessageId = messageId;
        testUntil.happened();
    }

}
