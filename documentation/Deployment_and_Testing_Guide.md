# Deployment & Testing Guide

## Prerequisites

- **Java Development Kit**: JDK 21 LTS
- **Build Tool**: Apache Maven 3.9+
- **Node.js**: Node v18+ / v20+ / v25+ with npm
- **Database**: MySQL 8.0 Server

---

## 1. Database Setup

1. Open MySQL Command Line or MySQL Workbench.
2. Execute the schema script:
   ```sql
   SOURCE database/schema.sql;
   ```
3. Execute the seed data script:
   ```sql
   SOURCE database/data.sql;
   ```

---

## 2. Spring Boot Backend Setup

1. Navigate to backend directory:
   ```bash
   cd cafe-backend
   ```
2. Verify `src/main/resources/application.properties` database password:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=root
   ```
3. Compile and launch backend:
   ```bash
   mvn clean spring-boot:run
   ```
4. Access OpenAPI / Swagger Documentation at:
   `http://localhost:8080/swagger-ui.html`

---

## 3. Angular Frontend Setup

1. Navigate to frontend directory:
   ```bash
   cd cafe-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Launch development server:
   ```bash
   npm start
   ```
4. Access Customer Digital Menu in browser at:
   `http://localhost:4200/customer/menu?table=1`

---

## 4. Default Demo Accounts

| Role | Username | Password | Access Path |
|---|---|---|---|
| **Customer** | *(No Login)* | *(None)* | `/customer/menu?table=1` |
| **Admin** | `admin` | `admin123` | `/admin/dashboard` |
| **Kitchen Chef** | `kitchen` | `kitchen123` | `/kitchen/dashboard` |
| **Waiter** | `waiter` | `waiter123` | `/waiter/dashboard` |
| **Cashier** | `cashier` | `cashier123` | `/cashier/billing` |
