import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckInvoicesTable {
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
            
            System.out.println("Connected to MySQL successfully!");
            
            // Check columns in invoices table
            ResultSet rs = stmt.executeQuery("DESCRIBE invoices");
            System.out.println("Columns in invoices table:");
            boolean hasPaymentStatus = false;
            while (rs.next()) {
                String field = rs.getString("Field");
                String type = rs.getString("Type");
                System.out.println(" - " + field + " (" + type + ")");
                if ("payment_status".equalsIgnoreCase(field)) {
                    hasPaymentStatus = true;
                }
            }
            
            if (!hasPaymentStatus) {
                System.out.println("ADDING missing payment_status column to invoices table...");
                stmt.executeUpdate("ALTER TABLE invoices ADD COLUMN payment_status VARCHAR(30) DEFAULT 'PENDING' AFTER total_payable");
                System.out.println("Successfully added payment_status column to invoices table!");
            } else {
                System.out.println("invoices table already has payment_status column.");
            }

            // Check columns in other tables as well
            System.out.println("\nChecking payments table:");
            rs = stmt.executeQuery("DESCRIBE payments");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
            }

            System.out.println("\nChecking orders table:");
            rs = stmt.executeQuery("DESCRIBE orders");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
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
