package com.cafe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class BillingDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyCouponRequest {
        private String couponCode;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplyCouponResponse {
        private String couponCode;
        private BigDecimal discountAmount;
        private BigDecimal netSubtotal;
        private boolean valid;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessPaymentRequest {
        private Long orderId;
        private String couponCode;
        private BigDecimal discountAmount;
        private BigDecimal gstPercentage = BigDecimal.valueOf(5.0); // 5% GST
        private String paymentMethod; // CASH, CARD, UPI
        private String transactionRef;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceResponse {
        private Long id;
        private String invoiceNumber;
        private Long orderId;
        private String orderNumber;
        private Integer tableNumber;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private String couponCode;
        private BigDecimal gstAmount;
        private BigDecimal totalPayable;
        private String paymentMethod;
        private String paymentStatus;
        private String pdfUrl;
        private String createdAt;
        private List<OrderDTOs.OrderItemResponse> items;
    }
}
