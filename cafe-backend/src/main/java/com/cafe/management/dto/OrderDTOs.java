package com.cafe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class OrderDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemRequest {
        private Long menuItemId;
        private Integer quantity;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceOrderRequest {
        private String sessionId;
        private Long tableId;
        private String customerName;
        private String customerPhone;
        private String notes;
        private List<CartItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        private String status;
        private Integer estimatedPrepTime;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private Long tableId;
        private Integer tableNumber;
        private String customerName;
        private String sessionId;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal netAmount;
        private String status;
        private String notes;
        private String orderTime;
        private List<OrderItemResponse> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String itemStatus;
        private String notes;
    }
}
