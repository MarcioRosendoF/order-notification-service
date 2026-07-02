package com.marcio.ordernotification.repository;

import com.marcio.ordernotification.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
