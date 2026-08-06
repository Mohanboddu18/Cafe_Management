# Database Design & Entity Relationship (ER) Specs

This document details the normalized relational database design (MySQL 8) for the **Cafe Management System**.

## ER Diagram Representation

```mermaid
erDiagram
    roles ||--o{ users : "has"
    roles ||--o{ role_permissions : "grants"
    permissions ||--o{ role_permissions : "includes"
    
    restaurant_tables ||--o| qr_codes : "generates"
    restaurant_tables ||--o{ customers : "seats"
    restaurant_tables ||--o{ cart : "holds"
    restaurant_tables ||--o{ orders : "places"
    
    categories ||--o{ menu_items : "contains"
    
    cart ||--o{ cart_items : "contains"
    menu_items ||--o{ cart_items : "referenced in"
    
    customers ||--o{ orders : "belongs to"
    orders ||--o{ order_items : "contains"
    menu_items ||--o{ order_items : "referenced in"
    
    orders ||--o| kitchen_orders : "queued in"
    orders ||--o| invoices : "billed in"
    invoices ||--o{ payments : "paid via"
    
    inventory ||--o{ stock_history : "logs"
```

## Summary of 21 Tables

1. `roles`: Master user roles (`ROLE_ADMIN`, `ROLE_WAITER`, `ROLE_KITCHEN`, `ROLE_CASHIER`).
2. `users`: System staff credentials with BCrypt password hashes.
3. `permissions`: Granular RBAC feature permissions.
4. `role_permissions`: Join table linking roles to permissions.
5. `restaurant_tables`: Physical table numbers, capacities, status, and QR tokens.
6. `qr_codes`: Generated ZXing Base64 QR code representations for tables.
7. `categories`: Digital menu categories with display order & images.
8. `menu_items`: Food & beverage items, prices, prep times, veg/non-veg status.
9. `customers`: Session-bound guest customer profiles.
10. `cart`: Active customer shopping carts.
11. `cart_items`: Menu items in customer cart with custom notes.
12. `orders`: Master order records, subtotal, discount, GST tax, status (`NEW`, `PREPARING`, `READY`, `SERVED`, `PAID`).
13. `order_items`: Specific line items in an order.
14. `kitchen_orders`: Live kitchen queue tracking estimated preparation time and completion.
15. `coupons`: Promotional discount coupons (Percentage or Flat).
16. `invoices`: Financial invoices with calculated subtotal, GST, and total payable.
17. `payments`: Payment records (`CASH`, `CARD`, `UPI`).
18. `inventory`: Raw ingredient stock levels & minimum required alert thresholds.
19. `stock_history`: Audit trail for stock additions, usage, and adjustments.
20. `notifications`: Role-targeted STOMP WebSocket notification log.
21. `audit_logs`: Detailed system security audit trail.
