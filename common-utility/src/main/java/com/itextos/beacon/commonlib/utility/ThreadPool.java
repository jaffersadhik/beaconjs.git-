package com.itextos.beacon.commonlib.utility;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadPool {
    private final int nThreads;
    private final PoolWorker[] threads;
    private final Queue<Runnable> taskQueue;

    // Constructor to initialize the ThreadPool with a fixed number of threads
    public ThreadPool(int nThreads) {
        this.nThreads = nThreads;
        taskQueue = new LinkedList<>();
        threads = new PoolWorker[nThreads];

        // Initialize and start all worker threads
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public int getQueueSize() {
    	
    	return taskQueue.size();
    }
    // Method to submit a task to the ThreadPool
    public void submitTask(Runnable task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            // Notify a worker thread that a task is available
            taskQueue.notify();
        }
    }

    // Method to shut down the ThreadPool
    public void shutdown() {
        for (int i = 0; i < nThreads; i++) {
            threads[i].interrupt();
        }
    }

    // Inner class representing a worker thread
    private class PoolWorker extends Thread {
        @Override
        public void run() {
            Runnable task;

            while (true) {
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty()) {
                        try {
                            // Wait for a task to be available
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            // If interrupted, exit the loop and stop the thread
                            return;
                        }
                    }
                    // Get the next task from the queue
                    task = taskQueue.poll();
                }

                // Execute the task
                try {
                    task.run();
                } catch (RuntimeException e) {
                    // Log or handle exceptions to prevent worker thread termination
                    System.err.println("ThreadPool worker encountered an error: " + e.getMessage());
                }
            }
        }
    }

}
