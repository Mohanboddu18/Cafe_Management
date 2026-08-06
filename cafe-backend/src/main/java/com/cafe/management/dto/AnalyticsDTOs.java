package com.cafe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardSummary {
        private BigDecimal todaysSales;
        private BigDecimal weeklySales;
        private BigDecimal monthlySales;
        private Long activeOrdersCount;
        private Long totalTablesCount;
        private Long occupiedTablesCount;
        private List<PopularItem> popularItems;
        private Map<String, BigDecimal> dailyRevenueChart;
        private List<RecentOrderSummary> recentOrders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PopularItem {
        private String itemName;
        private Long quantitySold;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentOrderSummary {
        private String orderNumber;
        private Integer tableNumber;
        private BigDecimal amount;
        private String status;
        private String time;
    }
}
