package com.marcio.ordernotification.controller;

import com.marcio.ordernotification.dto.CreateOrderRequest;
import com.marcio.ordernotification.dto.OrderResponse;
import com.marcio.ordernotification.dto.UpdateOrderStatusRequest;
import com.marcio.ordernotification.model.OrderStatus;
import com.marcio.ordernotification.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public Page<OrderResponse> listOrders(
        @RequestParam(required = false) OrderStatus status,
        Pageable pageable
    ) {
        return orderService.listOrders(status, pageable);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(id, request.status());
    }
}

