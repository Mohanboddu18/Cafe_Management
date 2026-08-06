package com.cafe.management.controller;

import com.cafe.management.dto.BillingDTOs.*;
import com.cafe.management.dto.OrderDTOs.OrderResponse;
import com.cafe.management.entity.RestaurantTable;
import com.cafe.management.service.BillingInvoiceService;
import com.cafe.management.service.OrderService;
import com.cafe.management.service.TableQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cashier")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
public class CashierController {

    @Autowired
    private TableQrService tableQrService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BillingInvoiceService billingInvoiceService;

    @GetMapping("/tables/active")
    public ResponseEntity<List<RestaurantTable>> getTablesForBilling() {
        return ResponseEntity.ok(tableQrService.getAllTables());
    }

    @PostMapping("/coupon/apply")
    public ResponseEntity<ApplyCouponResponse> applyCoupon(@RequestBody ApplyCouponRequest request) {
        return ResponseEntity.ok(billingInvoiceService.validateAndApplyCoupon(request.getCouponCode(), request.getSubtotal()));
    }

    @PostMapping("/payment/process")
    public ResponseEntity<InvoiceResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(billingInvoiceService.processPayment(request));
    }

    @GetMapping("/invoice/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(billingInvoiceService.getInvoiceByOrderId(orderId));
    }

    @GetMapping(value = "/invoice/order/{orderId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
        byte[] pdfBytes = billingInvoiceService.generateInvoicePdfBytes(orderId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Invoice_Order_" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
