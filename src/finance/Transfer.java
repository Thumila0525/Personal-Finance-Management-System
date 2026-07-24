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
 * Represents money moved from one account to another.
 */
public class Transfer extends Transaction {
    private Account fromAccount;
    private Account toAccount;

    public Transfer(int transactionId, double amount, Account fromAccount, Account toAccount) {
        super(transactionId, amount);
        try {
            if (fromAccount == null || toAccount == null) {
                throw new IllegalArgumentException("Transfer accounts cannot be null.");
            }
            if (fromAccount.getAccountId() == toAccount.getAccountId()) {
                throw new IllegalArgumentException("Transfer accounts must be different.");
            }
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    public int getFromAccountId() {
        return fromAccount.getAccountId();
    }

    public int getToAccountId() {
        return toAccount.getAccountId();
    }

    @Override
    public boolean execute() {
        try {
            if (fromAccount.withdraw(amount)) {
                toAccount.deposit(amount);
                return true;
            }
            return false;
        } catch (IllegalArgumentException ex) {
            System.err.println("Transfer execution failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        try {
            return super.toString() + " -> from Account #" + fromAccount.getAccountId()
                    + " to Account #" + toAccount.getAccountId();
        } catch (Exception ex) {
            return super.toString() + " -> transfer could not be described";
        }
    }
}