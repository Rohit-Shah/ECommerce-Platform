package com.platform.order.internal.web;

import com.platform.common.OrderStatus;
import com.platform.order.internal.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(String orderId, String customerId, OrderStatus status,
                            BigDecimal totalAmount, Instant createdAt, List<OrderItemDTO> items) {

    public record OrderItemDTO(String sku, int quantity, BigDecimal unitPrice,BigDecimal subTotal){}

    public static OrderResponse from(Order order){
        List<OrderItemDTO> itemDTOS = order.getItems().stream()
                .map(item -> new OrderItemDTO(item.getProductSku(),item.getQuantity(),item.getUnitPrice(),item.getSubTotal())).toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemDTOS
                );
    }
}
