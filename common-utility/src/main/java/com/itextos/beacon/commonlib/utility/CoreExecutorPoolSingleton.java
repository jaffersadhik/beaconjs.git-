package com.itextos.beacon.commonlib.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoreExecutorPoolSingleton {
    private static final int CORE_POOL_SIZE = 5; // Maximum core size of the thread pool
    private static CoreExecutorPoolSingleton instance;
    private final ExecutorService threadPool;

    // Private constructor for Singleton
    private CoreExecutorPoolSingleton() {
        threadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    }

    // Public method to get the singleton instance
    public static synchronized CoreExecutorPoolSingleton getInstance() {
        if (instance == null) {
            instance = new CoreExecutorPoolSingleton();
        }
        return instance;
    }

    // Method to submit tasks to the thread pool
    public void submitTask(Runnable task, String taskName) {
        threadPool.submit(() -> {
            Thread currentThread = Thread.currentThread();
            String originalThreadName = currentThread.getName();
            try {
                // Set custom thread name for this task
                currentThread.setName(taskName);
                task.run();
            } finally {
                // Restore the original thread name after execution
                currentThread.setName(originalThreadName);
            }
        });
    }

    // Method to shut down the thread pool
    public void shutdown() {
        threadPool.shutdown();
    }

}

