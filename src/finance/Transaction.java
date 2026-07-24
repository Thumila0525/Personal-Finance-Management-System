/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finance.management.system.pkg0.pkg1;

/**
 *
 * @author User
 */
import java.util.Date;

/**
 * Abstract base class representing any financial transaction.
 */
public abstract class Transaction {
    protected int transactionId;
    protected double amount;
    protected Date date;

    public Transaction(int transactionId, double amount) {
        try {
            if (transactionId <= 0) {
                throw new IllegalArgumentException("Transaction ID must be greater than zero.");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("Transaction amount cannot be negative.");
            }
            this.transactionId = transactionId;
            this.amount = amount;
            this.date = new Date();
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    public int getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    /**
     * Executes the transaction. Returns true if it succeeded
     * (e.g. sufficient balance for a withdrawal), false otherwise.
     */
    public abstract boolean execute();

    @Override
    public String toString() {
        try {
            return String.format("[#%d] %s - Amount: %.2f - Date: %s",
                    transactionId, getClass().getSimpleName(), amount, date);
        } catch (Exception ex) {
            return "[Invalid transaction]";
        }
    }
}