package com.platform.inventory.internal.listener;

import com.platform.common.events.OrderCreatedEvent;
import com.platform.common.exceptions.InventoryExceptions.InsufficientInventoryException;
import com.platform.inventory.internal.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryOrderEventListener {

    private final InventoryService inventoryService;

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event){
        log.info("Received Order Created Event for order id : {}",event.orderId());
        List<OrderCreatedEvent.OrderItemPayload> reservedItems = new ArrayList<>();
        try{
            for(var item : event.itemPayloads()){
                inventoryService.reserveStock(item.sku(),item.quantity());
                reservedItems.add(item);
            }
            log.info("Successfully reserved all items for orderId: {}", event.orderId());
        }catch (InsufficientInventoryException e){
            log.error("Unable to reserve stock for order id : {}. Rolling back previous reserved items",event.orderId());
            rollbackReservedItems(reservedItems);
        }catch (Exception e){
            log.error("Unable to reserve stock for order id : {}. Rolling back previous reserved items",event.orderId());
            rollbackReservedItems(reservedItems);
        }
    }

    private void rollbackReservedItems(List<OrderCreatedEvent.OrderItemPayload> itemsToRelease){
        for(var item : itemsToRelease){
            try{
                inventoryService.releaseStock(item.sku(),item.quantity());
                log.info("Compensated/Released SKU: {} quantity: {}", item.sku(), item.quantity());
            }catch (Exception e){
                log.error("CRITICAL: Failed to release SKU: {} during compensation!", item.sku(), e);
            }

        }
    }

}
