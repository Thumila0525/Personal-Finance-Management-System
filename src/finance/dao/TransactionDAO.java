package finance.dao;

import finance.DatabaseConnection;
import finance.ExpenseCategory;
import finance.IncomeCategory;
import finance.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class TransactionDAO {

    public boolean addIncome(int accountId, double amount, IncomeCategory category) {
        String sql = "INSERT INTO transactions (account_id, type, amount, category, date) VALUES (?, 'INCOME', ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, category.name());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding income record: " + e.getMessage());
            return false;
        }
    }

    public boolean addExpense(int accountId, double amount, ExpenseCategory category) {
        String sql = "INSERT INTO transactions (account_id, type, amount, category, date) VALUES (?, 'EXPENSE', ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, category.name());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding expense record: " + e.getMessage());
            return false;
        }
    }

    public boolean addTransfer(int fromAccountId, int toAccountId, double amount) {
        String sqlOut = "INSERT INTO transactions (account_id, type, amount, related_account_id, date) VALUES (?, 'TRANSFER_OUT', ?, ?, CURRENT_TIMESTAMP)";
        String sqlIn = "INSERT INTO transactions (account_id, type, amount, related_account_id, date) VALUES (?, 'TRANSFER_IN', ?, ?, CURRENT_TIMESTAMP)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtOut = conn.prepareStatement(sqlOut)) {
                pstmtOut.setInt(1, fromAccountId);
                pstmtOut.setDouble(2, amount);
                pstmtOut.setInt(3, toAccountId);
                pstmtOut.executeUpdate();
            }

            try (PreparedStatement pstmtIn = conn.prepareStatement(sqlIn)) {
                pstmtIn.setInt(1, toAccountId);
                pstmtIn.setDouble(2, amount);
                pstmtIn.setInt(3, fromAccountId);
                pstmtIn.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transfer: " + ex.getMessage());
                }
            }
            System.err.println("Error adding transfer record: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // Retrieves transaction history for an account using fixed-size arrays
    public Transaction[] getTransactionsByAccountId(int accountId) {
        // Using the max capacity limit defined in your User class
        Transaction[] transactions = new Transaction[100];
        int count = 0;

        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next() && count < transactions.length) {
                    int id = rs.getInt("transaction_id");
                    double amount = rs.getDouble("amount");
                    String type = rs.getString("type");
                    String category = rs.getString("category");

                    // Note: Depending on 'type' ('INCOME', 'EXPENSE', 'TRANSFER'),
                    // you will instantiate the specific subclass (Income, Expense, Transfer) here
                    // transactions[count] = new Income(id, amount, ...);

                    count++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }

        return trimTransactionArray(transactions, count);
    }

    // Helper method to remove null trailing slots
    private Transaction[] trimTransactionArray(Transaction[] original, int count) {
        Transaction[] trimmed = new Transaction[count];
        for (int i = 0; i < count; i++) {
            trimmed[i] = original[i];
        }
        return trimmed;
    }
}