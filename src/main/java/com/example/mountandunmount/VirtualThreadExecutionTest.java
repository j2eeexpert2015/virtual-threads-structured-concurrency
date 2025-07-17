package com.example.mountandunmount;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.valueOf;

/**
 * Demonstrates how virtual threads behave under different workloads
 * such as I/O, CPU-bound, and synchronized blocks.
 * Profiles thread execution and detects thread hops.
 */
public class VirtualThreadExecutionTest {

    private static final int NUM_TASKS = 50;
    private static final VirtualThreadProfiler profiler = new VirtualThreadProfiler(true);

    public static void main(String[] args) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int taskId = 0; taskId < NUM_TASKS; taskId++) {
                Runnable workload = () -> {
                    // Choose the workload type to test:
                     simulateBlockingIO();
                    // simulateCPULoad();
                    //simulateSynchronizedBlock();
                };

                executor.submit(profiler.profile(taskId, workload));
            }

            System.out.println("🔄 Waiting for virtual thread tasks to complete...");
        }

        profiler.displayReport();
    }

    // === Simulates a blocking I/O-like operation ===
    private static void simulateBlockingIO() {
        try {
            Thread.sleep(500); // Simulate external call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // === Simulates a CPU-intensive calculation ===
    private static void simulateCPULoad() {
        BigInteger result = ZERO;
        for (int i = 0; i < 10_000_000; i++) {
            result = result.add(valueOf(i).sqrt());
        }
        long sink = result.longValue(); // Prevent optimization
    }

    // === Simulates contention using a synchronized block ===
    private static int sharedState = 0;

    private static synchronized void simulateSynchronizedBlock() {
        sharedState++;
        try {
            Thread.sleep(100); // Simulated delay inside critical section
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

