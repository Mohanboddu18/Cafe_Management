# Deployment, Setup & Testing Walkthrough Guide

This guide provides step-by-step instructions for running, building, testing, and deploying the **Cafe Management System** across local development environments, Aiven Cloud MySQL databases, and Docker container clusters.

---

## 📋 System Requirements & Prerequisites

Before starting, ensure your host system has the following software installed:

| Requirement | Minimum Version | Recommended Version |
|---|---|---|
| **Java Development Kit (JDK)** | OpenJDK 21 LTS | Java 21 LTS |
| **Build Tool** | Apache Maven 3.9+ | Apache Maven 3.9.9 |
| **Node.js & npm** | Node v18.0.0+ | Node v20.x or LTS |
| **Database Engine** | MySQL Server 8.0+ | MySQL 8.0 / Aiven Cloud |
| **Container Engine (Optional)**| Docker Engine 24.0+ | Docker Desktop |

---

## 🛠️ Step 1: Environment Configuration (`.env`)

1. Locate the `.env` template file in the repository root directory:
   `c:/Users/mohan/Cafe Management/.env`
2. Update the environment credentials to match your local or cloud database setup:

```properties
# Database Connectivity Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cafe_management_db
DB_USERNAME=root
DB_PASSWORD=root

# JDBC Connection URL
DB_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

# JWT Secret Security Key
JWT_SECRET=CafeManagementSuperSecretKeyForJWTTokenGeneration2026SecureKey!

# Backend Port
SERVER_PORT=8080
```

---

## 🗄️ Step 2: Database Initialization

### Option A: Local MySQL Setup
1. Open MySQL Command Line or MySQL Workbench.
2. Run the initialization commands:
   ```sql
   CREATE DATABASE cafe_management_db;
   USE cafe_management_db;
   SOURCE database/schema.sql;
   SOURCE database/data.sql;
   ```

### Option B: Cloud Database Setup (Aiven Cloud MySQL)
If you prefer migrating to cloud hosted MySQL (such as Aiven Cloud):
1. Update `DB_HOST`, `DB_PORT`, `DB_USERNAME`, and `DB_PASSWORD` in `.env` with your Aiven credentials.
2. Compile and run the standalone migration script from `tools/`:
   ```bash
   javac -cp "cafe-backend/target/dependency/*" tools/MigrateToAiven.java
   java -cp "tools;cafe-backend/target/dependency/*" MigrateToAiven
   ```

---

## ☕ Step 3: Launching Spring Boot Backend

1. Navigate into the backend project folder:
   ```bash
   cd cafe-backend
   ```
2. Compile project dependencies and launch dev server:
   ```bash
   mvn clean spring-boot:run
   ```
3. Verify backend startup log:
   `Tomcat started on port 8080 (http) with context path '/'`
4. Access interactive OpenAPI Swagger UI documentation at:
   `http://localhost:8080/swagger-ui.html`

---

## 🅰️ Step 4: Launching Angular Frontend

1. Open a new terminal and navigate into the frontend project folder:
   ```bash
   cd cafe-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start Angular live-reload dev server:
   ```bash
   npm start
   ```
4. Access the applications in browser:
   - **Customer Digital QR Menu**: `http://localhost:4200/customer/menu?table=1`
   - **Staff Portal Login**: `http://localhost:4200/login`

---

## 🔑 Demo Account Credentials

| Role | Username | Password | Default Access Route |
|---|---|---|---|
| **Customer** | *(No Login)* | *(None)* | `/customer/menu?table=1` |
| **System Admin** | `admin` | `admin123` | `/admin/dashboard` |
| **Kitchen Chef** | `kitchen` | `kitchen123` | `/kitchen/dashboard` |
| **Floor Waiter** | `waiter` | `waiter123` | `/waiter/dashboard` |
| **Billing Cashier** | `cashier` | `cashier123` | `/cashier/billing` |

---

## 🐳 Step 5: Docker Containerization

To run the complete application as a containerized stack:

1. Build the multi-stage Docker container image:
   ```bash
   docker build -t cafe-management-app .
   ```
2. Launch the container mapping port 8080:
   ```bash
   docker run -d -p 8080:8080 --env-file .env --name cafe-app cafe-management-app
   ```

---

## 🧪 Step 6: Testing & Verification Utilities

The repository includes standalone test utilities located in `tools/`:

- **Verify Invoice Schema**: `java -cp tools CheckInvoicesTable`
- **Verify Reviews Schema**: `java -cp tools CheckReviewsTable`
- **Test PDF Invoice Generation**: `java -cp tools TestGenerateInvoice`
- **Test Payment Logic**: `java -cp tools TestPaymentExecution`
