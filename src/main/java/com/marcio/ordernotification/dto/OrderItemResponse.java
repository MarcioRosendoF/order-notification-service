package com.marcio.ordernotification.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID id,
    String productName,
    Integer quantity,
    BigDecimal unitPrice
) {}
