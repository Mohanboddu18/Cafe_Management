import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestOrdersData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12834862?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "sql12834862";
        String pass = "y7ecySmrmQ";

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
}
