# Backend Architecture & Engineering Specifications (Spring Boot 3.4 / Java 21)

This document provides a detailed technical specification of the backend application powering the **Cafe Management System**.

---

## 📦 Package Layout & Structure

The backend application is structured within `cafe-backend/src/main/java/com/cafe/management/` following standard Spring Boot layered architectural conventions.

```
com.cafe.management/
├── CafeApplication.java               # Application Entry Point (@SpringBootApplication)
│
├── config/                            # Framework Configurations
│   ├── SecurityConfig.java            # Spring Security 6 filter chain, CORS & endpoint permissions
│   ├── WebConfig.java                 # Web MVC CORS mapping configuration
│   └── WebSocketConfig.java           # STOMP WebSocket message broker configuration
│
├── controller/                        # REST Controllers (@RestController)
│   ├── AdminController.java           # Analytics, Menu CRUD, Tables & User Management (/api/admin)
│   ├── AuthController.java            # User authentication & token issuance (/api/auth)
│   ├── CashierController.java         # Billing, Coupon evaluation & PDF generation (/api/cashier)
│   ├── CustomerController.java        # Digital Menu, Cart & QR Order placement (/api/customer)
│   ├── KitchenController.java         # KDS Kanban queue management & status updates (/api/kitchen)
│   ├── ReviewController.java          # Customer reviews & feedback submission (/api/reviews)
│   ├── RootController.java            # API Health Check & system status endpoints (/api)
│   ├── WaiterController.java          # Table matrix, status toggle & walk-in orders (/api/waiter)
│   └── WebSocketController.java       # STOMP message payload listeners
│
├── dto/                               # Data Transfer Objects
│   ├── AuthRequest.java               # Login payload (username, password)
│   ├── AuthResponse.java              # JWT authentication response (token, role, username)
│   ├── OrderRequest.java               # Customer checkout order payload (tableNumber, items, notes)
│   ├── OrderStatusUpdateRequest.java   # KDS status update payload (orderId, newStatus)
│   ├── BillingRequest.java            # Cashier billing payload (orderId, couponCode, paymentMethod)
│   ├── InvoiceResponse.java           # Invoice summary payload (subtotal, tax, discount, total)
│   ├── MenuCategoryDTO.java           # Menu category representation
│   ├── MenuItemDTO.java               # Menu item representation with availability toggle
│   ├── ReviewDTO.java                 # Customer review rating payload
│   └── InventoryDTO.java              # Ingredient stock management DTO
│
├── entity/                            # JPA Hibernate Entities (@Entity)
│   ├── User.java                      # Staff account credentials and assigned roles
│   ├── Role.java                      # Security authority roles (ROLE_ADMIN, etc.)
│   ├── RestaurantTable.java           # Physical tables, capacity, status & QR code reference
│   ├── QRCodeEntity.java              # Base64 encoded table QR code representations
│   ├── Category.java                  # Menu category domain model
│   ├── MenuItem.java                  # Food & Beverage item domain model
│   ├── Customer.java                  # Session customer profile
│   ├── Cart.java                      # Customer active cart
│   ├── CartItem.java                  # Cart line item
│   ├── Order.java                     # Master order record
│   ├── OrderItem.java                 # Order line item with pricing snapshot
│   ├── KitchenOrder.java              # KDS active queue tracking entity
│   ├── Coupon.java                    # Discount coupon definition
│   ├── Invoice.java                   # Financial billing record
│   ├── Payment.java                   # Payment transaction record
│   ├── Inventory.java                 # Ingredient stock tracking entity
│   ├── StockHistory.java              # Stock audit log entity
│   ├── Review.java                    # Customer rating entity
│   ├── Notification.java              # WebSocket notification log entity
│   └── AuditLog.java                  # System audit trail entity
│
├── enums/                             # Domain Enums
│   ├── OrderStatus.java               # NEW, PREPARING, READY, SERVED, CANCELLED, PAID
│   ├── TableStatus.java               # VACANT, OCCUPIED, RESERVED, NEEDS_CLEANING
│   ├── PaymentMethod.java             # CASH, CARD, UPI
│   ├── PaymentStatus.java             # PENDING, COMPLETED, FAILED, REFUNDED
│   └── DiscountType.java              # PERCENTAGE, FLAT
│
├── exception/                         # Global Exception Handling
│   ├── GlobalExceptionHandler.java    # @ControllerAdvice intercepting exceptions into clean JSON
│   ├── ResourceNotFoundException.java # HTTP 404 handler
│   ├── BadRequestException.java       # HTTP 400 validation handler
│   └── UnauthorizedException.java     # HTTP 401 authentication handler
│
├── repository/                        # Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── RestaurantTableRepository.java
│   ├── CategoryRepository.java
│   ├── MenuItemRepository.java
│   ├── OrderRepository.java
│   ├── KitchenOrderRepository.java
│   ├── CouponRepository.java
│   ├── InvoiceRepository.java
│   ├── PaymentRepository.java
│   ├── InventoryRepository.java
│   └── ReviewRepository.java
│
├── security/                          # Spring Security Infrastructure
│   ├── CustomUserDetailsService.java  # Loads UserDetails from database
│   ├── JwtAuthenticationFilter.java   # Extends OncePerRequestFilter for Bearer token validation
│   ├── JwtTokenProvider.java          # JJWT token generation, signing & claim parser
│   └── JwtAuthenticationEntryPoint.java# Returns 401 Unauthorized JSON on unauthenticated access
│
└── service/                           # Business Logic & External Integrations
    ├── AdminService.java              # Business logic for analytics, menu CRUD, ZXing QR generation
    ├── AuthService.java               # User login & password encoder service
    ├── BillingService.java            # Billing calculation, coupon validation & iText 7 PDF rendering
    ├── CustomerService.java           # Menu retrieval, shopping cart & order placement
    ├── InventoryService.java          # Ingredient tracking & low-stock alerts
    ├── KitchenService.java            # KDS Kanban queue updates & WebSocket broadcasts
    ├── ReviewService.java             # Customer review submission & calculation
    ├── TableService.java              # Table status management & floor control
    └── WebSocketService.java          # STOMP messaging wrapper service
```

