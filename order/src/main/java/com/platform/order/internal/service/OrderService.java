package com.platform.order.internal.service;

import com.platform.exceptions.OrderNotFoundException;
import com.platform.order.internal.domain.Order;
import com.platform.order.internal.domain.OrderItem;
import com.platform.order.internal.domain.OrderRepository;
import com.platform.order.internal.web.CreateOrderRequest;
import com.platform.order.internal.web.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request){
        log.info("Creating order for customer : {} ",request.customerId());
        Order order = new Order(request.customerId());
        for(var item : request.items()){
            order.addItem(item.sku(),item.quantity(),item.unitPrice());
        }
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} created successfully with total {} ", order,order.getTotalAmount());
        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId){
        log.debug("Fetching order for order id : {}",orderId);
        return orderRepository.findByIdWithItems(orderId).map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException("Order not found " + orderId));
    }
}
