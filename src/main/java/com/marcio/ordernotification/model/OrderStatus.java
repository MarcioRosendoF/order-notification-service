package com.marcio.ordernotification.model;

import java.util.Collections;
import java.util.Set;

public enum OrderStatus {
    PENDING {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return Set.of(CONFIRMED, CANCELLED);
        }
    },
    CONFIRMED {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return Set.of(CANCELLED);
        }
    },
    CANCELLED {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return Collections.emptySet();
        }
    };

    public abstract Set<OrderStatus> allowedNextStates();

    public boolean canTransitionTo(OrderStatus next) {
        return allowedNextStates().contains(next);
    }
}
