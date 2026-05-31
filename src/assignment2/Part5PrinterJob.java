package assignment2;

/**
 * Assignment 2 - Part 5: Printer Job (Inter-thread Communication)
 * 
 * Requirements:
 * - Two threads: one for calculating/adding pages in tray, and one for printing.
 * - If total pages are 10 and job is 15 pages, the printing thread stays on wait.
 * - Notified once available pages >= printing pages.
 */

class Printer {
    private int totalPagesInTray = 10;

    public synchronized void printPages(int pagesToPrint) {
        System.out.println("Printing job started for " + pagesToPrint + " pages...");
        
        while (totalPagesInTray < pagesToPrint) {
            System.out.println("Current pages in tray: " + totalPagesInTray + ". Waiting for more pages...");
            try {
                // Wait for another thread to add pages and call notify()
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Sufficient pages available! Printing " + pagesToPrint + " pages...");
        totalPagesInTray -= pagesToPrint;
        System.out.println("Pages left in tray after job: " + totalPagesInTray);
    }

    public synchronized void addPages(int pagesToAdd) {
        System.out.println("Calculating and adding " + pagesToAdd + " more pages to the tray...");
        totalPagesInTray += pagesToAdd;
        System.out.println("New total pages in tray: " + totalPagesInTray);
        
        // Notify the waiting thread(s) that more pages are available
        notify();
    }
}

public class Part5PrinterJob {
    public static void main(String[] args) {
        Printer printer = new Printer();

        // Thread 1: Wants to print 15 pages (will have to wait)
        Thread printThread = new Thread(() -> {
            printer.printPages(15);
        });

        // Thread 2: Adds more pages after some time
        Thread calcThread = new Thread(() -> {
            try {
                // Sleep to simulate time taken to calculate/fetch more papers
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printer.addPages(10); // Adding 10 more pages (Total: 10 + 10 = 20)
        });

        printThread.start();
        calcThread.start();
    }
}
