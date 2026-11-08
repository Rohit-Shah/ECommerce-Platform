package com.platform.order.internal.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.events.OrderCreatedEvent;
import com.platform.common.exceptions.OrderExceptions.OrderNotFoundException;
import com.platform.order.internal.entity.Order;
import com.platform.order.internal.outbox.OutboxEvent;
import com.platform.order.internal.repository.OrderRepository;
import com.platform.order.internal.repository.OutboxEventRepository;
import com.platform.order.internal.service.OrderService;
import com.platform.order.internal.web.CreateOrderRequest;
import com.platform.order.internal.web.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private static final String ORDER_AGGREGATE_TYPE = "ORDER";
    private final ObjectMapper mapper;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer : {} ",request.customerId());
        Order order = new Order(request.customerId());
        for(var item : request.items()){
            order.addItem(item.sku(),item.quantity(),item.unitPrice());
        }
        Order savedOrder = orderRepository.save(order);
        List<OrderCreatedEvent.OrderItemPayload> orderItems = savedOrder.getItems().stream()
                .map(orderItem -> new OrderCreatedEvent.OrderItemPayload(orderItem.getProductSku(),orderItem.getQuantity()))
                .toList();
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(savedOrder.getId(),savedOrder.getCustomerId(),orderItems);
        //create outbox event
        String payloadJson = "";
        try{
            payloadJson = mapper.writeValueAsString(orderCreatedEvent);
        }catch (JsonProcessingException e){
            log.error("Error while converting payload to string",e);
            throw new IllegalStateException(e);
        }

        OutboxEvent outboxEvent = new OutboxEvent(ORDER_AGGREGATE_TYPE,savedOrder.getId(),orderCreatedEvent.getClass().getSimpleName(),payloadJson);
        outboxEventRepository.save(outboxEvent);
        log.info("Order {} created successfully with total {} ", order,order.getTotalAmount());
        return OrderResponse.from(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId){
        log.debug("Fetching order for order id : {}",orderId);
        return orderRepository.findByIdWithItems(orderId).map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException("Order not found " + orderId));
    }
}
