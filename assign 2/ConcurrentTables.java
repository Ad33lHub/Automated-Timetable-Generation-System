class RollNumberTable extends Thread {
    @Override
    public void run() {
        int rollNumber = 92;
        System.out.println("--- Roll Number Table ---");
        for (int i = 1; i <= 10; i++) {
            System.out.println("2020-SE-0" + rollNumber + " x " + i + " = " + (rollNumber * i));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class DOBTable extends Thread {
    @Override
    public void run() {
        int dob = 5; // 05-April
        System.out.println("--- Date of Birth Table (05) ---");
        for (int i = 1; i <= 10; i++) {
            System.out.println("05-April x " + i + " = " + (dob * i));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class ConcurrentTables {
    public static void main(String[] args) {
        RollNumberTable t1 = new RollNumberTable();
        DOBTable t2 = new DOBTable();
        t1.start();
        t2.start();
    }
}
