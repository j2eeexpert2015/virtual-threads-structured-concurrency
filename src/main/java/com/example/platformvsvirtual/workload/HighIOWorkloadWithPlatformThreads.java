package com.example.platformvsvirtual.workload;

import java.util.Scanner;
import java.util.concurrent.*;

public class HighIOWorkloadWithPlatformThreads {

    public static void main(String[] args) throws Exception {
        System.out.println("Press Enter to start");
        new Scanner(System.in).nextLine(); // Wait for enter

        ExecutorService executor = Executors.newFixedThreadPool(100);
        executeConcurrentWorkload(executor, 10_000);
        System.out.println("Press Enter to finish");
        new Scanner(System.in).nextLine();
    }

    private static void executeConcurrentWorkload(ExecutorService executor, int taskCount)
            throws InterruptedException {

        System.out.printf("Executing %,d I/O tasks...\n", taskCount);
        long startTime = System.currentTimeMillis();

        CountDownLatch completionLatch = new CountDownLatch(taskCount);
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                simulateIOOperation();
                completionLatch.countDown();
            });
        }

        completionLatch.await();
        executor.shutdown();

        printBenchmarkResults(taskCount, startTime);
    }

    private static void simulateIOOperation() {
        try {
            Thread.sleep(100); // Simulate I/O
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printBenchmarkResults(int taskCount, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("\nCompleted %,d tasks in %,dms (%,.1f ops/s)\n",
                taskCount, duration, taskCount/(duration/1000.0));
    }
}