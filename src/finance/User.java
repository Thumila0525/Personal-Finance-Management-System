/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finance;

/**
 * Represents a user of the finance management system.
 * Uses fixed-size arrays (no ArrayList) to store accounts and transactions,
 * so a maximum capacity must be set up front.
 */
public class User {
    private static final int MAX_ACCOUNTS = 10;
    private static final int MAX_TRANSACTIONS = 100;

    private int id;
    private String username;
    private String email;
    private String password;

    private Account[] accounts;
    private int accountCount;

    private Transaction[] transactions;
    private int transactionCount;

    private int nextTransactionId;

    // Default constructor added for DAO/Servlet instantiation
    public User() {
        this.accounts = new Account[MAX_ACCOUNTS];
        this.accountCount = 0;
        this.transactions = new Transaction[MAX_TRANSACTIONS];
        this.transactionCount = 0;
        this.nextTransactionId = 1;
    }

    public User(String username, String email, String password) {
        try {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("User username cannot be empty.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("User email cannot be empty.");
            }
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("User password cannot be empty.");
            }
            this.username = username.trim();
            this.email = email.trim();
            this.password = password;
            this.accounts = new Account[MAX_ACCOUNTS];
            this.accountCount = 0;
            this.transactions = new Transaction[MAX_TRANSACTIONS];
            this.transactionCount = 0;
            this.nextTransactionId = 1;
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- Core Methods ---

    public Account addAccount(Account account) {
        try {
            if (account == null) {
                throw new IllegalArgumentException("Account cannot be null.");
            }
            if (accountCount >= accounts.length) {
                throw new IllegalArgumentException("Cannot add account: storage full.");
            }
            if (getAccountById(account.getAccountId()) != null) {
                throw new IllegalArgumentException("Account ID " + account.getAccountId() + " is already in use.");
            }
            accounts[accountCount++] = account;
            return account;
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to add account: " + ex.getMessage());
            return null;
        }
    }

    private boolean storeTransaction(Transaction t) {
        try {
            if (t == null) {
                throw new IllegalArgumentException("Transaction cannot be null.");
            }
            if (transactionCount >= transactions.length) {
                throw new IllegalArgumentException("Cannot record transaction: storage full.");
            }
            transactions[transactionCount++] = t;
            return true;
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to record transaction: " + ex.getMessage());
            return false;
        }
    }

    public boolean addIncome(Account account, double amount, IncomeCategory category) {
        try {
            Income income = new Income(nextTransactionId, amount, account, category);
            boolean success = income.execute();
            if (success) {
                nextTransactionId++;
                return storeTransaction(income);
            }
            return false;
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to add income: " + ex.getMessage());
            return false;
        }
    }

    public boolean addExpense(Account account, double amount, ExpenseCategory category) {
        try {
            Expense expense = new Expense(nextTransactionId, amount, account, category);
            boolean success = expense.execute();
            if (success) {
                nextTransactionId++;
                return storeTransaction(expense);
            }
            System.out.println("Expense failed: insufficient balance in Account #" + account.getAccountId());
            return false;
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to add expense: " + ex.getMessage());
            return false;
        }
    }

    public boolean addTransfer(Account fromAccount, Account toAccount, double amount) {
        try {
            Transfer transfer = new Transfer(nextTransactionId, amount, fromAccount, toAccount);
            boolean success = transfer.execute();
            if (success) {
                nextTransactionId++;
                return storeTransaction(transfer);
            }
            System.out.println("Transfer failed: insufficient balance in Account #" + fromAccount.getAccountId());
            return false;
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to add transfer: " + ex.getMessage());
            return false;
        }
    }

    public Account getAccountById(int accountId) {
        try {
            for (int i = 0; i < accountCount; i++) {
                if (accounts[i].getAccountId() == accountId) {
                    return accounts[i];
                }
            }
            return null;
        } catch (Exception ex) {
            System.err.println("Unable to search accounts: " + ex.getMessage());
            return null;
        }
    }

    public int getAccountCount() {
        return accountCount;
    }

    public void printAccounts() {
        try {
            System.out.println("Accounts for " + username + ":");
            for (int i = 0; i < accountCount; i++) {
                System.out.println("  " + accounts[i]);
            }
        } catch (Exception ex) {
            System.err.println("Unable to print accounts: " + ex.getMessage());
        }
    }

    public void printTransactions() {
        try {
            System.out.println("Transaction history for " + username + ":");
            for (int i = 0; i < transactionCount; i++) {
                System.out.println("  " + transactions[i]);
            }
        } catch (Exception ex) {
            System.err.println("Unable to print transactions: " + ex.getMessage());
        }
    }
}