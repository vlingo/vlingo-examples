// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.reactive.messaging.patterns.messagebus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.reactive.messaging.patterns.messagebus.TradingBusCommands.RegisterCommandHandler;
import io.vlingo.reactive.messaging.patterns.messagebus.TradingBusCommands.RegisterNotificationInterest;
import io.vlingo.reactive.messaging.patterns.messagebus.TradingBusCommands.TradingCommand;
import io.vlingo.reactive.messaging.patterns.messagebus.TradingBusCommands.TradingNotification;

/**
 * TradingBus is the nexus between the {@link StockTrader}, {@link PortfolioManager}, and 
 * {@link MarketAnalysisTools} systems providing an ability to register interest in specific types
 * of messages and then delivering these messages asynchronously to each registrant.
 */
public class TradingBus 
extends Actor
implements TradingBusProcessor
{
    public final TestUntil until;
    private final Map<String, Vector<CommandHandler>> commandHandlers = new HashMap<>();
    private final Map<String, Vector<NotificationInterest>> notificationInterests = new HashMap<>();
    
    public TradingBus( TestUntil until )
    {
        this.until = until;
    }
    
    // ACTTION
    /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingBusProcessor#trade(io.vlingo.reactive.messaging.patterns.messagebus.TradingCommand) */
    @Override
    public void trade( TradingCommand command )
    {
        dispatchCommand( command );
    }

    /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingBusProcessor#notify(io.vlingo.reactive.messaging.patterns.messagebus.TradingNotification) */
    @Override
    public void notify( TradingNotification notification )
    {
        dispatchNotification( notification );
    }

    protected void dispatchCommand( TradingCommand command )
    {
        Vector<CommandHandler> commandHandlerVector = commandHandlers.get( command.commandId );
        for ( CommandHandler handler : commandHandlerVector )
        {
            unwrapCommandAndForward( command, handler );
            
            until.happened();
        }
    }

    protected void unwrapCommandAndForward( TradingCommand command, CommandHandler handler )
    {
        if ( command.commandId.equals( TradingProcessor.EXECUTE_BUY_ORDER ))
        {
            handler.executeBuyOrder( command.portfolioId, command.symbol, command.quantity, command.price );
        }
        else if ( command.commandId.equals( TradingProcessor.EXECUTE_SELL_ORDER ))
        {
            handler.executeSellOrder( command.portfolioId, command.symbol, command.quantity, command.price );
        }
        else
        {
            logger().log( String.format( "Unexpected command id '%d'", command.commandId ));
        }
    }

    protected void dispatchNotification( TradingNotification notification )
    {
        Vector<NotificationInterest> notificationInterestsVector = notificationInterests.get( notification.notificationId );
        for ( NotificationInterest notifier : notificationInterestsVector )
        {
            unwrapNotificationAndForward( notification, notifier );
            
            until.happened();
        }
    }

    protected void unwrapNotificationAndForward( TradingNotification notification, NotificationInterest notifier )
    {
        if ( notification.notificationId.equals( TradingProcessor.BUY_ORDER_EXECUTED ))
        {
            notifier.buyOrderExecuted( notification.portfolioId, notification.symbol, notification.quantity, notification.price );
        }
        else if ( notification.notificationId.equals( TradingProcessor.SELL_ORDER_EXECUTED ))
        {
            notifier.sellOrderExecuted( notification.portfolioId, notification.symbol, notification.quantity, notification.price );
        }
        else
        {
            logger().log( String.format( "Unexpected notification id '%s'", notification.notificationId ));
        }
    }

    // REGISTERING
    /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingBusProcessor#registerCommandHandler(java.lang.String, java.lang.String, io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor) */
    @Override
    public void registerHandler( RegisterCommandHandler register )
    {
        
        logger().log( String.format( "%s::registerCommandHandler( %s, %s, %s )", getClass().getSimpleName(), register.applicationId, register.commandId, register.handler.getClass().getSimpleName() ));
        CommandHandler commandHandler = new CommandHandler( register.commandId, register.applicationId, register.handler );
        Vector<CommandHandler> commandHandlerVector = commandHandlers.get( register.commandId );
        if ( Objects.isNull( commandHandlerVector ))
        {
            commandHandlerVector = new Vector<>();
            commandHandlers.put( register.commandId, commandHandlerVector );
        }
        
        commandHandlerVector.add( commandHandler );
        
        until.happened();
    }

    /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingBusProcessor#registerNotificationInterest(java.lang.String, java.lang.String, io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor) */
    @Override
    public void registerInterest( RegisterNotificationInterest register )
    {
        
        logger().log( String.format( "%s::registerNotificationInterest( %s, %s, %s )", getClass().getSimpleName(), register.applicationId, register.notificationId, register.getClass().getSimpleName() ));
        NotificationInterest notificationInterest = new NotificationInterest( register.notificationId, register.applicationId, register.interested );
        Vector<NotificationInterest> notificationInterestVector = notificationInterests.get( register.notificationId );
        if ( Objects.isNull( notificationInterestVector ))
        {
            notificationInterestVector = new Vector<>();
            notificationInterests.put( register.notificationId, notificationInterestVector );
        }
        
        notificationInterestVector.add( notificationInterest );
        
        until.happened();
    }

    public static final class CommandHandler
    implements TradingProcessor
    {
        public final String commandId;
        public final String applicationId;
        public final TradingProcessor tradingProcessor;
        
        public CommandHandler( final String commandId, final String applicationId, final TradingProcessor tradingProcessor )
        {
            this.commandId = commandId;
            this.applicationId = applicationId;
            this.tradingProcessor = tradingProcessor;
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#executeBuyOrder(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void executeBuyOrder( String portfolioId, String symbol, Integer quantity, Double price )
        {
            if ( isHandlingExecuteBuyOrder() )
                tradingProcessor.executeBuyOrder( portfolioId, symbol, quantity, price );
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#executeSellOrder(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void executeSellOrder( String portfolioId, String symbol, Integer quantity, Double price )
        {
            if ( isHandlingExecuteSellOrder() )
                tradingProcessor.executeSellOrder( portfolioId, symbol, quantity, price );
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#buyOrderExecuted(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void buyOrderExecuted( String portfolioId, String symbol, Integer quantity, Double price )
        {
            // unimplemented
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#sellOrderExecuted(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void sellOrderExecuted( String portfolioId, String symbol, Integer quantity, Double price )
        {
            // unimplemented
        }

        // TESTING
        public boolean isHandlingExecuteBuyOrder()
        {
            return TradingProcessor.EXECUTE_BUY_ORDER.equals( commandId );
        }

        public boolean isHandlingExecuteSellOrder()
        {
            return TradingProcessor.EXECUTE_SELL_ORDER.equals( commandId );
        }

    }
    
    public static final class NotificationInterest
    implements TradingProcessor
    {
        public final String notificationId;
        public final String applicationId;
        public final TradingProcessor tradingProcessor;
        
        public NotificationInterest( final String notificationId, final String applicationId, final TradingProcessor tradingProcessor )
        {
            this.notificationId = notificationId;
            this.applicationId = applicationId;
            this.tradingProcessor = tradingProcessor;
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#executeBuyOrder(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void executeBuyOrder( String portfolioId, String symbol, Integer quantity, Double price )
        {
            // unimplemented
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#executeSellOrder(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void executeSellOrder( String portfolioId, String symbol, Integer quantity, Double price )
        {
            // unimplemented
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#buyOrderExecuted(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void buyOrderExecuted( String portfolioId, String symbol, Integer quantity, Double price )
        {
            if ( isInterestedInBuyOrderExecuted() )
                tradingProcessor.buyOrderExecuted( portfolioId, symbol, quantity, price );
        }

        /* @see io.vlingo.reactive.messaging.patterns.messagebus.TradingProcessor#sellOrderExecuted(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Double) */
        @Override
        public void sellOrderExecuted( String portfolioId, String symbol, Integer quantity, Double price )
        {
            if ( isInterestedInSellOrderExecuted() )
                tradingProcessor.sellOrderExecuted( portfolioId, symbol, quantity, price );
        }

        // TESTING
        public boolean isInterestedInBuyOrderExecuted()
        {
            return TradingProcessor.BUY_ORDER_EXECUTED.equals( notificationId );
        }

        public boolean isInterestedInSellOrderExecuted()
        {
            return TradingProcessor.SELL_ORDER_EXECUTED.equals( notificationId );
        }
        
    }
}
