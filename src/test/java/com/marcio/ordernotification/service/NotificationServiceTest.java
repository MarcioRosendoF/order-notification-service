package com.marcio.ordernotification.service;

import com.marcio.ordernotification.dto.NotificationResponse;
import com.marcio.ordernotification.model.Notification;
import com.marcio.ordernotification.model.NotificationType;
import com.marcio.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldListAllNotificationsWhenOrderIdIsNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Notification notification = new Notification(UUID.randomUUID(), UUID.randomUUID(), NotificationType.ORDER_PENDING, "Message", Instant.now());
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        
        when(notificationRepository.findAll(pageable)).thenReturn(notificationPage);

        // Act
        Page<NotificationResponse> result = notificationService.listNotifications(null, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(notification.getId(), result.getContent().get(0).id());
        verify(notificationRepository).findAll(pageable);
    }

    @Test
    void shouldListNotificationsByOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Notification notification = new Notification(UUID.randomUUID(), orderId, NotificationType.ORDER_CONFIRMED, "Message", Instant.now());
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        
        when(notificationRepository.findByOrderId(orderId, pageable)).thenReturn(notificationPage);

        // Act
        Page<NotificationResponse> result = notificationService.listNotifications(orderId, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(notification.getId(), result.getContent().get(0).id());
        verify(notificationRepository).findByOrderId(orderId, pageable);
    }
}
