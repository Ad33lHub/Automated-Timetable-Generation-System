package assignment2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Assignment 2 - Part 1: Thread Pool Implementation
 * 
 * Questions & Answers:
 * 1. How many threads are running? 
 *    - In this program, we use a fixed thread pool of 3 threads. So, 3 worker threads are running plus the main thread.
 * 
 * 2. How many tasks are running? 
 *    - Initially, 3 tasks can run concurrently (since the pool size is 3). The remaining tasks (total 5) wait in a queue.
 * 
 * 3. If more tasks are added, then what will be the impact on number of threads? 
 *    - Since we use a FixedThreadPool, the number of threads will NOT increase. New tasks will be added to the queue and executed as soon as a thread becomes free.
 * 
 * 4. Explain the flow of program:
 *    - The program creates an ExecutorService with 3 fixed threads.
 *    - It submits 5 instances of the "Task" class to the executor.
 *    - Each task prints its name and the thread executing it, then sleeps for 1 second.
 *    - The executor distributes these tasks among the 3 available threads.
 *    - After all tasks are submitted, the executor is shut down.
 */

class Task implements Runnable {
    private String name;

    public Task(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(name + " is being executed by " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000); // Simulate some work
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " finished.");
    }
}

public class Part1ThreadPool {
    public static void main(String[] args) {
        // Create a fixed thread pool of size 3
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit 5 tasks
        for (int i = 1; i <= 5; i++) {
            Runnable worker = new Task("Task " + i);
            executor.execute(worker);
        }

        // Shut down the executor
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all tasks to finish
        }
        System.out.println("Finished all threads");
    }
}
