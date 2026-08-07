package com.cafe.management.controller;

import com.cafe.management.dto.BillingDTOs.*;
import com.cafe.management.dto.OrderDTOs.*;
import com.cafe.management.entity.*;
import com.cafe.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.cafe.management.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private TableQrService tableQrService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BillingInvoiceService billingInvoiceService;

    @GetMapping("/tables")
    public ResponseEntity<List<RestaurantTable>> getAllTables() {
        return ResponseEntity.ok(tableQrService.getAllTables());
    }

    @GetMapping("/table/{tableNumber}")
    public ResponseEntity<RestaurantTable> getTableInfo(@PathVariable Integer tableNumber) {
        return ResponseEntity.ok(tableQrService.getTableByNumber(tableNumber));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItem>> getMenuItems(@RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(menuItemService.searchMenuItems(search));
        } else if (categoryId != null) {
            return ResponseEntity.ok(menuItemService.getMenuItemsByCategory(categoryId));
        }
        return ResponseEntity.ok(menuItemService.getAvailableMenuItems());
    }

    @GetMapping("/cart")
    public ResponseEntity<Cart> getCart(@RequestParam String sessionId, @RequestParam Long tableId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(sessionId, tableId));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<Cart> addToCart(@RequestParam String sessionId,
                                          @RequestParam Long tableId,
                                          @RequestParam Long menuItemId,
                                          @RequestParam(defaultValue = "1") Integer quantity,
                                          @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(cartService.addItemToCart(sessionId, tableId, menuItemId, quantity, notes));
    }

    @PutMapping("/cart/items/{cartItemId}")
    public ResponseEntity<Cart> updateCartItem(@PathVariable Long cartItemId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(cartItemId, quantity));
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<Void> clearCart(@RequestParam String sessionId) {
        cartService.clearCart(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order/place")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable Long orderId) {
        OrderResponse res = null;
        try {
            res = orderService.getOrderById(orderId);
        } catch (Exception e) {
            try {
                res = orderService.getActiveOrderByTable(orderId);
            } catch (Exception ex) {
                List<OrderResponse> all = orderService.getAllOrders();
                if (!all.isEmpty()) {
                    res = all.get(all.size() - 1);
                }
            }
        }
        if (res == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/order/table/{tableId}/active")
    public ResponseEntity<OrderResponse> getActiveOrderByTable(@PathVariable Long tableId) {
        try {
            return ResponseEntity.ok(orderService.getActiveOrderByTable(tableId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/table/{tableId}/occupy")
    public ResponseEntity<RestaurantTable> occupyTable(
            @PathVariable Long tableId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String customerTokenSerial) {
        return ResponseEntity.ok(tableQrService.occupyTableWithToken(tableId, sessionId, customerTokenSerial));
    }

    @PostMapping("/table/switch")
    public ResponseEntity<OrderResponse> switchTable(@RequestBody SwitchTableRequest request) {
        return ResponseEntity.ok(orderService.switchTable(request));
    }

    @GetMapping("/invoice/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceForCustomer(@PathVariable Long orderId) {
        return ResponseEntity.ok(billingInvoiceService.getInvoiceByOrderId(orderId));
    }

    @PostMapping("/invoice/pay")
    public ResponseEntity<InvoiceResponse> customerPayInvoice(@RequestBody CustomerPaymentRequest request) {
        return ResponseEntity.ok(billingInvoiceService.customerPayInvoice(request));
    }

    @PostMapping("/invoice/generate")
    public ResponseEntity<InvoiceResponse> generateBill(@RequestBody GenerateInvoiceRequest request) {
        return ResponseEntity.ok(billingInvoiceService.generateBillForCustomer(request));
    }

    @PostMapping("/table/{tableId}/request-bill")
    public ResponseEntity<Void> requestBill(@PathVariable Long tableId) {
        orderService.requestBillForTable(tableId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/invoice/order/{orderId}/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
        byte[] pdfBytes = billingInvoiceService.generateInvoicePdfBytes(orderId);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Invoice_Order_" + orderId + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
