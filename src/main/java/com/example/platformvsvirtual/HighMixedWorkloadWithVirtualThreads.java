package com.example.platformvsvirtual;

import java.util.Scanner;
import java.util.concurrent.*;

public class HighMixedWorkloadWithVirtualThreads {

    // === CONFIGURABLE CONSTANTS ===
    private static final int TASK_COUNT = 10_000;
    private static final int CPU_WORK_BEFORE_MS = 10;
    private static final int IO_DURATION_MS = 1000;
    private static final int CPU_WORK_AFTER_MS = 5;
    private static final int PLATFORM_THREAD_POOL_SIZE = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("Press Enter to start");
        new Scanner(System.in).nextLine();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        executeConcurrentWorkload(executor, TASK_COUNT);

        System.out.println("Press Enter to finish");
        new Scanner(System.in).nextLine();
    }

    private static void executeConcurrentWorkload(ExecutorService executor, int taskCount)
            throws InterruptedException {

        System.out.printf("Executing %,d mixed workload tasks...\n", taskCount);
        long startTime = System.currentTimeMillis();

        CountDownLatch completionLatch = new CountDownLatch(taskCount);
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                performCpuWork(CPU_WORK_BEFORE_MS);
                simulateIOOperation(IO_DURATION_MS);
                performCpuWork(CPU_WORK_AFTER_MS);
                completionLatch.countDown();
            });
        }

        completionLatch.await();
        executor.shutdown();

        printBenchmarkResults(taskCount, startTime);
    }

    private static void performCpuWork(long durationMs) {
        long start = System.currentTimeMillis();
        double dummy = 0;
        while (System.currentTimeMillis() - start < durationMs) {
            for (int i = 0; i < 1000; i++) {
                dummy += Math.sqrt(i) * Math.sin(i);
            }
        }
    }

    private static void simulateIOOperation(long durationMs) {
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printBenchmarkResults(int taskCount, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("\nCompleted %,d tasks in %,dms (%,.1f ops/s)\n",
                taskCount, duration, taskCount / (duration / 1000.0));
    }
}
