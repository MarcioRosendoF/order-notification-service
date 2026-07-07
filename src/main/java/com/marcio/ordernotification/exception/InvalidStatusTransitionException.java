package com.marcio.ordernotification.exception;

import com.marcio.ordernotification.model.OrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(OrderStatus current, OrderStatus next) {
        super("Cannot transition from " + current + " to " + next);
    }
}
