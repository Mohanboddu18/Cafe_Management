package com.cafe.management.controller;

import com.cafe.management.dto.OrderDTOs.*;
import com.cafe.management.entity.Notification;
import com.cafe.management.service.NotificationService;
import com.cafe.management.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
public class KitchenController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/orders/live")
    public ResponseEntity<List<OrderResponse>> getLiveOrders() {
        return ResponseEntity.ok(orderService.getOrdersByStatus(List.of("NEW", "ACCEPTED", "PREPARING", "READY")));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId,
                                                            @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus(), request.getEstimatedPrepTime()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotificationsForRole("KITCHEN"));
    }
}
