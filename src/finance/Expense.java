/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finance.management.system.pkg0.pkg1;

/**
 *
 * @author User
 */
/**
 * Represents money withdrawn from an account.
 */
public class Expense extends Transaction {
    private Account account;
    private ExpenseCategory category;

    public Expense(int transactionId, double amount, Account account, ExpenseCategory category) {
        super(transactionId, amount);
        try {
            if (account == null) {
                throw new IllegalArgumentException("Expense account cannot be null.");
            }
            if (category == null) {
                throw new IllegalArgumentException("Expense category cannot be null.");
            }
            this.account = account;
            this.category = category;
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    @Override
    public boolean execute() {
        try {
            return account.withdraw(amount);
        } catch (IllegalArgumentException ex) {
            System.err.println("Expense execution failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        try {
            return super.toString() + " [" + category + "] -> withdrawn from Account #" + account.getAccountId();
        } catch (Exception ex) {
            return super.toString() + " [invalid expense]";
        }
    }
}