import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestOrdersData {
    public static void main(String[] args) {
        java.util.Map<String, String> env = loadEnvFile(".env");
        String host = env.getOrDefault("DB_HOST", System.getenv().getOrDefault("DB_HOST", "localhost"));
        String port = env.getOrDefault("DB_PORT", System.getenv().getOrDefault("DB_PORT", "3306"));
        String dbName = env.getOrDefault("DB_NAME", System.getenv().getOrDefault("DB_NAME", "defaultdb"));
        String user = env.getOrDefault("DB_USERNAME", System.getenv().getOrDefault("DB_USERNAME", "avnadmin"));
        String pass = env.getOrDefault("DB_PASSWORD", System.getenv().getOrDefault("DB_PASSWORD", ""));
        String url = env.getOrDefault("DB_URL", "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true");

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Querying orders in database:");
            ResultSet rs = stmt.executeQuery("SELECT id, order_number, table_id, total_amount, status FROM orders");
            while (rs.next()) {
                System.out.println("Order #" + rs.getLong("id") + " | Num: " + rs.getString("order_number") + " | Table: " + rs.getLong("table_id") + " | Total: " + rs.getDouble("total_amount") + " | Status: " + rs.getString("status"));
            }

            System.out.println("\nQuerying invoices in database:");
            rs = stmt.executeQuery("SELECT id, invoice_number, order_id, total_payable, payment_status FROM invoices");
            while (rs.next()) {
                System.out.println("Invoice #" + rs.getLong("id") + " | InvNum: " + rs.getString("invoice_number") + " | Order: " + rs.getLong("order_id") + " | Total: " + rs.getDouble("total_payable") + " | Status: " + rs.getString("payment_status"));
            }

            System.out.println("\nQuerying tables in database:");
            rs = stmt.executeQuery("SELECT id, table_number, status, capacity FROM restaurant_tables");
            while (rs.next()) {
                System.out.println("Table #" + rs.getLong("id") + " | Number: " + rs.getInt("table_number") + " | Status: " + rs.getString("status"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static java.util.Map<String, String> loadEnvFile(String filepath) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        java.io.File file = new java.io.File(filepath);
        if (!file.exists()) return map;

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();
                    map.put(key, val);
                }
            }
        } catch (Exception ignored) {}
        return map;
    }
}
