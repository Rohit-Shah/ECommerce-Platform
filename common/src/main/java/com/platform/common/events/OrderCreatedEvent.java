package com.platform.common.events;

import com.platform.common.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(String orderId, String customerId,
                                String eventId, Instant occurredOn,
                                List<OrderItemPayload> itemPayloads) implements DomainEvent {


    public OrderCreatedEvent(String orderId,String customerId, List<OrderItemPayload> items){
        this(orderId,customerId,UUID.randomUUID().toString(),Instant.now(),List.copyOf(items));
    }


    @Override
    public String aggregateType() {
        return "ORDER";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }

    public record OrderItemPayload(String sku, int quantity){}
}
