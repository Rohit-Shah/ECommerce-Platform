package com.platform.order.internal.entity;

import com.platform.common.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @Column(name = "id",length = 36)
    private String id;
    @Column(name = "customer_id",nullable = false,length = 36)
    private String customerId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 32)
    private OrderStatus status;
    @Column(name = "total_amount",nullable = false,precision = 12,scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "created_at",nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order(String customerId){
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addItem(String sku,int quantity,BigDecimal unitPrice){
        if(this.status != OrderStatus.PENDING){
            throw new IllegalStateException("Cannot add items to an order that is already " + this.status);
        }
        OrderItem item = new OrderItem(this,sku,quantity,unitPrice);
        this.items.add(item);
        recalculateTotal();
    }

    public void recalculateTotal(){
        this.totalAmount = this.items.stream().map(orderItem -> orderItem.getSubTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.updatedAt = Instant.now();
    }

    public void markAsStockConfirmed(){
        validateTransition(OrderStatus.STOCK_CONFIRMED);
        this.status = OrderStatus.STOCK_CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void markAsStockFailed(){
        validateTransition(OrderStatus.STOCK_FAILED);
        this.status = OrderStatus.STOCK_FAILED;
        this.updatedAt = Instant.now();
    }

    public void validateTransition(OrderStatus status){
        if(this.status == OrderStatus.CANCELLED || this.status == OrderStatus.COMPLETED){
            throw new IllegalStateException("Order is in terminal state : " + this.status);
        }
    }

    public List<OrderItem> getItems(){
        return Collections.unmodifiableList(items);
    }
}
