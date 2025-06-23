package com.example.virtualthreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Demonstrates using ThreadFactory to create platform and virtual threads with ExecutorService.
 */
public class VirtualThreadCreationWithThreadFactory {
    public static void main(String[] args) throws InterruptedException {

        // Task that prints basic thread info
        Runnable printThreadInfo = () -> {
            Thread current = Thread.currentThread();
            System.out.println("---- Task Started ----");
            System.out.println("Thread Name: " + current.getName());
            System.out.println("Is Virtual: " + current.isVirtual());
            System.out.println("Is Daemon: " + current.isDaemon());
        };

        System.out.println("\n=== Platform Thread ExecutorService ===");
        // Create factory to generate platform threads named "platform-thread-0", "platform-thread-1", ...
        ThreadFactory platformFactory = Thread.ofPlatform().name("platform-thread-", 0).factory();

        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(platformFactory)) {
            for (int i = 0; i < 5; i++) {
                int taskId = i;
                executor.submit(() -> {
                    System.out.println("Platform Task " + taskId);
                    printThreadInfo.run();
                });
            }
        }

        System.out.println("\n=== Virtual Thread ExecutorService ===");
        // Create factory to generate virtual threads named "virtual-thread-0", "virtual-thread-1", ...
        ThreadFactory virtualFactory = Thread.ofVirtual().name("virtual-thread-", 0).factory();

        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(virtualFactory)) {
            for (int i = 0; i < 5; i++) {
                int taskId = i;
                executor.submit(() -> {
                    System.out.println("Virtual Task " + taskId);
                    printThreadInfo.run();
                });
            }
        }
    }
}
