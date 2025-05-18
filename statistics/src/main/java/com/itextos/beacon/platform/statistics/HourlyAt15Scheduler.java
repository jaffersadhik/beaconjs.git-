package com.itextos.beacon.platform.statistics;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HourlyAt15Scheduler {

    public static void start() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task running at: " + new Date());
                
          //      HourlyInsert.doProcess();
                // Your task logic here
            }
        };

        long delay = getInitialDelayToNext15thMinute();
        long period = 60 * 60 * 1000L; // 1 hour in milliseconds

        timer.scheduleAtFixedRate(task, delay, period);
    }

    // Calculate delay until the next hh:15:00
    private static long getInitialDelayToNext15thMinute() {
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        int currentMinute = nextRun.get(Calendar.MINUTE);

        if (currentMinute >= 15) {
            nextRun.add(Calendar.HOUR_OF_DAY, 1);
        }
        nextRun.set(Calendar.MINUTE, 15);

        return nextRun.getTimeInMillis() - System.currentTimeMillis();
    }
}

