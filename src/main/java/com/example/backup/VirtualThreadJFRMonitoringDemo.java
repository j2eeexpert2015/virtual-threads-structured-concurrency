package com.example.backup;

import com.example.util.JFRUtil;

/**
 * Demonstrates virtual thread creation and monitoring using JFR (Java Flight Recorder).
 * This class shows how to capture and analyze virtual thread lifecycle events
 * using the JFRUtil for comprehensive thread monitoring.
 */
public class VirtualThreadJFRMonitoringDemo {
    public static void main(String[] args) throws InterruptedException {

        // Start JFR recording for comprehensive thread monitoring
        System.out.println("🚀 Starting JFR recording for virtual thread monitoring...");
        JFRUtil.startVirtualThreadRecording("VirtualThreadJFRDemo");

        try {
            // Your original code with minimal changes
            Runnable task = () -> {
                System.out.println("@@@@ Starting Task @@@@@");
                System.out.println("Running in thread: " + Thread.currentThread());
                System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
                System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());

                try {
                    Thread.sleep(5000); // Keep thread alive for 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            Thread platformThread = new Thread(task);
            platformThread.start();
            platformThread.join();

            //Thread virtualThread = Thread.startVirtualThread(task);
            Thread virtualThread = Thread.ofVirtual().name("JFRDemo-Thread").start(task);
            virtualThread.join();

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

        } finally {
            // Stop recording and analyze results
            System.out.println("\n📊 Stopping JFR recording and analyzing results...");
            var jfrFile = JFRUtil.stopRecording();
            if (jfrFile != null) {
                JFRUtil.analyzeRecording(jfrFile);
            }
        }
    }
}