---

## 🔧 Key Service Implementation Details

### 1. `BillingService.java` (Tax & Coupon Engine)
- **Subtotal Calculation**: Sum of line items (`quantity * priceAtOrder`).
- **Discount Evaluation**:
  - `PERCENTAGE`: `(subtotal * coupon.discountValue) / 100` (capped at max discount if specified).
  - `FLAT`: Fixed amount discount up to subtotal.
- **GST Tax Calculation**: 5% tax applied to `(subtotal - discount)`.
- **Total Amount**: `(subtotal - discount) + tax`.
- **iText 7 Invoice PDF Generation**: Generates standard formatted PDF documents programmatically using iText 7 `PdfDocument`, `Document`, and `Table` elements.

### 2. `KitchenService.java` (Real-Time KDS Kanban)
- Manages order state machine: `NEW` $\rightarrow$ `PREPARING` $\rightarrow$ `READY` $\rightarrow$ `SERVED`.
- On state change, invokes `WebSocketService.convertAndSend()` to publish STOMP payloads:
  - Destination `/topic/kitchen-orders` (for KDS UI update)
  - Destination `/topic/order-status/{orderId}` (for customer tracking UI)
  - Destination `/topic/waiter-alerts` (for waiter notifications when status becomes `READY`)

### 3. `AdminService.java` (ZXing QR & Excel Analytics)
- Generates base64 QR images for tables using ZXing `QRCodeWriter`.
- Exports daily/weekly/monthly sales metrics to Excel `.xlsx` format using Apache POI `XSSFWorkbook`, creating styled headers, item summary tables, and total revenue calculations.

---

## ⚙️ Configuration Properties (`application.properties`)

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration (Resolved dynamically via environment variables with fallbacks)
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/cafe_management_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Security Secrets
app.jwt.secret=${JWT_SECRET:CafeManagementSuperSecretKeyForJWTTokenGeneration2026SecureKey!}
app.jwt.expiration-ms=86400000

# WebSocket Settings
spring.websocket.path=/ws
```
