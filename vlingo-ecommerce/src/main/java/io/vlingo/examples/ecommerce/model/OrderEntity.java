package io.vlingo.examples.ecommerce.model;

import io.vlingo.common.Completes;
import io.vlingo.lattice.model.sourcing.EventSourced;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class OrderEntity extends EventSourced<OrderEntity.State> implements Order {

    static {
        BiConsumer<OrderEntity, OrderEvents.Created> created = OrderEntity::applyCreated;
        EventSourced.registerConsumer(OrderEntity.class, OrderEvents.Created.class, created);

        BiConsumer<OrderEntity, OrderEvents.PaymentReceived> paymentReceived = OrderEntity::applyPaymentReceived;
        EventSourced.registerConsumer(OrderEntity.class, OrderEvents.PaymentReceived.class, paymentReceived);

        BiConsumer<OrderEntity, OrderEvents.OrderShipped> orderShipped = OrderEntity::applyShipment;
        EventSourced.registerConsumer(OrderEntity.class, OrderEvents.PaymentReceived.class, orderShipped);

    }

    private State state;

    public OrderEntity(
            String orderId,
            UserId userId,
            Map<ProductId, Integer> productQuanityById
    ) {
        apply(OrderEvents.Created.with(orderId, userId, productQuanityById));
    }

    private void applyShipment(OrderEvents.OrderShipped orderShipped) {
        this.state = state.orderStatusUpdate(OrderInfo.OrderStatusEnum.paid);
    }

    private void applyPaymentReceived(OrderEvents.PaymentReceived paymentReceived) {
        this.state = state.orderStatusUpdate(OrderInfo.OrderStatusEnum.paid);
    }

    private void applyCreated(OrderEvents.Created created) {
        state = State.created(created.orderId, created.userId, created.quantityByProductId);
    }

    @Override
    public void paymentComplete(PaymentId paymentId, int orderStateHash) {
        if (state.status != OrderInfo.OrderStatusEnum.notPaid) {
            throw new IllegalStateException("Payment unexpected, already paid for.");
        }
        apply(OrderEvents.PaymentReceived.with(state.orderId, paymentId, OrderInfo.OrderStatusEnum.paid));
    }

    @Override
    public void orderShipped(PaymentId paymentId, int orderStateHash) {
        if (state.status != OrderInfo.OrderStatusEnum.notPaid) {
            throw new IllegalStateException("Payment unexpected, already paid for.");
        }
        apply(OrderEvents.OrderShipped.with(state.orderId, OrderInfo.OrderStatusEnum.paid));
    }

    @Override
    public Completes<OrderInfo> query() {
        return completes().with(state.createOrderInfo());
    }

    @Override
    public OrderInfo doesSyncOperatorWork() {
        return OrderInfo.empty(state.orderId);
    }

    @Override
    protected String streamName() {
        return "orderEvents";
    }

    static class State {
        final String                    orderId;
        final UserId                    userId;
        final Map<ProductId, OrderItem> itemsByProductId;
        final OrderInfo.OrderStatusEnum status;

        private State(
                String orderId,
                UserId userId,
                Map<ProductId, OrderItem> itemsByProductId,
                OrderInfo.OrderStatusEnum status
        ) {
            this.orderId = orderId;
            this.userId = userId;
            this.itemsByProductId = Collections.unmodifiableMap(new HashMap<>(itemsByProductId));
            this.status = status;
        }

        static State created(
                String orderId,
                UserId userId,
                Map<ProductId, Integer> itemsByProductId
        ) {
            Map<ProductId, OrderItem> orderItemsByProductId = itemsByProductId.entrySet().stream()
                    .map((entry) -> new OrderItem(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toMap(OrderItem::getProductId, orderItem -> orderItem));

            return new State(orderId, userId, orderItemsByProductId, OrderInfo.OrderStatusEnum.notPaid);
        }

        State orderStatusUpdate(OrderInfo.OrderStatusEnum newState) {
            return new State(orderId, userId, itemsByProductId, newState);
        }

        OrderInfo createOrderInfo() {
            return new OrderInfo(
                    orderId,
                    itemsByProductId.entrySet().stream()
                            .map(Map.Entry::getValue).collect(Collectors.toList()),
                    status);
        }
    }

}
