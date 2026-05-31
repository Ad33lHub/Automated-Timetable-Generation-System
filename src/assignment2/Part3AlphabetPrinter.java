package assignment2;

import java.util.Random;

/**
 * Assignment 2 - Part 3: Alphabet Printer
 * 
 * Requirements:
 * - Print alphabets from A-Z using start and sleep methods. (stop is deprecated).
 * - Use Math.random() for getting random numbers (65 to 90 for A-Z).
 * - Print 26 characters in the run method loop with fluctuating sleep visualization.
 */

class AlphabetThread extends Thread {
    private boolean running = true;

    // Custom stop method since Thread.stop() is deprecated
    public void stopPrinting() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("Starting Alphabet Printer (Random A-Z):");
        Random random = new Random();

        for (int i = 0; i < 26; i++) {
            if (!running) break;

            // Math.random() approach for characters A (65) to Z (90)
            int charCode = (int) (Math.random() * 26) + 65;
            char letter = (char) charCode;

            System.out.print(letter + " ");

            try {
                // Fluctuating visualization through sleep (100ms to 500ms)
                long sleepTime = (long) (Math.random() * 400) + 100;
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("\nThread interrupted.");
                return;
            }
        }
        System.out.println("\nFinished printing 26 characters.");
    }
}

public class Part3AlphabetPrinter {
    public static void main(String[] args) {
        AlphabetThread thread = new AlphabetThread();

        // start method
        thread.start();

        // Wait for it to finish or demonstrate stopping if needed
        try {
            thread.join(10000); // Wait up to 10 seconds for it to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (thread.isAlive()) {
            System.out.println("Stopping thread prematurely...");
            thread.stopPrinting();
        }
    }
}
