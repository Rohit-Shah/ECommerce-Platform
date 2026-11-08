package com.platform.order.internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.platform.order.internal.web.CreateOrderRequest;
import com.platform.order.internal.web.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request) throws JsonProcessingException;
    OrderResponse getOrder(String orderId);

}
