package com.example.threadlocal;


import java.util.concurrent.StructuredTaskScope;

/**
 * Demonstrates that ThreadLocal values set on the parent thread
 * are NOT visible in child virtual threads (both plain virtual threads and
 * those forked via StructuredTaskScope).
 *
 * Run with:  java --enable-preview com.example.threadlocal.ThreadLocalVirtualThreadIssue
 */
public class ThreadLocalVirtualThreadIssue {

    // Thread-local variable holding request-specific data
    private static final ThreadLocal<String> requestContext = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        // Set value on the main (platform) thread
        requestContext.set("parent-value");
        System.out.println("From Parent Thread,ThreadLocal value(requestContext) = " + requestContext.get());

        // --- Case 1: Plain virtual thread ---
        Thread virtualThread = Thread.ofVirtual().start(() -> {
            // Will be null: ThreadLocal doesn't flow from parent to child virtual thread
            System.out.println("From Virtual Thread,ThreadLocal value(requestContext) =" + requestContext.get());
            requestContext.set("virtual-thread-value");
            System.out.println("After set , from Virtual Thread,ThreadLocal value(requestContext) = " + requestContext.get());
        });
        virtualThread.join();



        // --- Case 2: StructuredTaskScope with virtual threads ---
        //try (var taskScope = new StructuredTaskScope.ShutdownOnSuccess<Void>()) {
        try (var taskScope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork child virtual threads
            taskScope.fork(() -> {
                System.out.println("From [Structured Scope vthread-1] requestContext = " + requestContext.get()); // null
                return null;
            });
            taskScope.fork(() -> {
                requestContext.set("structured-scope-child-value");
                System.out.println("From [Structured Scope vthread-2 after set] requestContext = " + requestContext.get());
                return null;
            });
            taskScope.fork(() -> {
                System.out.println("From [Structured Scope vthread-3] requestContext = " + requestContext.get()); // still null
                return null;
            });

            taskScope.join(); // Wait for all tasks
        }

        // Parent is still its own value
        System.out.println("[Parent Thread after structured scope] requestContext = " + requestContext.get());
    }
}
