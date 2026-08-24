import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class TestGenerateInvoice {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String dbName = System.getenv().getOrDefault("DB_NAME", "defaultdb");
        String user = System.getenv().getOrDefault("DB_USERNAME", "avnadmin");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "");
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to MySQL online database!");

            // 1. Get Order 1
            long orderId = 1L;
            BigDecimal subtotal = BigDecimal.ZERO;
            long tableId = 1L;

            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE id = ?")) {
                ps.setLong(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    subtotal = rs.getBigDecimal("total_amount");
                    tableId = rs.getLong("table_id");
                    System.out.println("Found Order #1: total_amount=" + subtotal + ", table_id=" + tableId + ", status=" + rs.getString("status"));
                } else {
                    System.out.println("Order #1 not found in database!");
                    return;
                }
            }

            // 2. Calculations
            BigDecimal gstPercentage = BigDecimal.valueOf(5.0);
            BigDecimal gstAmount = subtotal.multiply(gstPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal totalPayable = subtotal.add(gstAmount);

            System.out.println("Calculated subtotal=" + subtotal + ", gstAmount=" + gstAmount + ", totalPayable=" + totalPayable);

            // 3. Update Order
            try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET discount_amount = 0, tax_amount = ?, net_amount = ?, status = 'BILL_REQUESTED' WHERE id = ?")) {
                ps.setBigDecimal(1, gstAmount);
                ps.setBigDecimal(2, totalPayable);
                ps.setLong(3, orderId);
                ps.executeUpdate();
                System.out.println("Updated Order #1 to status BILL_REQUESTED");
            }

            // 4. Try Insert / Update Invoice
            String invoiceNumber = "INV-" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO invoices (invoice_number, order_id, subtotal, discount, coupon_code, gst_amount, total_payable, pdf_url, created_at, payment_status) " +
                    "VALUES (?, ?, ?, 0, NULL, ?, ?, ?, NOW(), 'PENDING') " +
                    "ON DUPLICATE KEY UPDATE subtotal = VALUES(subtotal), gst_amount = VALUES(gst_amount), total_payable = VALUES(total_payable), payment_status = VALUES(payment_status)")) {
                ps.setString(1, invoiceNumber);
                ps.setLong(2, orderId);
                ps.setBigDecimal(3, subtotal);
                ps.setBigDecimal(4, gstAmount);
                ps.setBigDecimal(5, totalPayable);
                ps.setString(6, "/api/cashier/invoice/" + orderId + "/pdf");
                ps.executeUpdate();
                System.out.println("Successfully inserted/updated invoice for Order #1!");
            }

            // 5. Try Insert Notification
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO notifications (target_role, title, message, order_id, table_id, is_read, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, 0, NOW())")) {
                ps.setString(1, "WAITER");
                ps.setString(2, "Bill Invoice Generated");
                ps.setString(3, "Bill generated for Table #1");
                ps.setLong(4, orderId);
                ps.setLong(5, tableId);
                ps.executeUpdate();
                System.out.println("Inserted notification for WAITER");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
