package com.example.virtualthreadcreation;

/**
 * Demonstrates two advanced builder options for virtual threads:
 * 1. inheritInheritableThreadLocals(boolean) – Controls context inheritance.
 * 2. uncaughtExceptionHandler(...) – Catches unhandled exceptions in the thread.
 */
public class VirtualThreadBuilderAdvancedDemo {

    // InheritableThreadLocal to simulate user context propagation
    private static final InheritableThreadLocal<String> userContext = new InheritableThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== Virtual Thread: InheritableThreadLocal Demo ===");
        userContext.set("ParentUser-A");

        Runnable inheritedTask = () -> {
            String threadName = Thread.currentThread().getName();
            String contextValue = userContext.get(); // May be null if not inherited
            System.out.println("[" + threadName + "] Inherited user context: " + contextValue);
        };

        // Virtual thread that inherits thread-local value from parent
        Thread inheritedTrue = Thread.ofVirtual()
            .name("inherited-true-thread")
            .inheritInheritableThreadLocals(true)
            .start(inheritedTask);

        // Virtual thread that does NOT inherit thread-local value
        Thread inheritedFalse = Thread.ofVirtual()
            .name("inherited-false-thread")
            .inheritInheritableThreadLocals(false)
            .start(inheritedTask);

        inheritedTrue.join();
        inheritedFalse.join();

        System.out.println("\n=== Virtual Thread: UncaughtExceptionHandler Demo ===");

        // Task that throws an exception
        Runnable faultyTask = () -> {
            throw new RuntimeException("Simulated failure!");
        };

        // Virtual thread with custom exception handler
        Thread errorHandledThread = Thread.ofVirtual()
            .name("exception-thread")
            .uncaughtExceptionHandler((t, e) -> {
                System.err.println("Uncaught exception in thread [" + t.getName() + "]: " + e.getMessage());
            })
            .start(faultyTask);

        errorHandledThread.join();
    }
}
