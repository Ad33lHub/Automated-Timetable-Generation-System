class Printer {
    private int availablePages = 10;
    private boolean jobComplete = false;

    public synchronized void loadPages(int pages) {
        System.out.println("[Loader] Loading " + pages + " pages into tray...");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        availablePages += pages;
        System.out.println("[Loader] Tray now has " + availablePages + " pages.");
        notify();
    }

    public synchronized void printPages(int requiredPages) {
        System.out.println("[Printer] Print job received for " + requiredPages + " pages.");
        System.out.println("[Printer] Currently available pages: " + availablePages);

        while (availablePages < requiredPages) {
            System.out.println("[Printer] Not enough pages! Waiting for paper load...");
            System.out.println("[Printer] Need " + (requiredPages - availablePages) + " more pages.");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[Printer] Notified! Checking pages again...");
            System.out.println("[Printer] Available pages now: " + availablePages);
        }

        System.out.println("[Printer] Printing " + requiredPages + " pages...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        availablePages -= requiredPages;
        jobComplete = true;
        System.out.println("[Printer] Print job complete! Remaining pages: " + availablePages);
    }

    public boolean isJobComplete() {
        return jobComplete;
    }
}

public class PrinterJob {
    public static void main(String[] args) {
        Printer printer = new Printer();

        Thread printThread = new Thread(() -> {
            printer.printPages(15);
        });

        Thread loaderThread = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printer.loadPages(10);
        });

        System.out.println("=== Printer Job Simulation ===");
        printThread.start();
        loaderThread.start();
    }
}
