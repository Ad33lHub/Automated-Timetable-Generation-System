public class AlphabetPrinter extends Thread {
    private volatile boolean running = true;

    @Override
    public void run() {
        for (int i = 0; i < 26 && running; i++) {
            int randomNum = (int) (Math.random() * 26);
            char ch = (char) ('A' + randomNum);
            System.out.println("Random Character: " + ch + " (ASCII: " + (int) ch + ")");
            try {
                int sleepTime = (int) (Math.random() * 1000) + 200;
                System.out.println("  Sleeping for " + sleepTime + " ms...");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted!");
            }
        }
        System.out.println("Thread finished printing 26 characters.");
    }

    public void stopThread() {
        running = false;
    }

    public static void main(String[] args) throws InterruptedException {
        AlphabetPrinter printer = new AlphabetPrinter();
        System.out.println("Starting Alphabet Printer Thread...");
        printer.start();
        printer.join();
        System.out.println("Main thread finished.");
    }
}
