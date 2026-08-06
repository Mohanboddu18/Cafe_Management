package com.cafe.management.service;

import com.cafe.management.dto.AnalyticsDTOs.*;
import com.cafe.management.dto.OrderDTOs;
import com.cafe.management.entity.Order;
import com.cafe.management.repository.OrderItemRepository;
import com.cafe.management.repository.OrderRepository;
import com.cafe.management.repository.RestaurantTableRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private OrderService orderService;

    public DashboardSummary getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        BigDecimal todaysSales = Optional.ofNullable(orderRepository.calculateSalesBetweenDates(startOfDay, endOfDay)).orElse(BigDecimal.ZERO);
        BigDecimal weeklySales = Optional.ofNullable(orderRepository.calculateSalesBetweenDates(startOfWeek, endOfDay)).orElse(BigDecimal.ZERO);
        BigDecimal monthlySales = Optional.ofNullable(orderRepository.calculateSalesBetweenDates(startOfMonth, endOfDay)).orElse(BigDecimal.ZERO);

        Long activeOrders = (long) orderRepository.findByStatusIn(List.of("NEW", "ACCEPTED", "PREPARING", "READY", "SERVED")).size();
        Long totalTables = tableRepository.count();
        Long occupiedTables = (long) tableRepository.findByStatus("OCCUPIED").size();

        // Popular Items
        List<PopularItem> popularItems = new ArrayList<>();
        List<Object[]> popularData = orderItemRepository.findPopularMenuItems();
        for (int i = 0; i < Math.min(5, popularData.size()); i++) {
            Object[] row = popularData.get(i);
            popularItems.add(PopularItem.builder()
                    .itemName((String) row[0])
                    .quantitySold(((Number) row[1]).longValue())
                    .build());
        }

        // Daily Revenue Chart (Last 7 Days)
        Map<String, BigDecimal> chartData = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            BigDecimal sales = Optional.ofNullable(orderRepository.calculateSalesBetweenDates(dayStart, dayEnd)).orElse(BigDecimal.ZERO);
            chartData.put(date.format(DateTimeFormatter.ofPattern("MMM dd")), sales);
        }

        // Recent Orders
        List<Order> recentList = orderRepository.findAll();
        recentList.sort((a, b) -> b.getOrderTime().compareTo(a.getOrderTime()));
        List<RecentOrderSummary> recentSummaries = recentList.stream().limit(6).map(o ->
                RecentOrderSummary.builder()
                        .orderNumber(o.getOrderNumber())
                        .tableNumber(o.getTable().getTableNumber())
                        .amount(o.getNetAmount())
                        .status(o.getStatus())
                        .time(o.getOrderTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                        .build()
        ).collect(Collectors.toList());

        return DashboardSummary.builder()
                .todaysSales(todaysSales)
                .weeklySales(weeklySales)
                .monthlySales(monthlySales)
                .activeOrdersCount(activeOrders)
                .totalTablesCount(totalTables)
                .occupiedTablesCount(occupiedTables)
                .popularItems(popularItems)
                .dailyRevenueChart(chartData)
                .recentOrders(recentSummaries)
                .build();
    }

    public byte[] exportSalesReportExcel() {
        List<Order> orders = orderRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sales Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Order Number", "Date & Time", "Table No", "Customer", "Subtotal ($)", "Discount ($)", "Tax ($)", "Net Amount ($)", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Order o : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(o.getOrderNumber());
                row.createCell(1).setCellValue(o.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                row.createCell(2).setCellValue(o.getTable().getTableNumber());
                row.createCell(3).setCellValue(o.getCustomer() != null ? o.getCustomer().getName() : "Guest");
                row.createCell(4).setCellValue(o.getTotalAmount().doubleValue());
                row.createCell(5).setCellValue(o.getDiscountAmount() != null ? o.getDiscountAmount().doubleValue() : 0.0);
                row.createCell(6).setCellValue(o.getTaxAmount() != null ? o.getTaxAmount().doubleValue() : 0.0);
                row.createCell(7).setCellValue(o.getNetAmount().doubleValue());
                row.createCell(8).setCellValue(o.getStatus());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error exporting Excel sales report", e);
        }
    }
}
