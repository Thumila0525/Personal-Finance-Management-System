/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package finance;

/**
 *
 * @author User
 */
import java.util.Scanner;

public class FinanceManagementSystem01 {

    /**
     * @param args the command line arguments
     */
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter your email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();
            User user = new User(name, email, password);

            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Choose an option: ");

                switch (choice) {
                    case 1:
                        createAccount(user);
                        break;
                    case 2:
                        recordIncome(user);
                        break;
                    case 3:
                        recordExpense(user);
                        break;
                    case 4:
                        recordTransfer(user);
                        break;
                    case 5:
                        user.printAccounts();
                        break;
                    case 6:
                        user.printTransactions();
                        break;
                    case 7:
                        running = false;
                        System.out.println("Goodbye, " + user.getName() + "!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void printMenu() {
        System.out.println("\n===== Finance Management System =====");
        System.out.println("1. Create Account");
        System.out.println("2. Add Income");
        System.out.println("3. Add Expense");
        System.out.println("4. Add Transfer");
        System.out.println("5. View Accounts");
        System.out.println("6. View Transaction History");
        System.out.println("7. Exit");
    }

    private static void createAccount(User user) {
        try {
            int id = readInt("Enter a new account ID (choose a number you'll remember): ");
            if (user.getAccountById(id) != null) {
                System.out.println("That account ID is already taken. Please try again with a different ID.");
                return;
            }
            System.out.print("Enter a name for this account (e.g. Checking, Savings): ");
            String accountName = scanner.nextLine().trim();

            System.out.println("Select account type:");
            System.out.println("  1. Bank Account");
            System.out.println("  2. Credit Account");
            int type = readInt("Choose an option: ");

            double balance = readDouble("Enter initial balance: ");

            Account account;
            if (type == 2) {
                double creditLimit = readDouble("Enter credit limit: ");
                account = new CreditAccount(id, accountName, balance, creditLimit);
            } else {
                account = new BankAccount(id, accountName, balance);
            }

            if (user.addAccount(account) != null) {
                System.out.println("Created " + account);
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Couldn't create account: " + ex.getMessage());
        }
    }

    private static void recordIncome(User user) {
        if (user.getAccountCount() == 0) {
            System.out.println("No accounts exist yet. Please create one first.");
            return;
        }
        Account account = selectAccount(user, "Enter account ID to deposit into: ");
        if (account == null) return;

        double amount = readDouble("Enter income amount: ");
        IncomeCategory category = selectEnum(IncomeCategory.class, "Select income category:");
        if (user.addIncome(account, amount, category)) {
            System.out.println("Income recorded successfully.");
        } else {
            System.out.println("Failed to record income.");
        }
    }

    private static void recordExpense(User user) {
        if (user.getAccountCount() == 0) {
            System.out.println("No accounts exist yet. Please create one first.");
            return;
        }
        Account account = selectAccount(user, "Enter account ID to withdraw from: ");
        if (account == null) return;

        double amount = readDouble("Enter expense amount: ");
        ExpenseCategory category = selectEnum(ExpenseCategory.class, "Select expense category:");
        user.addExpense(account, amount, category);
    }

    private static void recordTransfer(User user) {
        if (user.getAccountCount() < 2) {
            System.out.println("You need at least 2 accounts to make a transfer.");
            return;
        }
        Account from = selectAccount(user, "Enter FROM account ID: ");
        if (from == null) return;

        Account to = selectAccount(user, "Enter TO account ID: ");
        if (to == null) return;

        if (from.getAccountId() == to.getAccountId()) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        double amount = readDouble("Enter transfer amount: ");
        user.addTransfer(from, to, amount);
    }

    private static Account selectAccount(User user, String prompt) {
        try {
            int id = readInt(prompt);
            Account account = user.getAccountById(id);
            if (account == null) {
                System.out.println("No account found with ID " + id);
            }
            return account;
        } catch (IllegalArgumentException ex) {
            System.err.println("Account selection failed: " + ex.getMessage());
            return null;
        }
    }

    private static <T extends Enum<T>> T selectEnum(Class<T> enumClass, String title) {
        T[] values = enumClass.getEnumConstants();
        System.out.println(title);
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ". " + values[i]);
        }
        while (true) {
            int choice = readInt("Choose an option: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid option. Please try again.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value < 0) {
                    System.out.println("Amount cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}