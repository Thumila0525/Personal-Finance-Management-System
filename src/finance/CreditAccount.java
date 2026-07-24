/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finance;

/**
 *
 * @author User
 */
public class CreditAccount extends Account {
    private double creditLimit;

    public CreditAccount(int accountId, String name, double balance, double creditLimit) {
        super(accountId, name, balance);
        this.creditLimit = creditLimit;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public double getAvailableCredit() {
        return creditLimit + balance;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount > 0 && (balance - amount) >= -creditLimit) {
            balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Credit Account #" + accountId + " (" + name + ") | Balance: " + String.format("%.2f", balance)
                + " | Credit limit: " + String.format("%.2f", creditLimit)
                + " | Available credit: " + String.format("%.2f", getAvailableCredit());
    }
}