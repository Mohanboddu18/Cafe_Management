package com.cafe.management.controller;

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
        return ResponseEntity.ok(orderService.getOrderById(orderId));
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
    public ResponseEntity<RestaurantTable> occupyTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(tableQrService.updateTableStatus(tableId, "OCCUPIED"));
    }

    @PostMapping("/table/{tableId}/request-bill")
    public ResponseEntity<Void> requestBill(@PathVariable Long tableId) {
        orderService.requestBillForTable(tableId);
        return ResponseEntity.ok().build();
    }
}
