package com.itextos.beacon.commonlib.commondbpool;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectionRelease {

 
    public static void release() {

      
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new ReleaseConnectionTask(), 0, 60000); // Release connections every 60 seconds

}
    }

  

     class ReleaseConnectionTask extends TimerTask {

        public ReleaseConnectionTask() {
           
        }

        @Override
        public void run() {
            
        	DataSourceCollection.getInstance().evicAllConnectionPool();
        }
    }


