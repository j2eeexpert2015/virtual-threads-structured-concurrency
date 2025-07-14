package com.example.virtualthreads;

import com.example.util.CommonUtil;

/**
 * This class demonstrates the most basic way to create and manage threads in Java.
 * It contrasts traditional platform threads with the new virtual threads introduced in Java 21,
 * highlighting their simplicity of creation and execution.
 */

public class BasicVirtualThreadCreationMonitoringDemo {
	public static void main(String[] args) throws InterruptedException {
		
		Runnable task = () -> {
            System.out.println("@@@@ Starting Task @@@@@");
            System.out.println("Running in thread: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
            System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());

            /*
            try {
                Thread.sleep(5000); // Keep thread alive for 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
             */
        };

        CommonUtil.waitForUserInput();
        Thread platformThread = new Thread(task);
        platformThread.start();
        platformThread.join();
        
        Thread virtualThread = Thread.startVirtualThread(task);
        virtualThread.join();

        CommonUtil.waitForUserInput();

        /*
        System.out.println("🔄 Creating multiple virtual threads for better metrics...");
        // Create multiple virtual threads to ensure metrics are captured
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            Thread vt = Thread.startVirtualThread(() -> {
                System.out.println("Virtual thread " + threadNum + " running");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Virtual thread " + threadNum + " completed");
            });
            vt.join();

            // Small delay between threads
            Thread.sleep(100);
        }
        CommonUtil.waitForUserInput();
		*/
	}
    
}