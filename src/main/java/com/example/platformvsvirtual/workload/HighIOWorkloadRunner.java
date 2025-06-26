package com.example.platformvsvirtual.workload;


import java.util.concurrent.*;

public class HighIOWorkloadRunner {

    public static void runIOWorkload(ExecutorService executor, int taskCount, int sleepMs) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(sleepMs); // Simulated blocking I/O
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
    }
}

