package com.example.pinning;

import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Simulates a high-blocking workload using platform threads.
 * - Uses sleep to simulate blocking I/O.
 * - Fixed thread pool causes contention under heavy task load.
 */
public class HighBlockingIOWithPlatformThreadPinning {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Platform Thread Demo ===");
        System.out.println("PID: " + ProcessHandle.current().pid());
        System.out.println("Attach JFR or VisualVM. Press Enter to start...");
        new Scanner(System.in).nextLine();
        System.out.println("Proceeding...");

        int taskCount = 1000;
        int poolSize = 50;

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        Object sharedLock = new Object();
        BlockingQueue<String> taskQueue = new ArrayBlockingQueue<>(100);

        long start = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    doCpuWork(taskId, 300);
                    waitOnQueue(taskQueue, taskId);
                    synchronizedWork(sharedLock, taskId);
                    simulateBlockingIO(taskId);
                    doCpuWork(taskId, 300);
                } catch (Exception e) {
                    log(taskId, "Error: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        long end = System.currentTimeMillis();
        System.out.println("\n[Platform Threads] Total execution time: " + (end - start) + " ms");
    }

    private static void waitOnQueue(BlockingQueue<String> queue, int taskId) throws InterruptedException {
        String task = queue.poll(500, TimeUnit.MILLISECONDS);
        if (task == null) queue.offer("work-" + taskId, 100, TimeUnit.MILLISECONDS);
    }

    private static void synchronizedWork(Object lock, int taskId) {
        synchronized (lock) {
            try {
                Thread.sleep(200);
                for (int i = 0; i < 100_000; i++) Math.sqrt(i);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void simulateBlockingIO(int taskId) {
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void doCpuWork(int taskId, long durationMs) {
        long start = System.currentTimeMillis();
        double result = 0;
        while (System.currentTimeMillis() - start < durationMs) {
            for (int i = 0; i < 10000; i++) {
                result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
            }
        }
    }

    private static void log(int taskId, String message) {
        System.out.printf("[Task-%04d] %s%n", taskId, message);
    }
}
