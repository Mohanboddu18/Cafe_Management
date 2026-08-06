package com.cafe.management.service;

import com.cafe.management.dto.OrderDTOs.*;
import com.cafe.management.entity.*;
import com.cafe.management.exception.BadRequestException;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private KitchenOrderRepository kitchenOrderRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CartService cartService;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        Customer customer = Customer.builder()
                .sessionId(request.getSessionId())
                .name(request.getCustomerName() != null ? request.getCustomerName() : "Table " + table.getTableNumber() + " Customer")
                .phone(request.getCustomerPhone())
                .customerTokenSerial(request.getCustomerTokenSerial())
                .table(table)
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        String orderNumber = "ORD-" + System.currentTimeMillis() % 1000000;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .table(table)
                .customer(savedCustomer)
                .status("NEW")
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemRequest ci : request.getItems()) {
            MenuItem item = menuItemRepository.findById(ci.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + ci.getMenuItemId()));

            BigDecimal totalItemPrice = item.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(totalItemPrice);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(item)
                    .quantity(ci.getQuantity())
                    .unitPrice(item.getPrice())
                    .totalPrice(totalItemPrice)
                    .itemStatus("PENDING")
                    .notes(ci.getNotes())
                    .build();

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(subtotal);
        order.setNetAmount(subtotal);

        Order savedOrder = orderRepository.save(order);

        // Create Kitchen Order Entry
        KitchenOrder kitchenOrder = KitchenOrder.builder()
                .order(savedOrder)
                .kitchenStatus("QUEUED")
                .estimatedPrepTime(15)
                .build();
        kitchenOrderRepository.save(kitchenOrder);

        // Update Table Status to OCCUPIED
        table.setStatus("OCCUPIED");
        tableRepository.save(table);

        // Clear Customer Cart
        cartService.clearCart(request.getSessionId());

        // Notify Kitchen Staff via WebSocket
        notificationService.sendNotification(
                "KITCHEN",
                "New Order Received!",
                "Order #" + savedOrder.getOrderNumber() + " placed for Table " + table.getTableNumber(),
                savedOrder.getId(),
                table.getId()
        );

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    public OrderResponse getActiveOrderByTable(Long tableId) {
        List<Order> orders = orderRepository.findActiveOrdersByTableId(tableId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No active order for table " + tableId);
        }
        return mapToOrderResponse(orders.get(0));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(List<String> statuses) {
        return orderRepository.findByStatusIn(statuses).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status, Integer prepTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String newStatus = status.toUpperCase();
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        // Update Kitchen Status if present
        kitchenOrderRepository.findByOrderId(orderId).ifPresent(ko -> {
            if ("PREPARING".equals(newStatus)) {
                ko.setKitchenStatus("PREPARING");
                if (prepTime != null) ko.setEstimatedPrepTime(prepTime);
                ko.setStartedAt(LocalDateTime.now());
            } else if ("READY".equals(newStatus)) {
                ko.setKitchenStatus("READY");
                ko.setCompletedAt(LocalDateTime.now());
            } else if ("COMPLETED".equals(newStatus)) {
                ko.setKitchenStatus("COMPLETED");
            }
            kitchenOrderRepository.save(ko);
        });

        // Trigger Live WebSocket Notifications
        if ("READY".equals(newStatus)) {
            notificationService.sendNotification(
                    "WAITER",
                    "Order Ready for Delivery!",
                    "Order #" + updated.getOrderNumber() + " for Table " + updated.getTable().getTableNumber() + " is READY to serve.",
                    updated.getId(),
                    updated.getTable().getId()
            );
        } else if ("SERVED".equals(newStatus)) {
            notificationService.sendNotification(
                    "CASHIER",
                    "Table Food Served",
                    "Table " + updated.getTable().getTableNumber() + " food has been served.",
                    updated.getId(),
                    updated.getTable().getId()
            );
        }

        return mapToOrderResponse(updated);
    }

    @Transactional
    public void requestBillForTable(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        table.setStatus("BILL_REQUESTED");
        tableRepository.save(table);

        notificationService.sendNotification(
                "CASHIER",
                "Bill Requested!",
                "Table " + table.getTableNumber() + " requested the final bill.",
                null,
                table.getId()
        );
        notificationService.sendNotification(
                "WAITER",
                "Bill Requested!",
                "Table " + table.getTableNumber() + " requested the final bill.",
                null,
                table.getId()
        );
    }

    @Transactional
    public OrderResponse switchTable(SwitchTableRequest request) {
        RestaurantTable fromTable = tableRepository.findById(request.getFromTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin table not found"));
        RestaurantTable toTable = tableRepository.findById(request.getToTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination table not found"));

        if ("OCCUPIED".equals(toTable.getStatus()) || "BILL_REQUESTED".equals(toTable.getStatus())) {
            throw new BadRequestException("Destination Table #" + toTable.getTableNumber() + " is currently occupied!");
        }

        // 1. Mark Destination Table as OCCUPIED and link Token & Session
        toTable.setStatus("OCCUPIED");
        toTable.setCurrentSessionId(request.getSessionId());
        toTable.setCurrentTokenSerial(request.getCustomerTokenSerial());
        tableRepository.save(toTable);

        // 2. Remove Token Link from Origin Table and reset to AVAILABLE
        fromTable.setStatus("AVAILABLE");
        fromTable.setCurrentSessionId(null);
        fromTable.setCurrentTokenSerial(null);
        tableRepository.save(fromTable);

        // 3. Transfer Cart Table
        cartService.transferCartTable(request.getSessionId(), request.getFromTableId(), toTable.getId());

        // 4. Update Active Orders to New Table
        List<Order> activeOrders = orderRepository.findActiveOrdersByTableId(request.getFromTableId());
        Order mainOrder = null;
        for (Order order : activeOrders) {
            order.setTable(toTable);
            if (order.getCustomer() != null) {
                if (request.getCustomerTokenSerial() != null) {
                    order.getCustomer().setCustomerTokenSerial(request.getCustomerTokenSerial());
                }
                order.getCustomer().setTable(toTable);
                customerRepository.save(order.getCustomer());
            }
            String noteMsg = "[TABLE SWITCHED]: Moved from Table #" + fromTable.getTableNumber() +
                    " ➡️ Table #" + toTable.getTableNumber() + " (Token: " + request.getCustomerTokenSerial() + ")";
            order.setNotes(order.getNotes() != null ? order.getNotes() + " | " + noteMsg : noteMsg);
            orderRepository.save(order);
            mainOrder = order;
        }

        // 5. Send Live Notification to Waiter & Kitchen
        String alertMsg = "Customer Token [" + (request.getCustomerTokenSerial() != null ? request.getCustomerTokenSerial() : "GUEST") +
                "] SWITCHED from Table #" + fromTable.getTableNumber() + " ➡️ Table #" + toTable.getTableNumber() + "! Serve food to Table #" + toTable.getTableNumber() + ".";

        notificationService.sendNotification(
                "WAITER",
                "🔄 Table Switch Alert!",
                alertMsg,
                mainOrder != null ? mainOrder.getId() : null,
                toTable.getId()
        );
        notificationService.sendNotification(
                "KITCHEN",
                "🔄 Table Switch Alert!",
                alertMsg,
                mainOrder != null ? mainOrder.getId() : null,
                toTable.getId()
        );

        return mainOrder != null ? mapToOrderResponse(mainOrder) : null;
    }

    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item ->
                OrderItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItem().getId())
                        .menuItemName(item.getMenuItem().getName())
                        .imageUrl(item.getMenuItem().getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .itemStatus(item.getItemStatus())
                        .notes(item.getNotes())
                        .build()
        ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableId(order.getTable().getId())
                .tableNumber(order.getTable().getTableNumber())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : "Guest")
                .customerTokenSerial(order.getCustomer() != null ? order.getCustomer().getCustomerTokenSerial() : null)
                .sessionId(order.getCustomer() != null ? order.getCustomer().getSessionId() : null)
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .netAmount(order.getNetAmount())
                .status(order.getStatus())
                .notes(order.getNotes())
                .orderTime(order.getOrderTime() != null ? order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "")
                .items(itemResponses)
                .build();
    }
}
