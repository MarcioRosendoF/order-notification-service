package com.marcio.ordernotification.repository;

import com.marcio.ordernotification.model.Order;
import com.marcio.ordernotification.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}

