package com.platform.order.internal.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.common.OrderStatus;
import com.platform.common.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.SQLType;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    @Column(name = "id",nullable = false,length = 36)
    private String id;
    @Column(name = "aggregate_type",nullable = false,length = 64)
    private String aggregateType;
    @Column(name = "aggregate_id",nullable = false,length = 64)
    private String aggregateId;
    @Column(name = "event_type",nullable = false,length = 128)
    private String eventType;
    @Column(name="payload",columnDefinition = "LONGTEXT")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private OutboxStatus status;
    @Column(name = "retry_count",nullable = false)
    private int retryCount = 0;
    @Column(name = "created_at",nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

    public OutboxEvent (String aggregateType, String aggregateId, String eventType, String payload){
        this.id = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.retryCount = 0;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markAsPublished(){
        this.status = OutboxStatus.PUBLISHED;
        this.updatedAt = Instant.now();
    }

    public void markAsFailed(){
        this.retryCount++;
        this.status = this.retryCount >= 3 ? OutboxStatus.FAILED : OutboxStatus.PENDING;
        this.updatedAt = Instant.now();
    }
}
