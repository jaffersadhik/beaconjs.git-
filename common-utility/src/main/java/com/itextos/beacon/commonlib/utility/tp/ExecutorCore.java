package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorCore {

	
    // Public method to get the singleton instance
    public static  ExecutorCore getInstance() {
       
        return new ExecutorCore();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
       Thread.ofVirtual().start(task);
    }

    
}
