package com.marcio.ordernotification.messaging.producer;

import com.marcio.ordernotification.config.RabbitMQConfig;
import com.marcio.ordernotification.messaging.event.OrderStatusChangedEvent;
import com.marcio.ordernotification.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderEventPublisher orderEventPublisher;

    @Test
    void shouldPublishOrderStatusChangedEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                orderId,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                Instant.now()
        );

        // Act
        orderEventPublisher.publishStatusChangedEvent(event);

        // Assert
        ArgumentCaptor<OrderStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_ORDERS),
                eq(RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED),
                eventCaptor.capture()
        );

        OrderStatusChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(orderId, capturedEvent.orderId());
        assertEquals(OrderStatus.PENDING, capturedEvent.oldStatus());
        assertEquals(OrderStatus.CONFIRMED, capturedEvent.newStatus());
    }
}
