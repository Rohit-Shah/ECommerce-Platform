package com.platform.order.internal.repository;

import com.platform.order.internal.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,String> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") String id);
}
