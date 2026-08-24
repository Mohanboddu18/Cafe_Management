# Detailed Project Structure & Directory Blueprint

This document provides a comprehensive structural analysis of the **Cafe Management System** codebase. It covers every directory, package, class, component, configuration file, database script, and utility tool in detail.

---

## 📂 Master Directory Tree View

```
c:/Users/mohan/Cafe Management/
├── .env                                  # Active local environment configuration & secrets
├── .env.example                          # Template environment variable file
├── .gitignore                            # Git file ignore specifications (excludes target/, node_modules/, .class)
├── Dockerfile                            # Root multi-stage Docker build configuration for containerization
├── README.md                             # Master repository README & Quick Start Guide
│
├── database/                             # Database SQL Schemas & Initial Seed Data
│   ├── schema.sql                        # 21 Normalized MySQL 8 relational tables, foreign keys & indexes
│   └── data.sql                          # Seed data (Default Admin, Roles, Menu Items, Coupons, Inventory)
│
├── tools/                                # Dedicated Standalone Java Migration & Testing Utilities
│   ├── README.md                         # Instructions for compiling & running tools
│   ├── CheckInvoicesTable.java           # Utility script verifying invoices schema columns
│   ├── CheckReviewsTable.java            # Utility script verifying reviews & rating columns
│   ├── MigrateToAiven.java               # Automated Aiven MySQL Cloud database creation & script runner
│   ├── TestGenerateInvoice.java          # PDF generation & iText 7 invoice rendering test script
│   ├── TestOrdersData.java               # Database query test script for customer orders
│   └── TestPaymentExecution.java         # Payment logic, GST tax, and coupon discount validation test
│
├── documentation/                        # Project Documentation Suite
│   ├── README.md                         # Central Documentation Portal Index
│   ├── PROJECT_STRUCTURE.md              # Complete Project Blueprint & File Breakdown (This File)
│   ├── ARCHITECTURE_AND_TECH_STACK.md    # System Architecture, Tech Stack & Data Flow Specs
│   ├── BACKEND_ARCHITECTURE.md           # Spring Boot Package & Class Engineering Specs
│   ├── FRONTEND_ARCHITECTURE.md          # Angular 20 Standalone Component & RxJS Architecture
│   ├── DATABASE_SCHEMA_AND_MODELS.md     # 21 Tables Schema Reference & Mermaid ER Diagram
│   ├── API_DOCUMENTATION.md              # REST & WebSocket API Specification
│   ├── DEPLOYMENT_AND_TESTING_GUIDE.md   # Setup, Aiven Cloud DB, Docker & Testing Walkthrough
│   └── Postman_Collection.json           # Importable Postman API Test Collection
│
├── cafe-backend/                         # Spring Boot 3.4 (Java 21 LTS) Backend Application
│   ├── pom.xml                           # Maven dependencies (Spring Web, JPA, Security, WebSockets, iText, POI, ZXing)
│   ├── Dockerfile                        # Backend specific multi-stage Docker build file
│   ├── .dockerignore                     # Docker build exclusion rules
│   └── src/
│       ├── main/
│       │   ├── java/com/cafe/management/
│       │   │   ├── CafeApplication.java   # Spring Boot Main Entry Point
│       │   │   ├── config/               # Security, Web, & WebSocket Spring configurations
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── WebConfig.java
│       │   │   │   └── WebSocketConfig.java
│       │   │   ├── controller/           # REST API Controllers (Admin, Auth, Billing, Customer, Kitchen, Waiter, Review)
│       │   │   │   ├── AdminController.java
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── CashierController.java
│       │   │   │   ├── CustomerController.java
│       │   │   │   ├── KitchenController.java
│       │   │   │   ├── ReviewController.java
│       │   │   │   ├── RootController.java
│       │   │   │   ├── WaiterController.java
│       │   │   │   └── WebSocketController.java
│       │   │   ├── dto/                  # Data Transfer Objects for API requests & responses
│       │   │   ├── entity/               # JPA Hibernate Entities (21 mapped tables)
│       │   │   ├── enums/                # Order, Payment, and Role Enums
│       │   │   ├── exception/            # Global Exception Handling & Custom Exception classes
│       │   │   ├── repository/           # Spring Data JPA Repository interfaces
│       │   │   ├── security/             # JWT Token Provider, Authentication Filter & Custom UserDetailsService
│       │   │   └── service/              # Business Logic Services & Implementations
│       │   └── resources/
│       │       ├── application.properties # Spring Boot configuration properties
│       │       └── templates/            # Invoice PDF HTML/CSS templates
│       └── test/                         # JUnit 5 & Mockito Unit / Integration Tests
│
└── cafe-frontend/                        # Angular 20 Standalone Frontend Application
    ├── package.json                      # npm dependencies & package scripts
    ├── angular.json                      # Angular CLI project configuration
    ├── proxy.conf.json                   # Dev server proxy configuration to backend API (port 8080)
    ├── tsconfig.json                     # TypeScript compiler configuration
    └── src/
        ├── index.html                    # Root HTML file with Google Fonts & icons
        ├── styles.css                    # Global CSS design system (Variables, Glassmorphism, Utilities)
        ├── main.ts                       # Angular Application Bootstrap entry point
        └── app/
            ├── app.component.ts          # Root Angular Shell Component
            ├── app.routes.ts             # Application Navigation Route Definitions & Guards
            ├── core/                     # Core Business Logic, Services, Guards & Interceptors
            │   ├── guards/               # Auth & Role-Based Route Guards
            │   ├── interceptors/         # JWT Bearer Token HTTP Interceptor
            │   ├── models/               # TypeScript interfaces & domain models
            │   └── services/             # Injectable RxJS HTTP & WebSocket Services
            ├── features/                 # Modular Feature Page Components
            │   ├── admin/                # Admin Portal (Dashboard, Menu CRUD, Inventory, Analytics, Employees)
            │   ├── auth/                 # Login Component
            │   ├── cashier/              # Billing, Coupon Processing & Invoice Printing Component
            │   ├── customer/             # No-Auth Digital Menu, Cart, Checkout & Order Tracking
            │   ├── kitchen/              # Real-Time KDS Kanban Order Board
            │   └── waiter/               # Table Matrix, Walk-In Order & Alert Component
            └── shared/                   # Shared Reusable Components (Header, Navbar, Footer, Modal, Toast)
```

