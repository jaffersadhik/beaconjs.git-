package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorTablePoller {

    public static ExecutorTablePoller getInstance() {
     
        return new ExecutorTablePoller();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
        Thread.ofVirtual().start(task);
    }

  

}
