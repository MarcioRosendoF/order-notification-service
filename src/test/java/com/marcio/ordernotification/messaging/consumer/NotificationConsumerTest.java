package com.marcio.ordernotification.messaging.consumer;

import com.marcio.ordernotification.messaging.event.OrderStatusChangedEvent;
import com.marcio.ordernotification.model.Notification;
import com.marcio.ordernotification.model.NotificationType;
import com.marcio.ordernotification.model.OrderStatus;
import com.marcio.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void shouldConsumeEventAndSaveNotification() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                orderId,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                Instant.now()
        );

        // Act
        notificationConsumer.consumeOrderStatusChanged(event);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertNotNull(savedNotification);
        assertEquals(orderId, savedNotification.getOrderId());
        assertEquals(NotificationType.ORDER_CONFIRMED, savedNotification.getType());
        assertEquals(
                "Order " + orderId + " status changed from PENDING to CONFIRMED",
                savedNotification.getMessage()
        );
    }
}
