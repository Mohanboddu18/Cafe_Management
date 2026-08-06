# Enterprise Full-Stack Cafe Management System

A production-ready, modular, and full-stack **Cafe Management System** built with **Angular 20**, **Spring Boot (Java 21)**, **MySQL 8**, **JWT Security**, **WebSockets (STOMP)**, **ZXing QR Codes**, and **iText 7 PDF Invoicing**.

---

## Key System Features

- **No-Auth Customer QR Ordering**: Scans table QR code $\rightarrow$ opens table menu $\rightarrow$ cart customization $\rightarrow$ live WebSocket status tracking.
- **Live Kitchen Display System (KDS)**: Real-time order Kanban board with status transitions (`NEW` $\rightarrow$ `PREPARING` $\rightarrow$ `READY`) and Web Audio chime notifications.
- **Waiter Floor Control**: Table matrix overview, ready food pickup alerts, walk-in order placement, table status management.
- **Cashier Billing & Invoicing**: Coupon application (`WELCOME10`, `FLAT50`), 5% GST tax calculation, payment processing (Cash, Card, UPI), and downloadable iText PDF invoices.
- **Admin Management Portal**:
  - Today/Weekly/Monthly Revenue analytics & popular item leaderboards.
  - Category & Menu CRUD with image URLs and availability toggles.
  - Automatic ZXing QR code generator per table.
  - Employee RBAC user management.
  - Ingredient inventory stock tracking & low stock alerts.
  - Sales report export to Excel (`.xlsx`) via Apache POI.
  - Audit logging.

---

## Project Structure

```
c:/Users/mohan/Cafe Management/
├── database/
│   ├── schema.sql                    # 21 Normalized MySQL 8 Tables & Indexes
│   └── data.sql                      # Seed Data (Users, Menu, Tables, Coupons, Inventory)
├── cafe-backend/                      # Spring Boot (Java 21, Maven)
│   ├── pom.xml
│   └── src/main/java/com/cafe/management/
├── cafe-frontend/                     # Angular 20 Standalone Components & RxJS
│   ├── package.json
│   └── src/app/
├── documentation/                     # ER Diagram, Postman Specs & Deployment Guide
└── README.md
```

---

## Quick Start Instructions

1. **Import Database**:
   ```sql
   CREATE DATABASE cafe_management_db;
   USE cafe_management_db;
   SOURCE database/schema.sql;
   SOURCE database/data.sql;
   ```

2. **Start Backend**:
   ```bash
   cd cafe-backend
   mvn clean spring-boot:run
   ```

3. **Start Frontend**:
   ```bash
   cd cafe-frontend
   npm install
   npm start
   ```

4. **Access Applications**:
   - Customer Menu: `http://localhost:4200/customer/menu?table=1`
   - Staff Portal Login: `http://localhost:4200/login`
   - Swagger Documentation: `http://localhost:8080/swagger-ui.html`
