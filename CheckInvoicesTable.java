import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckInvoicesTable {
    public static void main(String[] args) {
        String url = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12834862?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "sql12834862";
        String pass = "y7ecySmrmQ";

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
}
