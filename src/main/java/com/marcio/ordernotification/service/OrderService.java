package com.marcio.ordernotification.service;

import com.marcio.ordernotification.dto.CreateOrderRequest;
import com.marcio.ordernotification.dto.OrderItemResponse;
import com.marcio.ordernotification.dto.OrderResponse;
import com.marcio.ordernotification.model.Order;
import com.marcio.ordernotification.model.OrderItem;
import com.marcio.ordernotification.model.OrderStatus;
import com.marcio.ordernotification.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (var itemReq : request.items()) {
            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID());
            item.setProductName(itemReq.productName());
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(itemReq.unitPrice());
            order.addItem(item);

            BigDecimal itemTotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        return mapToResponse(saved);
    }

    private OrderResponse mapToResponse(Order order) {
        var items = order.getItems().stream()
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice()
            ))
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            items
        );
    }
}
