package assignment2;

/**
 * Assignment 2 - Part 2: Concurrent Tables
 * 
 * Requirements:
 * - Print two tables concurrently using threads.
 * - Table 1: Roll number (e.g., 2020-SE-092 -> using 92)
 * - Table 2: Date of birth (e.g., 05-April -> using 5)
 */

class TablePrinter extends Thread {
    private int number;
    private String label;

    public TablePrinter(int number, String label) {
        this.number = number;
        this.label = label;
    }

    @Override
    public void run() {
        System.out.println("Printing table for " + label + " (" + number + "):");
        for (int i = 1; i <= 10; i++) {
            System.out.println(label + ": " + number + " x " + i + " = " + (number * i));
            try {
                // Sleep briefly to ensure concurrency is visible
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Part2ConcurrentTables {
    public static void main(String[] args) {
        // Table 1: Roll Number (2020-SE-092 -> 92)
        TablePrinter rollTable = new TablePrinter(92, "Roll No (092)");
        
        // Table 2: DOB (05-April -> 5)
        TablePrinter dobTable = new TablePrinter(5, "DOB (05)");

        // Start both threads concurrently
        rollTable.start();
        dobTable.start();
    }
}
