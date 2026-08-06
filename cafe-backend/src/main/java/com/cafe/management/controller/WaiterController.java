package com.cafe.management.controller;

import com.cafe.management.dto.OrderDTOs.*;
import com.cafe.management.entity.Notification;
import com.cafe.management.entity.RestaurantTable;
import com.cafe.management.service.NotificationService;
import com.cafe.management.service.OrderService;
import com.cafe.management.service.TableQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waiter")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
public class WaiterController {

    @Autowired
    private TableQrService tableQrService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/tables")
    public ResponseEntity<List<RestaurantTable>> getAllTables() {
        return ResponseEntity.ok(tableQrService.getAllTables());
    }

    @PutMapping("/tables/{tableId}/status")
    public ResponseEntity<RestaurantTable> updateTableStatus(@PathVariable Long tableId, @RequestParam String status) {
        return ResponseEntity.ok(tableQrService.updateTableStatus(tableId, status));
    }

    @GetMapping("/orders/ready")
    public ResponseEntity<List<OrderResponse>> getReadyOrders() {
        return ResponseEntity.ok(orderService.getOrdersByStatus(List.of("READY")));
    }

    @PutMapping("/orders/{orderId}/mark-served")
    public ResponseEntity<OrderResponse> markServed(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, "SERVED", null));
    }

    @PostMapping("/orders/walk-in")
    public ResponseEntity<OrderResponse> createWalkInOrder(@RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotificationsForRole("WAITER"));
    }
}
