package finance.management.system.pkg0.pkg1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseBootstrap {
    private static final Path PROJECT_ROOT = Paths.get(".").toAbsolutePath().normalize();
    private static final Path DATABASE_DIR = PROJECT_ROOT.resolve("database");
    private static final Path DATABASE_FILE = DATABASE_DIR.resolve("fintrack.db");
    private static final Path SCHEMA_FILE = DATABASE_DIR.resolve("schema.sql");
    private static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_FILE;

    public static void recreate() throws IOException {
        try {
            Files.deleteIfExists(DATABASE_FILE);
            initialize();
        } catch (SecurityException ex) {
            throw new IllegalStateException("Access denied while recreating the database.", ex);
        }
    }

    public static void initialize() throws IOException {
        try {
            Files.createDirectories(DATABASE_DIR);
            if (!Files.exists(SCHEMA_FILE)) {
                throw new IOException("Database schema is missing: " + SCHEMA_FILE);
            }

            String schema = Files.readString(SCHEMA_FILE, StandardCharsets.UTF_8);
            try (Connection connection = DriverManager.getConnection(JDBC_URL);
                    Statement statement = connection.createStatement()) {
                connection.setAutoCommit(false);
                statement.execute("PRAGMA foreign_keys = ON");
                for (String sql : schema.split(";\\s*")) {
                    if (!sql.trim().isEmpty() && !sql.trim().startsWith("PRAGMA")) {
                        statement.execute(sql);
                    }
                }
                connection.commit();
            } catch (SQLException ex) {
                throw new IllegalStateException(
                        "SQLite initialization failed. Add the SQLite JDBC driver to the Java project.", ex);
            }
        } catch (IOException ex) {
            throw new IOException("Could not initialize Java database files.", ex);
        } catch (SecurityException ex) {
            throw new IllegalStateException("Access denied while initializing Java database files.", ex);
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0 && "--recreate".equals(args[0])) {
                recreate();
            } else {
                initialize();
            }
            System.out.println("Java database initialized at " + DATABASE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to initialize Java database: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Failed to initialize Java database: " + e.getMessage());
            System.exit(1);
        }
    }
}
