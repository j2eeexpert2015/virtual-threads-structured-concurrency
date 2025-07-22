package com.example.mountandunmount;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates how virtual threads get mounted and unmounted on carrier threads
 * when performing blocking operations like Thread.sleep().
 */
public class VirtualThreadMountUnmount {

    public static void main(String[] args) throws InterruptedException {

        Runnable task1 = () -> {
            log("Starting task1 - Initial mount");
            sleepFor(10, ChronoUnit.MICROS);  // This will cause unmount
            log("After first sleep - Mounted again");
            sleepFor(10, ChronoUnit.MICROS);
            log("After second sleep - Mounted again");
            sleepFor(10, ChronoUnit.MICROS);
            log("After third sleep - Mounted again");
        };

        Runnable task2 = () -> {
            sleepFor(10, ChronoUnit.MICROS);
            sleepFor(10, ChronoUnit.MICROS);
            sleepFor(10, ChronoUnit.MICROS);
        };

        int NUMBER_OF_THREADS = 10;
        List<Thread> threads = new ArrayList<>();

        for (int index = 0; index < NUMBER_OF_THREADS; index++) {
            Thread thread = (index == 0)
                    ? Thread.ofVirtual().unstarted(task1)
                    : Thread.ofVirtual().unstarted(task2);
            threads.add(thread);
        }

        // Start all virtual threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        log("All virtual threads have completed execution.");
    }

    /**
     * Utility method to sleep for a given duration and simulate blocking I/O.
     */
    private static void sleepFor(int amount, ChronoUnit unit) {
        try {
            Thread.sleep(Duration.of(amount, unit));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Logs the current thread info with a custom message.
     */
    private static void log(String message) {
        Thread current = Thread.currentThread();
        System.out.printf("[%-20s] Thread: %s (Virtual: %s)%n",
                message,
                current.getName(),
                current.isVirtual());
    }
}

