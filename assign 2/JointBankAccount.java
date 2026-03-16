class BankAccount {
    private int balance = 50000;

    public synchronized void withdraw(String user, int amount) {
        System.out.println(user + " wants to withdraw Rs." + amount);
        System.out.println("Current Balance: Rs." + balance);

        if (balance >= amount) {
            System.out.println(user + " is withdrawing Rs." + amount + "...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            balance -= amount;
            System.out.println(user + " successfully withdrew Rs." + amount);
            System.out.println("Remaining Balance: Rs." + balance);
        } else {
            System.out.println("Insufficient balance! " + user + " cannot withdraw Rs." + amount);
            System.out.println("Available Balance: Rs." + balance);
        }
        System.out.println("-----------------------------------");
    }
}

public class JointBankAccount {
    public static void main(String[] args) {
        BankAccount account = new BankAccount();

        Thread userA = new Thread(() -> {
            account.withdraw("User A", 45000);
        });

        Thread userB = new Thread(() -> {
            account.withdraw("User B", 20000);
        });

        userA.start();
        userB.start();
    }
}
