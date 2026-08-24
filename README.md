# Enterprise Full-Stack Cafe Management System

A production-ready, modular, and full-stack **Cafe Management System** built with **Angular 20**, **Spring Boot (Java 21)**, **MySQL 8**, **JWT Security**, **WebSockets (STOMP)**, **ZXing QR Codes**, **iText 7 PDF Invoicing**, and **Apache POI Excel Analytics**.

---

## 🌟 Key System Highlights

- 📱 **No-Auth Customer QR Ordering**: Scans table QR code $\rightarrow$ opens table menu $\rightarrow$ cart customization $\rightarrow$ live WebSocket status tracking.
- 🍳 **Live Kitchen Display System (KDS)**: Real-time order Kanban board with status transitions (`NEW` $\rightarrow$ `PREPARING` $\rightarrow$ `READY`) and Web Audio chime notifications.
- 👨‍🍳 **Waiter Floor Control**: Interactive table matrix overview, ready food pickup alerts, walk-in order placement, table status management.
- 💵 **Cashier Billing & Invoicing**: Coupon application (`WELCOME10`, `FLAT50`), 5% GST tax calculation, payment processing (Cash, Card, UPI), and downloadable iText 7 PDF invoices.
- 📊 **Admin Management Portal**:
  - Today/Weekly/Monthly Revenue analytics & popular item leaderboards.
  - Category & Menu CRUD with image URLs and availability toggles.
  - Automatic ZXing Base64 QR code generator per table.
  - Employee RBAC user management.
  - Ingredient inventory stock tracking & low stock alerts.
  - Sales report export to Excel (`.xlsx`) via Apache POI.
  - System audit logging.

---

## 📂 Project Directory Structure

```
c:/Users/mohan/Cafe Management/
├── .env                                  # Active environment configuration
├── .env.example                          # Template environment variables
├── .gitignore                            # Git build exclusions
├── Dockerfile                            # Multi-stage Docker deployment build file
├── README.md                             # Project root guide (This File)
│
├── database/                             # Relational Database Schemas & Seed Data
│   ├── schema.sql                        # 21 Normalized MySQL 8 Tables
│   └── data.sql                          # Seed data (Users, Roles, Menu, Coupons, Inventory)
│
├── tools/                                # Standalone Java Migration & Testing Utilities
│   ├── README.md                         # Usage guide for tools
│   ├── MigrateToAiven.java               # Aiven MySQL Cloud DB creator & runner
│   ├── CheckInvoicesTable.java           # Schema column inspection tool
│   ├── CheckReviewsTable.java            # Rating and review inspector
│   ├── TestGenerateInvoice.java          # iText 7 PDF rendering test
│   ├── TestOrdersData.java               # Orders DB query inspector
│   └── TestPaymentExecution.java         # Payment logic & coupon test
│
├── documentation/                        # Comprehensive Documentation Suite
│   ├── README.md                         # Documentation Hub Index & Master Portal
│   ├── PROJECT_STRUCTURE.md              # Detailed Blueprint of All Files & Packages
│   ├── ARCHITECTURE_AND_TECH_STACK.md    # Multi-Tier System Architecture & Data Flows
│   ├── BACKEND_ARCHITECTURE.md           # Spring Boot Engineering & Package Layout
│   ├── FRONTEND_ARCHITECTURE.md          # Angular 20 Standalone UI & CSS Design System
│   ├── DATABASE_SCHEMA_AND_MODELS.md     # 21 Tables Schema Reference & ER Diagram
│   ├── API_DOCUMENTATION.md              # REST & WebSocket API Specification
│   ├── Deployment_and_Testing_Guide.md   # Setup, Aiven Cloud DB, Docker & Testing Walkthrough
│   └── Postman_Collection.json           # Importable Postman Test Suite
│
├── cafe-backend/                         # Spring Boot 3.4 (Java 21 LTS) Application
│   ├── pom.xml                           # Maven dependencies
│   └── src/                              # Java controllers, services, entities, DTOs & security
│
└── cafe-frontend/                        # Angular 20 Standalone Application
    ├── package.json                      # Frontend dependencies
    └── src/                              # Components, RxJS services, routes & CSS system
```

---

## 📚 Complete Project Documentation

For full technical specifications, architecture blueprints, API references, and database models, explore the `documentation/` folder:

- 📁 [Detailed Project Structure Blueprint](file:///c:/Users/mohan/Cafe%20Management/documentation/PROJECT_STRUCTURE.md)
- 🏗️ [Architecture & Technology Stack Specifications](file:///c:/Users/mohan/Cafe%20Management/documentation/ARCHITECTURE_AND_TECH_STACK.md)
- ☕ [Backend Architecture Engineering Specs](file:///c:/Users/mohan/Cafe%20Management/documentation/BACKEND_ARCHITECTURE.md)
- 🅰️ [Frontend Architecture & Component Specs](file:///c:/Users/mohan/Cafe%20Management/documentation/FRONTEND_ARCHITECTURE.md)
- 🗄️ [Database Schema & ER Diagram](file:///c:/Users/mohan/Cafe%20Management/documentation/DATABASE_SCHEMA_AND_MODELS.md)
- 🔌 [REST & WebSocket API Reference](file:///c:/Users/mohan/Cafe%20Management/documentation/API_DOCUMENTATION.md)
- 🚀 [Deployment & Testing Guide](file:///c:/Users/mohan/Cafe%20Management/documentation/Deployment_and_Testing_Guide.md)
- 🛠️ [Database Migration & Verification Tools](file:///c:/Users/mohan/Cafe%20Management/tools/README.md)

---

## 🚀 Quick Start Instructions

1. **Setup Database**:
   ```sql
   CREATE DATABASE cafe_management_db;
   USE cafe_management_db;
   SOURCE database/schema.sql;
   SOURCE database/data.sql;
   ```

2. **Start Spring Boot Backend**:
   ```bash
   cd cafe-backend
   mvn clean spring-boot:run
   ```

3. **Start Angular Frontend**:
   ```bash
   cd cafe-frontend
   npm install
   npm start
   ```

4. **Access Application Interfaces**:
   - Customer Digital Menu: `http://localhost:4200/customer/menu?table=1`
   - Staff Portal Login: `http://localhost:4200/login`
   - OpenAPI Swagger Docs: `http://localhost:8080/swagger-ui.html`

---

## 🔑 Default Demo Staff Accounts

| Role | Username | Password | Default Path |
|---|---|---|---|
| **System Admin** | `admin` | `admin123` | `/admin/dashboard` |
| **Kitchen Chef** | `kitchen` | `kitchen123` | `/kitchen/dashboard` |
| **Floor Waiter** | `waiter` | `waiter123` | `/waiter/dashboard` |
| **Billing Cashier** | `cashier` | `cashier123` | `/cashier/billing` |
