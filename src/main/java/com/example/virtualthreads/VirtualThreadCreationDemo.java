package com.example.virtualthreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demonstrates all official ways to create virtual threads in Java 21.
 * Each creation method is separated into individual demo methods.
 */
public class VirtualThreadCreationDemo {
    
    /**
     * Demo 1: One-shot static method using Thread.startVirtualThread()
     * This is the simplest way to create and start a virtual thread immediately.
     */
    private static void demoStartVirtualThread() {
        System.out.println("\n--- Demo 1: Thread.startVirtualThread() ---");
        Thread.startVirtualThread(() -> {
            System.out.println("1. startVirtualThread: " + Thread.currentThread());
            System.out.println("   Thread name: " + Thread.currentThread().getName());
            System.out.println("   Is virtual: " + Thread.currentThread().isVirtual());
        });
    }
    
    /**
     * Demo 2: Builder pattern with immediate start
     * Allows customization (like naming) before starting the thread.
     */
    private static void demoBuilderWithStart() {
        System.out.println("\n--- Demo 2: Builder.start() ---");
        Thread.ofVirtual()
            .name("custom-virtual-thread-1")
            .start(() -> {
                System.out.println("2. Builder start: " + Thread.currentThread());
                System.out.println("   Custom name applied: " + Thread.currentThread().getName());
            });
    }
    
    /**
     * Demo 3: Builder pattern with unstarted thread
     * Creates the thread object first, then starts it separately.
     * Useful when you need to configure or store the thread reference.
     */
    private static void demoBuilderWithUnstarted() {
        System.out.println("\n--- Demo 3: Builder.unstarted() ---");
        Thread unstartedThread = Thread.ofVirtual()
            .name("custom-virtual-thread-2")
            .unstarted(() -> {
                System.out.println("3. Unstarted then started: " + Thread.currentThread());
                System.out.println("   Thread state before start was NEW");
            });
        
        System.out.println("   Created thread, state: " + unstartedThread.getState());
        unstartedThread.start();
    }
    
    /**
     * Demo 4: ExecutorService for managing multiple virtual threads
     * Best for submitting many tasks that should run on virtual threads.
     */
    private static void demoExecutorService() {
        System.out.println("\n--- Demo 4: ExecutorService ---");
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submit single task
            executor.submit(() -> {
                System.out.println("Executor task: " + Thread.currentThread());
                System.out.println("   Managed by ExecutorService");
            });
            
            // Submit multiple tasks to show scalability
            for (int i = 0; i < 3; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Executor batch task " + taskId + ": " + 
                                     Thread.currentThread().getName());
                });
            }
            
            // ExecutorService automatically closes due to try-with-resources
            System.out.println("   ExecutorService will auto-close and wait for tasks");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Demo: Virtual Thread Creation Styles");
        System.out.println("=====================================");
        
        // Run each demo method
        demoStartVirtualThread();
        demoBuilderWithStart();
        demoBuilderWithUnstarted();
        demoExecutorService();
        
        // Small delay to let all threads finish
        Thread.sleep(100);
        System.out.println("All demos completed!");
    }
}