import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestPaymentExecution {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String dbName = System.getenv().getOrDefault("DB_NAME", "defaultdb");
        String user = System.getenv().getOrDefault("DB_USERNAME", "avnadmin");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "");
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Testing payment queries against online database...");

            // 1. Get latest invoice
            long invoiceId = 0;
            long orderId = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM invoices ORDER BY id DESC LIMIT 1")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    invoiceId = rs.getLong("id");
                    orderId = rs.getLong("order_id");
                    System.out.println("Found Invoice #" + invoiceId + " for Order #" + orderId);
                }
            }

            if (invoiceId == 0) {
                System.out.println("No invoice found!");
                return;
            }

            // 2. Test Inserting Payment
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO payments (invoice_id, payment_method, transaction_ref, payment_status, paid_at) " +
                    "VALUES (?, 'CASH', 'TXN-12345', 'PENDING', NOW())")) {
                ps.setLong(1, invoiceId);
                ps.executeUpdate();
                System.out.println("Successfully inserted test CASH payment into payments table!");
            }

            // 3. Test Updating Invoice Status
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE invoices SET payment_status = 'CASH_PENDING_WAITER' WHERE id = ?")) {
                ps.setLong(1, invoiceId);
                ps.executeUpdate();
                System.out.println("Successfully updated invoice payment_status!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
