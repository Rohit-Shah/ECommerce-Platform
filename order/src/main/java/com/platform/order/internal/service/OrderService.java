package com.platform.order.internal.service;

import com.platform.order.internal.web.CreateOrderRequest;
import com.platform.order.internal.web.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(String orderId);

}
