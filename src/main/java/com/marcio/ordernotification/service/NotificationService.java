package com.marcio.ordernotification.service;

import com.marcio.ordernotification.dto.NotificationResponse;
import com.marcio.ordernotification.model.Notification;
import com.marcio.ordernotification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listNotifications(UUID orderId, Pageable pageable) {
        Page<Notification> notifications;

        if (orderId == null) {
            notifications = notificationRepository.findAll(pageable);
        } else {
            notifications = notificationRepository.findByOrderId(orderId, pageable);
        }

        return notifications.map(this::mapToResponse);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrderId(),
                notification.getType(),
                notification.getMessage(),
                notification.getSentAt()
        );
    }
}
