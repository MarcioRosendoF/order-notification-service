package com.marcio.ordernotification.service;

import com.marcio.ordernotification.dto.CreateOrderItemRequest;
import com.marcio.ordernotification.dto.CreateOrderRequest;
import com.marcio.ordernotification.dto.OrderResponse;
import com.marcio.ordernotification.model.Order;
import com.marcio.ordernotification.model.OrderStatus;
import com.marcio.ordernotification.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndCalculateTotalCorrectly() {
        UUID customerId = UUID.randomUUID();
        CreateOrderItemRequest item1 = new CreateOrderItemRequest("Product A", 2, new BigDecimal("10.50"));
        CreateOrderItemRequest item2 = new CreateOrderItemRequest("Product B", 3, new BigDecimal("20.00"));
        CreateOrderRequest request = new CreateOrderRequest(customerId, List.of(item1, item2));

        when(orderRepository.save(any(Order.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(customerId, response.customerId());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("81.00"), response.totalAmount());
        assertEquals(2, response.items().size());
    }
}
