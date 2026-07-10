package com.marcio.ordernotification.integration;

import com.marcio.ordernotification.config.RabbitMQConfig;
import com.marcio.ordernotification.dto.CreateOrderItemRequest;
import com.marcio.ordernotification.dto.CreateOrderRequest;
import com.marcio.ordernotification.dto.OrderResponse;
import com.marcio.ordernotification.dto.UpdateOrderStatusRequest;
import com.marcio.ordernotification.messaging.event.OrderStatusChangedEvent;
import com.marcio.ordernotification.model.OrderStatus;
import com.marcio.ordernotification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeadLetterQueueIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private NotificationRepository mockNotificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldRouteMessageToDLQWhenConsumerFails() throws Exception {
        // Arrange: Make the repository fail when trying to save a notification
        doThrow(new RuntimeException("Database failure simulated for testing DLQ"))
                .when(mockNotificationRepository).save(any());

        // 1. Create order
        CreateOrderItemRequest item = new CreateOrderItemRequest("Xbox Series X", 1, new BigDecimal("4000.00"));
        CreateOrderRequest createRequest = new CreateOrderRequest(UUID.randomUUID(), List.of(item));

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderResponse orderResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                OrderResponse.class
        );

        // 2. Transition status (will trigger RabbitMQ message publish and consumer exception)
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        mockMvc.perform(patch("/orders/" + orderResponse.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 3. Verify message was routed to DLQ because of consumer exception
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    OrderStatusChangedEvent dlqEvent = (OrderStatusChangedEvent) rabbitTemplate
                            .receiveAndConvert(RabbitMQConfig.QUEUE_STATUS_CHANGED_DLQ);
                    
                    assertNotNull(dlqEvent);
                    assertEquals(orderResponse.id(), dlqEvent.orderId());
                    assertEquals(OrderStatus.PENDING, dlqEvent.oldStatus());
                    assertEquals(OrderStatus.CONFIRMED, dlqEvent.newStatus());
                });
    }
}
