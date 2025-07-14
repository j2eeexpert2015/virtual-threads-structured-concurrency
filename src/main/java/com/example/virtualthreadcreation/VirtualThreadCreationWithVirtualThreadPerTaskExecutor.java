package com.example.virtualthreadcreation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demonstrates running tasks using platform threads vs. virtual threads.
 * Prints all thread info in a single line to avoid scrambled output.
 */
public class VirtualThreadCreationWithVirtualThreadPerTaskExecutor {
    public static void main(String[] args) throws InterruptedException {

        // Task that prints thread info in one line
        Runnable task = () -> {
            Thread current = Thread.currentThread();
            System.out.println(
                "Thread: " + current.getName() +
                " | Virtual: " + current.isVirtual() +
                " | Daemon: " + current.isDaemon()
            );
        };

        System.out.println("\n=== Platform Thread Executor ===");
        try (ExecutorService executor = Executors.newCachedThreadPool()) {
            for (int i = 0; i < 5; i++) {
                executor.submit(task);
            }
        }

        System.out.println("\n=== Virtual Thread Per Task Executor ===");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                executor.submit(task);
            }
        }
        // No manual shutdown, threads are cleaned up after task ends
    }
}
