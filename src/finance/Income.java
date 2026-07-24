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
 * Represents money deposited into an account.
 */
public class Income extends Transaction {
    private Account account;
    private IncomeCategory category;

    public Income(int transactionId, double amount, Account account, IncomeCategory category) {
        super(transactionId, amount);
        try {
            if (account == null) {
                throw new IllegalArgumentException("Income account cannot be null.");
            }
            if (category == null) {
                throw new IllegalArgumentException("Income category cannot be null.");
            }
            this.account = account;
            this.category = category;
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    public IncomeCategory getCategory() {
        return category;
    }

    @Override
    public boolean execute() {
        try {
            account.deposit(amount);
            return true;
        } catch (IllegalArgumentException ex) {
            System.err.println("Income execution failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        try {
            return super.toString() + " [" + category + "] -> deposited to Account #" + account.getAccountId();
        } catch (Exception ex) {
            return super.toString() + " [invalid income]";
        }
    }
}