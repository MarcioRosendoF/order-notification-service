package com.marcio.ordernotification.messaging.event;

import com.marcio.ordernotification.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant occurredAt
) {
}
