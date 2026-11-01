package com.platform.order.internal.web;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(String customerId, List<OrderItemRequest> items) {

    public record OrderItemRequest(String sku,int quantity,BigDecimal unitPrice) {}
}
