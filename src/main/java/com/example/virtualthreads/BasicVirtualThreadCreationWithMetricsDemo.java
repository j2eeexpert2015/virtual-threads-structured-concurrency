package com.example.virtualthreads;

import com.example.util.PrometheusMetricsInitializer;

/**
 * This class demonstrates basic virtual and platform thread creation
 * along with JFR metrics tracking using Micrometer and Prometheus.
 */
public class BasicVirtualThreadCreationWithMetricsDemo {

    public static void main(String[] args) throws InterruptedException {

        // Initialize Prometheus and JFR metrics
        PrometheusMetricsInitializer.initialize();

        // CRITICAL: Wait for JFR stream to be fully initialized
        System.out.println("⏱️ Waiting for JFR stream to initialize...");
        Thread.sleep(2000);

        Runnable task = () -> {
            System.out.println("@@@@ Starting Task @@@@@");
            System.out.println("Running in thread: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
            System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());
            try {
                Thread.sleep(500); // Give JFR time to capture activity
            } catch (InterruptedException ignored) {}
            System.out.println("@@@@ Task Complete @@@@@");
        };

        System.out.println("🧵 Creating platform thread...");
        // Run task on a platform thread
        Thread platformThread = new Thread(task);
        platformThread.start();
        platformThread.join();

        System.out.println("🌟 Creating virtual thread...");
        // Run task on a virtual thread
        Thread virtualThread = Thread.startVirtualThread(task);
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

        // Extended delay to allow JFR RecordingStream to capture all events
        System.out.println("⏳ Waiting for JFR events to be processed...");
        Thread.sleep(5000);

        System.out.println("✅ Check metrics at: http://localhost:8081/metrics");
        System.out.println("🔍 Look for: jfr_virtual_thread_starts_total and jfr_virtual_thread_ends_total");
    }
}