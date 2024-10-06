package com.itextos.beacon.commonlib.utility.tp;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KafkaProducerExecutorPoolSingleton {

	private static KafkaProducerExecutorPoolSingleton instance;
    private final ScheduledExecutorService scheduler;
    private final List<Runnable> tasks;
    private int currentTaskIndex = 0;

    private ThreadPool threadpool=new ThreadPool(2); 
    // Private constructor for Singleton
    private KafkaProducerExecutorPoolSingleton() {
        scheduler = Executors.newScheduledThreadPool(1);
        tasks = new ArrayList<>();
        startTaskRotation() ;
    }

    // Public method to get the singleton instance
    public static synchronized KafkaProducerExecutorPoolSingleton getInstance() {
        if (instance == null) {
            instance = new KafkaProducerExecutorPoolSingleton();
        }
        return instance;
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    // Method to start executing tasks in a round-robin manner
    private void startTaskRotation() {
        scheduler.scheduleAtFixedRate(() -> {
        	 if(threadpool.getQueueSize()<100) {
            Runnable taskToRun;

            synchronized (tasks) {
                if (tasks.isEmpty()) {
                    return; // No tasks to run
                }

                // Get the current task to run in a round-robin manner
                taskToRun = tasks.get(currentTaskIndex);
                currentTaskIndex = (currentTaskIndex + 1) % tasks.size();
            }

           
            	
            threadpool.submitTask(taskToRun);
            
            }
            
        }, 0, 10L, TimeUnit.MILLISECONDS);
    }

    // Method to shut down the thread pool
    public void shutdown() {
        scheduler.shutdown();
    }

}
