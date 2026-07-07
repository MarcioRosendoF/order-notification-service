package com.marcio.ordernotification.dto;

import com.marcio.ordernotification.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull OrderStatus status
) {}