---

## 🔍 Detailed Component & Directory Breakdown

### 1. Project Root Directory
- **[`.env`](file:///c:/Users/mohan/Cafe%20Management/.env)**: Holds active environment secrets (Database URL, credentials, JWT Secret key, server port).
- **[`.env.example`](file:///c:/Users/mohan/Cafe%20Management/.env.example)**: Environment template providing dummy keys for developers setting up new environments.
- **[`Dockerfile`](file:///c:/Users/mohan/Cafe%20Management/Dockerfile)**: Multi-stage Docker build file that compiles backend source code via Maven 3.9 and packages it into a lightweight JRE 21 container image.
- **[`README.md`](file:///c:/Users/mohan/Cafe%20Management/README.md)**: Main landing document explaining key system features, project structure, and quick start setup steps.

### 2. `database/` Directory
- **[`database/schema.sql`](file:///c:/Users/mohan/Cafe%20Management/database/schema.sql)**: Production database definition script containing 21 normalized MySQL 8 table schemas (`users`, `roles`, `restaurant_tables`, `categories`, `menu_items`, `orders`, `invoices`, `inventory`, etc.) complete with primary keys, foreign key constraints, indexes, and timestamps.
- **[`database/data.sql`](file:///c:/Users/mohan/Cafe%20Management/database/data.sql)**: Initial database seed script populated with default system roles, pre-configured admin and staff credentials, sample menu categories, items, table numbers, coupons, and initial inventory stock.

### 3. `tools/` Directory
- **[`tools/README.md`](file:///c:/Users/mohan/Cafe%20Management/tools/README.md)**: Technical guide detailing how to compile and execute standalone Java tools.
- **[`tools/MigrateToAiven.java`](file:///c:/Users/mohan/Cafe%20Management/tools/MigrateToAiven.java)**: Standalone JDBC migration script that reads `.env`, connects to remote Aiven MySQL cloud databases, creates the target database, and executes `schema.sql` and `data.sql`.
- **[`tools/CheckInvoicesTable.java`](file:///c:/Users/mohan/Cafe%20Management/tools/CheckInvoicesTable.java)**: Verification tool inspecting schema columns for invoice calculation.
- **[`tools/CheckReviewsTable.java`](file:///c:/Users/mohan/Cafe%20Management/tools/CheckReviewsTable.java)**: Inspection script for review ratings and menu item linkages.
- **[`tools/TestGenerateInvoice.java`](file:///c:/Users/mohan/Cafe%20Management/tools/TestGenerateInvoice.java)**: Standalone execution script verifying iText 7 PDF rendering mechanics.
- **[`tools/TestOrdersData.java`](file:///c:/Users/mohan/Cafe%20Management/tools/TestOrdersData.java)**: Database query test script analyzing active customer orders.
- **[`tools/TestPaymentExecution.java`](file:///c:/Users/mohan/Cafe%20Management/tools/TestPaymentExecution.java)**: Billing and payment logic test validating coupon application and GST tax computation.

### 4. `documentation/` Directory
Contains the 8 complete documentation files forming the project's official engineering repository documentation suite.

### 5. `cafe-backend/` (Spring Boot 3.4 / Java 21)
- **[`pom.xml`](file:///c:/Users/mohan/Cafe%20Management/cafe-backend/pom.xml)**: Maven configuration defining Java 21 target version and dependencies:
  - `spring-boot-starter-web` & `spring-boot-starter-security`
  - `spring-boot-starter-data-jpa` & `mysql-connector-j`
  - `spring-boot-starter-websocket` (STOMP protocol)
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JWT security tokens)
  - `com.google.zxing:core` & `javase` (QR Code Base64 generation)
  - `com.itextpdf:itext7-core` (PDF invoice rendering)
  - `org.apache.poi:poi-ooxml` (Excel analytics report generation)
- **`src/main/java/com/cafe/management/`**:
  - **`CafeApplication.java`**: Spring Boot application bootstrapper.
  - **`config/`**: Security configuration (`SecurityConfig.java`), CORS configuration (`WebConfig.java`), and WebSocket STOMP endpoint registration (`WebSocketConfig.java`).
  - **`controller/`**: Exposes REST endpoints for Admin, Auth, Cashier Billing, Customer Digital Menu, KDS Kitchen Display, Waiter Floor, and Customer Reviews.
  - **`service/`**: Core business domain logic handling order status state machines, inventory stock deduction, coupon validation, PDF generation, and STOMP notifications.
  - **`entity/`**: 21 JPA entity classes mapping database tables with Hibernate annotations.
  - **`repository/`**: Spring Data JPA repositories with custom JPQL queries.
  - **`security/`**: JWT Token filtering, token validation, user authentication entry point, and custom `UserDetailsService`.

### 6. `cafe-frontend/` (Angular 20 Standalone)
- **[`package.json`](file:///c:/Users/mohan/Cafe%20Management/cafe-frontend/package.json)**: Angular 20 dependencies including `@angular/router`, `@stomp/stompjs`, `@fortawesome/angular-fontawesome`, and build scripts.
- **[`angular.json`](file:///c:/Users/mohan/Cafe%20Management/cafe-frontend/angular.json)**: CLI workspace options and build target configurations.
- **[`src/styles.css`](file:///c:/Users/mohan/Cafe%20Management/cafe-frontend/src/styles.css)**: Central styling sheet featuring modern CSS custom variables, vibrant dark mode palette, glassmorphic cards, custom scrollbars, and responsive grid layouts.
- **`src/app/`**:
  - **`app.routes.ts`**: Route configuration defining path mappings and attaching role-based `AuthGuard` protections.
  - **`core/services/`**: Injectable RxJS services (`AuthService`, `CartService`, `MenuService`, `OrderService`, `WebSocketService`, `KitchenService`, `BillingService`, `AdminService`, `ReviewService`).
  - **`features/`**: Feature components for Customer Digital Menu, Kitchen Kanban Display, Waiter Table Matrix, Cashier Billing, and Admin Analytics & CRUD Management.
