package com.cafe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReviewDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRatingRequest {
        private Long menuItemId;
        private Integer rating; // 1 to 5 stars
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitReviewRequest {
        private Long orderId;
        private String customerName;
        private List<ItemRatingRequest> ratings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Integer rating;
        private String comment;
        private String customerName;
        private String createdAt;
    }
}
