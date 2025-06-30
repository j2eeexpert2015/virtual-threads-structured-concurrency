package com.example.platformvsvirtual.workload;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.example.util.CommonUtil.waitForUserInput;

public class PlatformThreadvsVirtualThread {

    private static final int NUMBER_OF_TASKS = 1000; // Total number of tasks to execute

    public static void main(String[] args) {
        // Enable detailed pinning diagnostics
        // System.setProperty("jdk.tracePinnedThreads", "full");
        waitForUserInput();
        comparePlatformThreads();
        waitForUserInput();
        compareVirtualThreads();
        waitForUserInput();
    }

    private static void comparePlatformThreads() {
        ExecutorService executor = Executors.newFixedThreadPool(10); // Traditional thread pool

        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            executor.submit(PlatformThreadvsVirtualThread::performCommonOperation);
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.println("Traditional Threads: Execution Time = " + (endTime - startTime) + " ms");
        System.out.println("Memory Usage = " + (endMemory - startMemory) + " bytes");
    }

    private static void compareVirtualThreads() {
        ThreadFactory namedVirtualThreadFactory = Thread.ofVirtual()
                .name("virtual-thread-", 0)
                .factory();

        ExecutorService executor = Executors.newThreadPerTaskExecutor(namedVirtualThreadFactory);

        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            executor.submit(PlatformThreadvsVirtualThread::performCommonOperation);
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.println("Virtual Threads: Execution Time = " + (endTime - startTime) + " ms");
        System.out.println("Memory Usage = " + (endMemory - startMemory) + " bytes");
    }

    private static void simulateIOOperation() {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(100); // Simulate I/O
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long end = System.currentTimeMillis();
    }

    private static void simulateCPUOperation() {
        long start = System.currentTimeMillis();
        long result = 0;
        for (int i = 0; i < 1_000_000_000; i++) {
            result += Math.sqrt(i);
        }
        long end = System.currentTimeMillis();
    }

    private static void simulateHeavyCPUOperation() {
        long start = System.currentTimeMillis();
        long result = 0;
        for (int i = 0; i < 5_000_000; i++) {
            result += Math.sqrt(i) * Math.cbrt(i) / Math.log(i + 1);
        }
        long end = System.currentTimeMillis();
    }

    private static void performCommonOperation() {
        logThreadInfo();
        simulateIOOperation();
    }

    private static void logThreadInfo() {
        Thread thread = Thread.currentThread();
        System.out.printf(
                "Thread Name: %-20s | Virtual: %-5s | Daemon: %-5s\n",
                thread.getName(),
                thread.isVirtual(),
                thread.isDaemon()
        );
    }
}
