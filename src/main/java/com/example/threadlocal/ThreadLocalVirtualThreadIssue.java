package com.example.threadlocal;

import java.util.concurrent.StructuredTaskScope;

/**
 * ThreadLocal set in a parent thread is NOT visible in child virtual threads
 * (both plain virtual threads and those forked via StructuredTaskScope).
 */
public class ThreadLocalVirtualThreadIssue {

    private static final ThreadLocal<String> requestContext = new ThreadLocal<>();

    private static void print(String label) {
        System.out.println(label + " = " + requestContext.get());
    }

    public static void main(String[] args) throws Exception {
        // Parent thread sets a value
        requestContext.set("parent-value");
        print("Parent Thread");

        // --- Case 1: Plain virtual thread ---
        Thread virtualThread = Thread.ofVirtual().start(() -> {
            print("Virtual Thread (before set)");          // null
            requestContext.set("virtual-thread-value");
            print("Virtual Thread (after set)");           // virtual-thread-value
            requestContext.remove();
        });
        virtualThread.join();

        // --- Case 2: StructuredTaskScope with virtual threads ---
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> { print("Structured Scope — Child Virtual Thread 1"); return null; }); // null
            scope.fork(() -> {
                requestContext.set("structured-scope-child-value");
                print("Structured Scope — Child Virtual Thread 2 (after set)");                    // structured-scope-child-value
                requestContext.remove();
                return null;
            });
            scope.fork(() -> { print("Structured Scope — Child Virtual Thread 3"); return null; }); // null
            scope.join();
        }

        // Parent remains unchanged
        print("Parent Thread (after scope)"); // parent-value
        requestContext.remove();
    }
}
