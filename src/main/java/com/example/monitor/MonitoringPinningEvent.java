package com.example.monitor;

import com.example.util.ThreadUtil;


public class MonitoringPinningEvent
{
    private static final Object lock = new Object();

    public static void main(String[] args){
        // Enable detailed pinning diagnostics
        System.setProperty("jdk.tracePinnedThreads", "full");

        var thread = Thread.ofVirtual().unstarted(() -> {
            //To see the pinning event, execute this class with runMonitoringPinningEvent.sh file
            System.out.println("This virtual thread will be pinned on its carrier thread");
            synchronized (lock){
                ThreadUtil.sleepOfMillis(1);
            }
        });

        thread.start();
        ThreadUtil.join(thread);
    }
}
