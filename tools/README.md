# Database Migration & Verification Tools

This folder contains standalone Java utilities used for database migration, schema validation, test data seeding, and feature verification for the **Cafe Management System**.

---

## Tool Overview

| Script Name | Purpose | Description |
|---|---|---|
| **`MigrateToAiven.java`** | Cloud DB Migration | Reads `.env` configuration, connects to remote MySQL (e.g. Aiven Cloud), creates required database, and executes `schema.sql` and `data.sql`. |
| **`CheckInvoicesTable.java`** | Schema Inspection | Checks structural integrity of the `invoices` table and verifies key columns (`payment_status`, `tax`, `subtotal`, `total_amount`). |
| **`CheckReviewsTable.java`** | Feature Verification | Inspects `reviews` and `menu_items` tables to ensure rating columns and review relationships are properly indexed. |
| **`TestGenerateInvoice.java`** | PDF Invoicing Test | Direct standalone test script for iText 7 PDF invoice rendering and file output generation. |
| **`TestOrdersData.java`** | Order Data Inspection | Queries and logs active customer orders, item details, subtotal calculations, and status transitions. |
| **`TestPaymentExecution.java`** | Payment Logic Test | Simulates cashier billing transactions, coupon discount verification (`WELCOME10`, `FLAT50`), 5% GST tax, and payment record insertion. |

---

## How to Execute Tools

### Prerequisites
- JDK 21 installed and configured in system PATH.
- MySQL JDBC Driver (or Maven classpath resolution).
- `.env` file present in the project root directory (`c:/Users/mohan/Cafe Management/.env`).

### Compilation & Execution Examples

To compile and run any tool from the repository root:

```bash
# 1. Database Migration to Cloud (Aiven MySQL)
javac -cp "cafe-backend/target/dependency/*" tools/MigrateToAiven.java
java -cp "tools;cafe-backend/target/dependency/*" MigrateToAiven

# 2. Check Invoices Table Schema
javac tools/CheckInvoicesTable.java
java -cp tools CheckInvoicesTable

# 3. Test Payment Execution & Invoicing
javac tools/TestPaymentExecution.java
java -cp tools TestPaymentExecution
```
