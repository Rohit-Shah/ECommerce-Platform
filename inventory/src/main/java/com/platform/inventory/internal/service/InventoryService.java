package com.platform.inventory.internal.service;

public interface InventoryService {
    void reserveStock(String sku,int quantity);
}
