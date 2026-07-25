package finance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Relative path to your fintrack.db file inside the database folder
    private static final String URL = "jdbc:sqlite:database/fintrack.db";

    public static Connection getConnection() throws SQLException {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("SQLite JDBC Driver not found in library path!");
        }
    }

    // Quick main method to test if the connection works!
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Successfully connected to fintrack.db!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }
}