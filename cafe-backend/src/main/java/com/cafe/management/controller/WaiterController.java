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

    @Autowired
    private com.cafe.management.service.BillingInvoiceService billingInvoiceService;

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
        OrderResponse res = orderService.updateOrderStatus(orderId, "SERVED", null);
        notificationService.markNotificationsForOrderAsRead(orderId, "WAITER");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/orders/confirm-cash/{orderId}")
    public ResponseEntity<com.cafe.management.dto.BillingDTOs.InvoiceResponse> confirmCashPayment(@PathVariable Long orderId) {
        com.cafe.management.dto.BillingDTOs.InvoiceResponse res = billingInvoiceService.confirmCashPayment(orderId);
        notificationService.markNotificationsForOrderAsRead(orderId, "WAITER");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/orders/walk-in")
    public ResponseEntity<OrderResponse> createWalkInOrder(@RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotificationsForRole("WAITER"));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        notificationService.markAllAsReadForRole("WAITER");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications/clear-all")
    public ResponseEntity<Void> clearAllNotifications() {
        notificationService.deleteAllNotificationsForRole("WAITER");
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/orders/invoice/{orderId}/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
        byte[] pdfBytes = billingInvoiceService.generateInvoicePdfBytes(orderId);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Invoice_Order_" + orderId + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
