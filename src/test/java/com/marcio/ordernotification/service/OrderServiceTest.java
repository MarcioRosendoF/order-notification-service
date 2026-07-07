package com.marcio.ordernotification.service;

import com.marcio.ordernotification.dto.CreateOrderItemRequest;
import com.marcio.ordernotification.dto.CreateOrderRequest;
import com.marcio.ordernotification.dto.OrderResponse;
import com.marcio.ordernotification.exception.InvalidStatusTransitionException;
import com.marcio.ordernotification.exception.OrderNotFoundException;
import com.marcio.ordernotification.messaging.event.OrderStatusChangedEvent;
import com.marcio.ordernotification.messaging.producer.OrderEventPublisher;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

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

    @Test
    void shouldGetOrderByIdSuccessfully() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.id());
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void shouldTransitionStatusFromPendingToConfirmed() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        OrderResponse response = orderService.updateStatus(orderId, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, response.status());
        verify(orderEventPublisher).publishStatusChangedEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void shouldTransitionStatusFromPendingToCancelled() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        OrderResponse response = orderService.updateStatus(orderId, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(orderEventPublisher).publishStatusChangedEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void shouldTransitionStatusFromConfirmedToCancelled() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        OrderResponse response = orderService.updateStatus(orderId, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(orderEventPublisher).publishStatusChangedEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenTransitioningFromConfirmedToPending() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.updateStatus(orderId, OrderStatus.PENDING));
    }

    @Test
    void shouldThrowExceptionWhenTransitioningFromCancelledToConfirmed() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.updateStatus(orderId, OrderStatus.CONFIRMED));
    }

    @Test
    void shouldThrowExceptionWhenTransitioningFromCancelledToPending() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.updateStatus(orderId, OrderStatus.PENDING));
    }
}

