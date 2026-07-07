package com.marcio.ordernotification.messaging.consumer;

import com.marcio.ordernotification.config.RabbitMQConfig;
import com.marcio.ordernotification.messaging.event.OrderStatusChangedEvent;
import com.marcio.ordernotification.model.Notification;
import com.marcio.ordernotification.model.NotificationType;
import com.marcio.ordernotification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STATUS_CHANGED)
    @Transactional
    public void consumeOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Received OrderStatusChangedEvent for order: {}", event.orderId());

        NotificationType type = NotificationType.valueOf("ORDER_" + event.newStatus().name());

        Notification notification = new Notification();
        notification.setOrderId(event.orderId());
        notification.setType(type);
        notification.setMessage(
                String.format("Order %s status changed from %s to %s", 
                        event.orderId(), event.oldStatus(), event.newStatus())
        );

        notificationRepository.save(notification);
        log.info("Notification saved for order: {}", event.orderId());
    }
}
