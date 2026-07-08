package com.marcio.ordernotification.controller;

import com.marcio.ordernotification.dto.NotificationResponse;
import com.marcio.ordernotification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for retrieving notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List notifications", description = "Retrieves a paginated list of notifications, optionally filtered by orderId")
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> listNotifications(
            @RequestParam(required = false) UUID orderId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<NotificationResponse> notifications = notificationService.listNotifications(orderId, pageable);
        return ResponseEntity.ok(notifications);
    }
}
