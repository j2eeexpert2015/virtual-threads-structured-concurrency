package com.example.virtualthreadcreation;

/**
 * Simple demo showing why custom thread names help with debugging
 */
public class SimpleThreadNamingDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== PROBLEM: Generic Thread Names ===");
        problemDemo();
        
        Thread.sleep(500);
        
        System.out.println("\n=== SOLUTION: Custom Thread Names ===");
        solutionDemo();
    }
    
    // PROBLEM: Hard to identify which task failed
    private static void problemDemo() throws InterruptedException {
        Thread t1 = Thread.startVirtualThread(() -> processOrder("ORDER-123"));
        Thread t2 = Thread.startVirtualThread(() -> processPayment("PAY-456"));  
        Thread t3 = Thread.startVirtualThread(() -> simulateError("TASK-789"));
        
        t1.join();
        t2.join(); 
        t3.join();
        
        System.out.println("🤔 Problem: Empty thread names! Can't identify what failed.");
    }
    
    // SOLUTION: Clear identification of failed task
    private static void solutionDemo() throws InterruptedException {
        Thread t1 = Thread.ofVirtual().name("order-processor").start(() -> processOrder("ORDER-123"));
        Thread t2 = Thread.ofVirtual().name("payment-processor").start(() -> processPayment("PAY-456"));
        Thread t3 = Thread.ofVirtual().name("error-task").start(() -> simulateError("TASK-789"));
        
        t1.join();
        t2.join();
        t3.join();
        
        System.out.println("✅ Solution: Clear thread names! You know 'error-task' failed.");
    }
    
    private static void processOrder(String id) {
        String threadName = Thread.currentThread().getName();
        if (threadName.isEmpty()) threadName = "VirtualThread-" + Thread.currentThread().threadId();
        System.out.println("[" + threadName + "] Processing order: " + id);
    }
    
    private static void processPayment(String id) {
        String threadName = Thread.currentThread().getName();
        if (threadName.isEmpty()) threadName = "VirtualThread-" + Thread.currentThread().threadId();
        System.out.println("[" + threadName + "] Processing payment: " + id);
    }
    
    private static void simulateError(String id) {
        String threadName = Thread.currentThread().getName();
        if (threadName.isEmpty()) threadName = "VirtualThread-" + Thread.currentThread().threadId();
        
        try {
            System.out.println("[" + threadName + "] Starting task: " + id);
            throw new RuntimeException("Database connection failed!");
        } catch (Exception e) {
            System.err.println("ERROR in [" + threadName + "]: " + e.getMessage());
        }
    }
}