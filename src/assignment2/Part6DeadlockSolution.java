package assignment2;

/**
 * Assignment 2 - Part 6: Deadlock Demo & Solution
 * 
 * Requirements:
 * - Three threads, 3 locks.
 * - Demonstrate deadlock using un-sequenced locks.
 * - Solve it through sequenced locking.
 */

public class Part6DeadlockSolution {

    // Lock placeholders
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private final Object lock3 = new Object();

    // DEADLOCK SCENARIO (Un-sequenced locks)
    public void deadlockApproach() {
        System.out.println("--- Attempting Deadlock Scenario ---");
        
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Holding lock 1...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                System.out.println("Thread 1: Waiting for lock 2...");
                synchronized (lock2) {
                    System.out.println("Thread 1: Acquired lock 2.");
                }
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2: Holding lock 2...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                System.out.println("Thread 2: Waiting for lock 3...");
                synchronized (lock3) {
                    System.out.println("Thread 2: Acquired lock 3.");
                }
            }
        }, "Thread-2");

        Thread t3 = new Thread(() -> {
            synchronized (lock3) {
                System.out.println("Thread 3: Holding lock 3...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                System.out.println("Thread 3: Waiting for lock 1...");
                synchronized (lock1) {
                    System.out.println("Thread 3: Acquired lock 1.");
                }
            }
        }, "Thread-3");

        t1.start();
        t2.start();
        t3.start();
    }

    // SOLUTION SCENARIO (Sequenced locks)
    public void solutionApproach() {
        System.out.println("\n--- Attempting Solution (Sequenced Locking) ---");
        
        // In the solution, all threads acquire locks in the SAME ORDER (lock1 -> lock2 -> lock3)
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Holding lock 1...");
                synchronized (lock2) {
                    System.out.println("Thread 1: Acquired lock 2.");
                }
            }
        }, "Fixed-Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lock1) { // Changed from lock2 to lock1 to follow sequence
                System.out.println("Thread 2: Holding lock 1...");
                synchronized (lock2) {
                    System.out.println("Thread 2: Waiting for lock 3...");
                    synchronized (lock3) {
                        System.out.println("Thread 2: Acquired lock 3.");
                    }
                }
            }
        }, "Fixed-Thread-2");

        Thread t3 = new Thread(() -> {
            synchronized (lock1) { // Changed from lock3 to lock1 to follow sequence
                System.out.println("Thread 3: Holding lock 1...");
                synchronized (lock3) {
                    System.out.println("Thread 3: Acquired lock 3.");
                }
            }
        }, "Fixed-Thread-3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Solution: All threads finished without deadlock!");
    }

    public static void main(String[] args) {
        Part6DeadlockSolution demo = new Part6DeadlockSolution();
        
        // To run deadlock (will hang):
        // demo.deadlockApproach(); 
        
        // To run solution:
        demo.solutionApproach();
    }
}
