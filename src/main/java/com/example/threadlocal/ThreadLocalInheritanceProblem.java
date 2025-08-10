package com.example.threadlocal;

/**
 * Demonstrates ThreadLocal (or InheritableThreadLocal) with platform vs virtual threads
 */
public class ThreadLocalInheritanceProblem {

    // Uncomment this to see inheritance in platform threads:
    //private static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

    private static final ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "default");

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Platform vs Virtual Threads (ThreadLocal) ===");

        System.out.println("\n--- Platform Threads ---");
        testWithPlatformThread();

        System.out.println("\n--- Virtual Threads ---");
        testWithVirtualThread();
    }

    private static void testWithPlatformThread() throws InterruptedException {
        threadLocal.set("parent-value");
        print("Parent");

        Thread child = new Thread(ThreadLocalInheritanceProblem::childTask);
        child.start();
        child.join();

        print("Parent (after child)");
        threadLocal.remove();
    }

    private static void testWithVirtualThread() throws InterruptedException {
        threadLocal.set("parent-value");
        print("Parent");

        Thread child = Thread.ofVirtual().start(ThreadLocalInheritanceProblem::childTask);
        child.join();

        print("Parent (after child)");
        threadLocal.remove();
    }

    private static void childTask() {
        print("Child (before set)");
        threadLocal.set("child-value");
        print("Child (after set)");
        threadLocal.remove();
    }

    private static void print(String label) {
        System.out.println(label + " = " + threadLocal.get());
    }
}
