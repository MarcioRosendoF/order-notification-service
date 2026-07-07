package com.marcio.ordernotification.dto;

import com.marcio.ordernotification.model.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID orderId,
    NotificationType type,
    String message,
    Instant sentAt
) {}
