package com.cafe.management.controller;

import com.cafe.management.dto.AnalyticsDTOs.DashboardSummary;
import com.cafe.management.dto.AuthDTOs.MessageResponse;
import com.cafe.management.dto.AuthDTOs.SignupRequest;
import com.cafe.management.entity.*;
import com.cafe.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ReportAnalyticsService reportAnalyticsService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private TableQrService tableQrService;

    @Autowired
    private AuthService authService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private AuditLogService auditLogService;

    // --- DASHBOARD ANALYTICS ---
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        return ResponseEntity.ok(reportAnalyticsService.getDashboardSummary());
    }

    @GetMapping("/reports/sales/excel")
    public ResponseEntity<byte[]> exportSalesReportExcel() {
        byte[] excelBytes = reportAnalyticsService.exportSalesReportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Cafe_Sales_Report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    // --- CATEGORIES CRUD ---
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    // --- MENU ITEMS CRUD ---
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }

    @PostMapping("/menu")
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem item, @RequestParam Long categoryId) {
        return ResponseEntity.ok(menuItemService.createMenuItem(item, categoryId));
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem item, @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, item, categoryId));
    }

    @PatchMapping("/menu/{id}/toggle-availability")
    public ResponseEntity<Void> toggleMenuItemAvailability(@PathVariable Long id) {
        menuItemService.toggleAvailability(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok().build();
    }

    // --- TABLE & QR CRUD ---
    @GetMapping("/tables")
    public ResponseEntity<List<RestaurantTable>> getAllTables() {
        return ResponseEntity.ok(tableQrService.getAllTables());
    }

    @PostMapping("/tables")
    public ResponseEntity<RestaurantTable> createTable(@RequestParam Integer tableNumber, @RequestParam(defaultValue = "4") Integer capacity) {
        return ResponseEntity.ok(tableQrService.createTable(tableNumber, capacity));
    }

    @GetMapping("/tables/{tableId}/qr")
    public ResponseEntity<QrCodeEntity> getTableQr(@PathVariable Long tableId) {
        return ResponseEntity.ok(tableQrService.getQrCodeByTableId(tableId));
    }

    @PutMapping("/tables/{tableId}/status")
    public ResponseEntity<RestaurantTable> updateTableStatus(@PathVariable Long tableId, @RequestParam String status) {
        return ResponseEntity.ok(tableQrService.updateTableStatus(tableId, status));
    }

    // --- EMPLOYEE MANAGEMENT ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<MessageResponse> createEmployee(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    // --- INVENTORY & STOCK ---
    @GetMapping("/inventory")
    public ResponseEntity<List<Inventory>> getInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @PostMapping("/inventory")
    public ResponseEntity<Inventory> createInventoryItem(@RequestBody Inventory item) {
        return ResponseEntity.ok(inventoryService.createInventoryItem(item));
    }

    @PostMapping("/inventory/{id}/stock")
    public ResponseEntity<Inventory> updateStock(@PathVariable Long id,
                                                @RequestParam BigDecimal quantity,
                                                @RequestParam String transactionType,
                                                @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(inventoryService.updateStock(id, quantity, transactionType, notes));
    }

    @GetMapping("/inventory/{id}/history")
    public ResponseEntity<List<StockHistory>> getStockHistory(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getStockHistory(id));
    }

    // --- COUPONS CRUD ---
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.updateCoupon(id, coupon));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }

    // --- AUDIT LOGS ---
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}
