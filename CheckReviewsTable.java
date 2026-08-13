import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckReviewsTable {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String dbName = System.getenv().getOrDefault("DB_NAME", "defaultdb");
        String user = System.getenv().getOrDefault("DB_USERNAME", "avnadmin");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "");
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true");

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Checking menu_item_reviews table:");
            try {
                ResultSet rs = stmt.executeQuery("DESCRIBE menu_item_reviews");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
                }
            } catch (Exception e) {
                System.out.println("menu_item_reviews table missing! Creating it...");
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS menu_item_reviews (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        menu_item_id BIGINT NOT NULL,
                        order_id BIGINT NOT NULL,
                        rating INT NOT NULL,
                        comment TEXT,
                        customer_name VARCHAR(100),
                        created_at DATETIME NULL,
                        FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE,
                        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
                """);
                System.out.println("menu_item_reviews table created successfully!");
            }

            // Check if average_rating and total_ratings exist on menu_items
            System.out.println("\nChecking menu_items table:");
            ResultSet rs = stmt.executeQuery("DESCRIBE menu_items");
            boolean hasAvg = false, hasTotal = false;
            while (rs.next()) {
                String f = rs.getString("Field");
                System.out.println(" - " + f + " (" + rs.getString("Type") + ")");
                if ("average_rating".equalsIgnoreCase(f)) hasAvg = true;
                if ("total_ratings".equalsIgnoreCase(f)) hasTotal = true;
            }

            if (!hasAvg) {
                stmt.executeUpdate("ALTER TABLE menu_items ADD COLUMN average_rating DOUBLE DEFAULT 5.0");
                System.out.println("Added average_rating to menu_items");
            }
            if (!hasTotal) {
                stmt.executeUpdate("ALTER TABLE menu_items ADD COLUMN total_ratings INT DEFAULT 0");
                System.out.println("Added total_ratings to menu_items");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
