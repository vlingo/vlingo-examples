package io.vlingo.examples.ecommerce.model;

import io.vlingo.common.Completes;

import java.util.Map;


public interface Order {

    void initOrderForUserProducts(UserId userId, Map<ProductId, Integer> quantityByProduct);

    void paymentComplete(PaymentId paymentId);

    void orderShipped(PaymentId paymentId, int orderStateHash);

    Completes<OrderInfo> query();
}
