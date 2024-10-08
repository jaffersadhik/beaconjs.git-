package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorTable2DB {


    // Public method to get the singleton instance
    public static  ExecutorTable2DB getInstance() {
  
        return new ExecutorTable2DB();;
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
        Thread.ofVirtual().start(task);
    }

  
}
