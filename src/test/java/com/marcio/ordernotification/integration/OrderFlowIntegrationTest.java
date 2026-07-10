package com.marcio.ordernotification.integration;

import com.marcio.ordernotification.dto.CreateOrderItemRequest;
import com.marcio.ordernotification.dto.CreateOrderRequest;
import com.marcio.ordernotification.dto.OrderResponse;
import com.marcio.ordernotification.dto.UpdateOrderStatusRequest;
import com.marcio.ordernotification.model.Notification;
import com.marcio.ordernotification.model.NotificationType;
import com.marcio.ordernotification.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldExecuteFullOrderFlowAndCreateNotification() throws Exception {
        // 1. Create order
        CreateOrderItemRequest item = new CreateOrderItemRequest("PlayStation 5", 1, new BigDecimal("4500.00"));
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

        assertNotNull(orderResponse.id());
        assertEquals(OrderStatus.PENDING, orderResponse.status());

        // 2. Transition status from PENDING to CONFIRMED
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        mockMvc.perform(patch("/orders/" + orderResponse.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 3. Wait for async notification database persistence
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<Notification> notifications = notificationRepository.findAll();
                    assertEquals(1, notifications.size());
                    
                    Notification notification = notifications.get(0);
                    assertEquals(orderResponse.id(), notification.getOrderId());
                    assertEquals(NotificationType.ORDER_CONFIRMED, notification.getType());
                    assertEquals(
                            "Order " + orderResponse.id() + " status changed from PENDING to CONFIRMED",
                            notification.getMessage()
                    );
                });

        // 4. Try invalid transition (CONFIRMED -> PENDING) - Should fail
        UpdateOrderStatusRequest invalidUpdateRequest = new UpdateOrderStatusRequest(OrderStatus.PENDING);

        mockMvc.perform(patch("/orders/" + orderResponse.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
                .andExpect(status().isUnprocessableEntity());
    }
}
