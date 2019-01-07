// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.reactive.messaging.patterns.recipientlist;

import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;

/**
 * RecipientListTest driver for this recipient list example.
 */
public class RecipientListTest
{
    public static final String WORLD_NAME = "recipient-list-example";

    @Test
    public void testRecipientListRuns()
    {
        World world = World.startWithDefaults( WORLD_NAME );
        world.defaultLogger().log( "RecipientListTest: is started" );
        
        TestUntil untilRegistered = TestUntil.happenings( 5 );
        TestUntil until = TestUntil.happenings( 57 );
        
        OrderProcessor mtnSppliesOrderProcessor = world.actorFor( OrderProcessor.class, MountainSuppliesOrderProcessor.class, until, untilRegistered );
        world.actorFor( QuoteProcessor.class, BudgetHikersPriceQuotes.class, mtnSppliesOrderProcessor );
        world.actorFor( QuoteProcessor.class, HighSierraPriceQuotes.class, mtnSppliesOrderProcessor );
        world.actorFor( QuoteProcessor.class, MountainAscentPriceQuotes.class, mtnSppliesOrderProcessor );
        world.actorFor( QuoteProcessor.class, PinnacleGearPriceQuotes.class, mtnSppliesOrderProcessor );
        world.actorFor( QuoteProcessor.class, RockBottomOuterwearPriceQuotes.class, mtnSppliesOrderProcessor );
        
        untilRegistered.completes();
        world.defaultLogger().log( String.format( "Register completes!!!" ));
        
        mtnSppliesOrderProcessor
            .requestForQuote( 
                new RetailBasket( 
                    "123", 
                    new RetailItem( "1", 29.95 ), 
                    new RetailItem( "2", 99.95 ), 
                    new RetailItem( "3", 14.95 )
                )
            );
        
        mtnSppliesOrderProcessor
            .requestForQuote( 
                new RetailBasket( 
                    "125", 
                    new RetailItem( "4", 39.95 ), 
                    new RetailItem( "5", 199.95 ), 
                    new RetailItem( "6", 149.95 ),
                    new RetailItem( "7", 724.99 )
                )
            );
    
        mtnSppliesOrderProcessor
            .requestForQuote( 
                new RetailBasket( 
                    "129", 
                    new RetailItem( "8", 119.99 ), 
                    new RetailItem( "9", 499.95 ), 
                    new RetailItem( "10", 519.00 ),
                    new RetailItem( "11", 209.50 )
                )
            );
    
        mtnSppliesOrderProcessor
            .requestForQuote( 
                new RetailBasket( 
                    "135", 
                    new RetailItem( "12", .97 ), 
                    new RetailItem( "13", 9.50 ), 
                    new RetailItem( "14", 1.99 )
                )
            );
    
        mtnSppliesOrderProcessor
            .requestForQuote( 
                new RetailBasket( 
                    "140", 
                    new RetailItem( "15", 107.50 ), 
                    new RetailItem( "16", 9.50 ), 
                    new RetailItem( "17", 599.99 ),
                    new RetailItem( "18", 249.95 ),
                    new RetailItem( "19", 789.99 )
                )
            );
    
        until.completes();
        
        world.defaultLogger().log( "RecipientListTest: is completed" );
        world.terminate();
    }

}
