package com.marcio.ordernotification.messaging.producer;

import com.marcio.ordernotification.config.RabbitMQConfig;
import com.marcio.ordernotification.messaging.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Publishing OrderStatusChangedEvent for order: {}", event.orderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ORDERS,
                RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED,
                event
        );
    }
}
