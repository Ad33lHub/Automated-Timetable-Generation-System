package assignment2;

/**
 * Assignment 2 - Part 4: Joint Bank Account
 * 
 * Requirements:
 * - Total amount: 50,000.
 * - User A wants to withdraw 45,000.
 * - User B wants to withdraw 20,000.
 * - Apply synchronization mechanism for handling multi-threaded access.
 */

class BankAccount {
    private int balance = 50000;

    // Synchronized method to ensure thread safety
    public synchronized void withdraw(String user, int amount) {
        System.out.println(user + " is trying to withdraw " + amount);
        
        if (balance >= amount) {
            System.out.println(user + " is about to withdraw...");
            try {
                // Sleep to simulate processing time and make concurrency issues more likely if not synchronized
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            balance -= amount;
            System.out.println(user + " completed withdrawal.");
            System.out.println("Remaining Balance: " + balance);
        } else {
            System.out.println("Sorry, not enough balance for " + user + ". Current balance: " + balance);
        }
    }
}

class UserThread extends Thread {
    private BankAccount account;
    private String name;
    private int amount;

    public UserThread(BankAccount account, String name, int amount) {
        this.account = account;
        this.name = name;
        this.amount = amount;
    }

    @Override
    public void run() {
        account.withdraw(name, amount);
    }
}

public class Part4JointBankAccount {
    public static void main(String[] args) {
        BankAccount account = new BankAccount();
        
        // Create two threads representing two users withdrawing from the same account
        UserThread userA = new UserThread(account, "User A", 45000);
        UserThread userB = new UserThread(account, "User B", 20000);

        userA.start();
        userB.start();
    }
}
