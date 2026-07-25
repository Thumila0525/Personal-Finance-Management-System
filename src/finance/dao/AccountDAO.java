package finance.dao;

import finance.Account;
import finance.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

    // Adds a new account to the database
    public boolean addAccount(int userId, String name, double initialBalance, String accountType) {
        String sql = "INSERT INTO accounts (user_id, name, balance, account_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            pstmt.setDouble(3, initialBalance);
            pstmt.setString(4, accountType);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding account: " + e.getMessage());
            return false;
        }
    }

    // Updates the balance of an account
    public boolean updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
            return false;
        }
    }

    // Retrieves all accounts for a specific user using a fixed-size array
    public Account[] getAccountsByUserId(int userId) {
        // Using the max capacity limit defined in your User class
        Account[] accounts = new Account[10];
        int count = 0;

        String sql = "SELECT * FROM accounts WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next() && count < accounts.length) {
                    int accountId = rs.getInt("account_id");
                    String name = rs.getString("name");
                    double balance = rs.getDouble("balance");
                    String type = rs.getString("account_type");

                    // Note: Since Account is abstract, you will need to instantiate
                    // the specific subclass (e.g., BankAccount, VirtualAccount) based on 'type'
                    // accounts[count] = new BankAccount(accountId, name, balance);

                    count++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving accounts: " + e.getMessage());
        }

        // Return a perfectly sized array to avoid null elements
        return trimAccountArray(accounts, count);
    }

    // Helper method to remove null trailing slots
    private Account[] trimAccountArray(Account[] original, int count) {
        Account[] trimmed = new Account[count];
        for (int i = 0; i < count; i++) {
            trimmed[i] = original[i];
        }
        return trimmed;
    }
}