package com.platform.inventory.internal.entity;

import com.platform.common.exceptions.InventoryExceptions.InsufficientInventoryException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @Column(name = "sku",nullable = false, length = 64)
    private String sku;
    @Column(name="available_stock", nullable = false)
    private int availableStock;
    @Column(name="reserved_stock", nullable = false)
    private int reservedStock;
    @Version
    @Column(name = "version",nullable = false)
    private Long version;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Inventory(String sku,int availableStock,int reservedStock){
        this.sku = sku;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
        this.updatedAt = Instant.now();
    }

    public void reserve(int quantity){
        if(quantity < 0) {
            throw new IllegalArgumentException("Quantity should be greater than zero");
        }
        if(quantity > availableStock){
            throw new InsufficientInventoryException("Requested quantity is greater than available stock");
        }
        this.availableStock -= quantity;
        this.reservedStock += quantity;
        this.updatedAt = Instant.now();
    }

    public void release(int quantity){
        this.reservedStock -= quantity;
        this.availableStock += quantity;
        this.updatedAt = Instant.now();
    }

    public void confirm(int quantity){
        this.reservedStock -= quantity;
        this.updatedAt = Instant.now();
    }

}
