public class DeadlockDemo {
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();
    private static final Object lock3 = new Object();

    public static void main(String[] args) {
        System.out.println("=== Deadlock Demo with 3 Locks (Solved) ===\n");

        // SOLUTION: All threads acquire locks in the SAME ORDER (lock1 -> lock2 -> lock3)
        // This prevents circular wait condition and avoids deadlock.

        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-1: Acquired Lock 1");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    System.out.println("Thread-1: Acquired Lock 2");
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    synchronized (lock3) {
                        System.out.println("Thread-1: Acquired Lock 3");
                        System.out.println("Thread-1: Completed work with all 3 locks!\n");
                    }
                }
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-2: Acquired Lock 1");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    System.out.println("Thread-2: Acquired Lock 2");
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    synchronized (lock3) {
                        System.out.println("Thread-2: Acquired Lock 3");
                        System.out.println("Thread-2: Completed work with all 3 locks!\n");
                    }
                }
            }
        }, "Thread-2");

        Thread thread3 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-3: Acquired Lock 1");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    System.out.println("Thread-3: Acquired Lock 2");
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    synchronized (lock3) {
                        System.out.println("Thread-3: Acquired Lock 3");
                        System.out.println("Thread-3: Completed work with all 3 locks!\n");
                    }
                }
            }
        }, "Thread-3");

        thread1.start();
        thread2.start();
        thread3.start();
    }
}
