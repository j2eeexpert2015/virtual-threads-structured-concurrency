package com.example.pinning;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPinningDemo {

    enum ThreadType { PLATFORM, VIRTUAL }

    public static void main(String[] args) {
        // Enable detailed pinned thread tracing
        System.setProperty("jdk.tracePinnedThreads", "full");
        //The JVM logs the first occurrence of a pinning event per call site.
        System.out.println("=== Thread Pinning Demo ===");
        System.out.println("Available CPU Cores: " + Runtime.getRuntime().availableProcessors());

        int[] threadCounts = {2, 4, 8, 16, 32};
        for (int threadCount : threadCounts) {
            runThreads(ThreadType.PLATFORM, threadCount);
            runThreads(ThreadType.VIRTUAL, threadCount);
            System.out.println("------------------------------");
        }

        System.out.println("Demo finished. Note: Virtual threads may appear slower due to pinning (synchronized block).");
    }

    // Handles both thread types, logging, and timing
    static void runThreads(ThreadType type, int threadCount) {
        System.out.println("Testing with " + threadCount + " threads using " + type + " threads");

        long start = System.currentTimeMillis();
        ExecutorService executor = (type == ThreadType.PLATFORM)
                ? Executors.newFixedThreadPool(threadCount)
                : Executors.newVirtualThreadPerTaskExecutor();

        try (executor) {
            for (int i = 0; i < threadCount; i++) {
                final int taskId = i + 1;
                executor.submit(() -> {
                    //System.out.println("[" + type + "] Task " + taskId + " started");
                    synchronized (ThreadPinningDemo.class) { // Shared lock (causes pinning)
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}
                    }
                    //System.out.println("[" + type + "] Task " + taskId + " completed");
                });
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("%s Threads took: %d ms%n", type, elapsed);
    }
}
