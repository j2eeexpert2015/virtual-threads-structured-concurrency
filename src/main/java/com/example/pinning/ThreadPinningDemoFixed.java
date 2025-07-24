package com.example.pinning;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPinningDemoFixed {

    private static final ReentrantLock lock = new ReentrantLock();
    enum ThreadType { PLATFORM, VIRTUAL }

    public static void main(String[] args) {
        // Enable detailed pinned thread tracing
        System.setProperty("jdk.tracePinnedThreads", "full");
        //The JVM logs the first occurrence of a pinning event per call site.
        System.out.println("=== Thread Pinning Demo (Fixed: No Pinning) ===");
        System.out.println("Available CPU Cores: " + Runtime.getRuntime().availableProcessors());

        int[] threadCounts = {2, 4, 8, 16, 32};
        for (int threadCount : threadCounts) {
            runThreads(ThreadType.PLATFORM, threadCount);
            runThreads(ThreadType.VIRTUAL, threadCount);
            System.out.println("------------------------------");
        }
        System.out.println("Demo finished.");
    }

    // Handles both thread types with ReentrantLock (Loom-friendly)
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
                    lock.lock();
                    try {
                        Thread.sleep(1000); // Virtual threads can unmount here
                    } catch (InterruptedException ignored) {}
                    finally {
                        lock.unlock();
                    }
                });
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("%s Threads took: %d ms%n", type, elapsed);
    }
}
