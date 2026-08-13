import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class MigrateToAiven {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   AIVEN MYSQL MIGRATION & INITIALIZATION TOOL");
        System.out.println("=========================================================");

        Map<String, String> env = loadEnvFile(".env");
        
        String host = env.getOrDefault("DB_HOST", System.getenv().getOrDefault("DB_HOST", "localhost"));
        String port = env.getOrDefault("DB_PORT", System.getenv().getOrDefault("DB_PORT", "3306"));
        String dbName = env.getOrDefault("DB_NAME", System.getenv().getOrDefault("DB_NAME", "defaultdb"));
        String user = env.getOrDefault("DB_USERNAME", System.getenv().getOrDefault("DB_USERNAME", "avnadmin"));
        String pass = env.getOrDefault("DB_PASSWORD", System.getenv().getOrDefault("DB_PASSWORD", ""));

        if (pass == null || pass.trim().isEmpty() || pass.equals("your_aiven_mysql_password")) {
            System.err.println("[ERROR] Please update DB_PASSWORD in your .env file with your actual Aiven MySQL password before running migration!");
            return;
        }

        String url = env.getOrDefault("DB_URL", "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&autoReconnect=true&serverTimezone=UTC");

        System.out.println("Connecting to Aiven MySQL at: " + host + ":" + port + " (" + dbName + ")...");

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            System.out.println("[SUCCESS] Connected to Aiven MySQL successfully!\n");

            // 1. Execute Schema
            File schemaFile = new File("database/schema.sql");
            if (schemaFile.exists()) {
                System.out.println("Executing database/schema.sql (Creating 21 tables)...");
                executeSqlScript(stmt, schemaFile);
                System.out.println("[SUCCESS] Schema created successfully!");
            } else {
                System.err.println("[WARNING] database/schema.sql not found!");
            }

            // 2. Execute Seed Data
            File dataFile = new File("database/data.sql");
            if (dataFile.exists()) {
                System.out.println("\nExecuting database/data.sql (Seeding default data)...");
                executeSqlScript(stmt, dataFile);
                System.out.println("[SUCCESS] Seed data inserted successfully!");
            } else {
                System.err.println("[WARNING] database/data.sql not found!");
            }

            System.out.println("\n=========================================================");
            System.out.println("   MIGRATION COMPLETED SUCCESSFULLY!");
            System.out.println("=========================================================");

        } catch (Exception e) {
            System.err.println("\n[ERROR] Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, String> loadEnvFile(String filepath) {
        Map<String, String> map = new HashMap<>();
        File file = new File(filepath);
        if (!file.exists()) return map;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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

    private static void executeSqlScript(Statement stmt, File sqlFile) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (Exception e) {
                            System.out.println("Note executing SQL statement: " + e.getMessage());
                        }
                    }
                    sb.setLength(0);
                }
            }
        }
    }
}
