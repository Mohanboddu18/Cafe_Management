# Complete REST API & WebSocket Specifications

This document provides a comprehensive API specification for the **Cafe Management System** backend services.

---

## 🌐 API Fundamentals

- **Base REST URL**: `http://localhost:8080/api`
- **WebSocket Gateway**: `ws://localhost:8080/ws`
- **Authentication Protocol**: HTTP Bearer Token (`Authorization: Bearer <JWT_TOKEN>`)
- **Data Exchange Format**: JSON (`Content-Type: application/json`)

---

## 🔐 1. Authentication Endpoints

### `POST /api/auth/login`
Authenticates staff members and returns JWT token.

- **Access**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **Success Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "expiresInMs": 86400000
}
```

---

## ☕ 2. Customer Endpoints (No Login Required)

### `GET /api/customer/menu`
Fetches digital menu categories and active menu items for a specific table.

- **Access**: Public
- **Query Parameter**: `table=1`
- **Success Response (200 OK)**:
```json
[
  {
    "categoryId": 1,
    "categoryName": "Beverages",
    "items": [
      {
        "id": 101,
        "name": "Iced Hazelnut Latte",
        "description": "Espresso shot blended with roasted hazelnut syrup & cold milk.",
        "price": 249.00,
        "isVeg": true,
        "isAvailable": true,
        "prepTimeMinutes": 10,
        "imageUrl": "https://images.unsplash.com/photo-1517701604599-bb29b565090c"
      }
    ]
  }
]
```

### `POST /api/customer/order`
Submits a new customer cart order.

- **Access**: Public
- **Request Body**:
```json
{
  "tableNumber": 1,
  "items": [
    {
      "menuItemId": 101,
      "quantity": 2,
      "specialNotes": "Extra cold milk, less sugar"
    }
  ]
}
```
- **Success Response (201 Created)**:
```json
{
  "orderId": 104,
  "orderNumber": "ORD-2026-104",
  "tableNumber": 1,
  "subtotal": 498.00,
  "taxAmount": 24.90,
  "totalAmount": 522.90,
  "orderStatus": "NEW",
  "createdAt": "2026-08-24T21:20:00Z"
}
```

### `GET /api/customer/order/{id}`
Retrieves live order status details for customer tracking.

- **Access**: Public
- **Success Response (200 OK)**: Status string (`NEW`, `PREPARING`, `READY`, `SERVED`, `PAID`).

---

## 🍳 3. Kitchen KDS Endpoints

### `GET /api/kitchen/orders`
Returns active kitchen order queue cards sorted by timestamp.

- **Access**: Requires `ROLE_KITCHEN` or `ROLE_ADMIN`
- **Success Response (200 OK)**: Array of active kitchen orders with line items and customer notes.

### `PUT /api/kitchen/orders/{id}/status`
Updates order status across KDS Kanban state machine.

- **Access**: Requires `ROLE_KITCHEN` or `ROLE_ADMIN`
- **Query Parameter**: `status=PREPARING` or `status=READY`
- **Success Response (200 OK)**: Updated order payload. Triggers real-time WebSocket broadcast.

---

## 👨‍🍳 4. Waiter Floor Control Endpoints

### `GET /api/waiter/tables`
Retrieves interactive table grid status overview.

- **Access**: Requires `ROLE_WAITER` or `ROLE_ADMIN`
- **Success Response (200 OK)**: List of tables with seating capacities and statuses (`VACANT`, `OCCUPIED`, `RESERVED`).

### `PUT /api/waiter/tables/{id}/status`
Updates dining table status manually (e.g. marking table clean/vacant).

- **Access**: Requires `ROLE_WAITER` or `ROLE_ADMIN`

---

## 💵 5. Cashier Billing & Invoicing Endpoints

### `GET /api/cashier/orders/active`
Retrieves unbilled orders ready for checkout.

- **Access**: Requires `ROLE_CASHIER` or `ROLE_ADMIN`

### `POST /api/cashier/coupons/validate`
Validates promotional coupon code against order subtotal.

- **Access**: Requires `ROLE_CASHIER` or `ROLE_ADMIN`
- **Request Body**:
```json
{
  "couponCode": "WELCOME10",
  "subtotal": 500.00
}
```
- **Success Response (200 OK)**:
```json
{
  "valid": true,
  "discountType": "PERCENTAGE",
  "discountValue": 10.00,
  "discountAmount": 50.00
}
```

### `POST /api/cashier/invoices/process`
Processes payment and generates tax invoice.

- **Access**: Requires `ROLE_CASHIER` or `ROLE_ADMIN`
- **Request Body**:
```json
{
  "orderId": 104,
  "couponCode": "WELCOME10",
  "paymentMethod": "UPI"
}
```
- **Success Response (200 OK)**: Complete Invoice JSON details.

### `GET /api/cashier/invoices/{id}/pdf`
Downloads compiled iText 7 PDF invoice file.

- **Access**: Requires `ROLE_CASHIER` or `ROLE_ADMIN`
- **Response**: Binary stream (`Content-Type: application/pdf`).

---

## 📊 6. Admin Management Endpoints

### `GET /api/admin/dashboard/analytics`
Fetches high-level metrics for dashboard cards.

- **Access**: Requires `ROLE_ADMIN`
- **Response**: Revenue today, total orders, low stock items count, top selling menu items.

### `POST /api/admin/menu-items`
Creates new menu item.

- **Access**: Requires `ROLE_ADMIN`

### `GET /api/admin/tables/{id}/qr`
Generates Base64 ZXing QR image string for table.

- **Access**: Requires `ROLE_ADMIN`

### `GET /api/admin/reports/export-excel`
Downloads Excel sales report `.xlsx` file generated via Apache POI.

- **Access**: Requires `ROLE_ADMIN`
- **Response**: Binary stream (`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`).

---

## 📡 7. Real-Time STOMP WebSocket Topics

| Destination Channel | Message Type | Recipient | Description |
|---|---|---|---|
| `/topic/kitchen-orders` | JSON Payload | Kitchen KDS UI | Triggers real-time card addition & chime sound when customer places order. |
| `/topic/order-status/{orderId}` | JSON Payload | Customer Tracking UI | Live status progress update (`NEW` $\rightarrow$ `PREPARING` $\rightarrow$ `READY`). |
| `/topic/waiter-alerts` | JSON Payload | Waiter Floor UI | Broadcasts instant notification when order status becomes `READY` for table delivery. |
