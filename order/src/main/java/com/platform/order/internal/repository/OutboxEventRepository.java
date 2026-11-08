package com.platform.order.internal.repository;

import com.platform.common.OrderStatus;
import com.platform.common.OutboxStatus;
import com.platform.order.internal.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent,String> {
    List<OutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
