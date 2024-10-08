package com.itextos.beacon.commonlib.utility.tp;

public class TopicExecutorPoolSingleton {


    public static TopicExecutorPoolSingleton getInstance() {
    
        return  new TopicExecutorPoolSingleton();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
        
    	Thread.ofVirtual().start(task);
    }

    
}
