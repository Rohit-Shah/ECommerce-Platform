package com.platform.order.internal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @Column(name="id",length = 36)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;
    @Column(name="product_sku",nullable = false, length = 64)
    private String productSku;
    @Column(name = "quantity",nullable = false)
    private int quantity;
    @Column(name = "unit_price",nullable = false,precision = 12,scale = 2)
    private BigDecimal unitPrice;

    public OrderItem(Order order,String productSku,int quantity, BigDecimal unitPrice){
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if(unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        this.id = UUID.randomUUID().toString();
        this.order = order;
        this.quantity = quantity;
        this.productSku = productSku;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubTotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
