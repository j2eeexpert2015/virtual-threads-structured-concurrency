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

        int NUMBER_OF_THREADS = 10;
        Runnable taskWithLogAndSleep = () -> {
            log("Starting task1 - Initial mount");
            sleepFor(500, ChronoUnit.MICROS);  // This will cause unmount

        };

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = Thread.ofVirtual().unstarted(taskWithLogAndSleep);
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
        System.out.printf("[%-20s] Thread: %s (Virtual: %s), Object: %s%n",
                message,
                current.getName(),
                current.isVirtual(),
                current);

    }
}

