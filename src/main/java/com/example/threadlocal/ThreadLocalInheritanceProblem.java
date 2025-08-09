package com.example.threadlocal;

/**
 * Demonstrates that regular ThreadLocal values do NOT flow from a parent thread
 * to child threads (both platform and virtual).
 */
public class ThreadLocalInheritanceProblem {

    // Thread-local variable with default value
    //private static final ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "default");
    private static final InheritableThreadLocal<String> threadLocal =
            new InheritableThreadLocal<>() {
                @Override protected String initialValue() { return "default"; }
            };

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Step 2: ThreadLocal Inheritance Problem ===\n");

        System.out.println("▶ Running with Platform Threads");
        demonstrateThreadLocalInheritance(false);

        System.out.println("\n▶ Running with Virtual Threads");
        demonstrateThreadLocalInheritance(true);
    }

    /**
     * Demonstrates that ThreadLocal values are not inherited by child threads.
     *
     * @param useVirtualThreads true to use a virtual thread for the child, false for platform thread
     */
    private static void demonstrateThreadLocalInheritance(boolean useVirtualThreads) throws InterruptedException {
        // Parent thread sets its own value
        threadLocal.set("Value set by parent thread");
        printThreadLocalState("Parent thread");

        // Create and run child thread
        Thread childThread = useVirtualThreads
                ? Thread.ofVirtual().name("Child-Virtual-Thread").unstarted(ThreadLocalInheritanceProblem::childTask)
                : new Thread(ThreadLocalInheritanceProblem::childTask, "Child-Platform-Thread");

        childThread.start();
        childThread.join();

        // Show parent's value again to confirm it’s unchanged
        printThreadLocalState("Parent thread (after child finished)");
    }

    /**
     * Code executed by the child thread: reads parent's TL (will be default),
     * then writes its own value and reads it back.
     */
    private static void childTask() {
        printThreadLocalState("Child thread (initial read)"); // likely "default"
        threadLocal.set("Value set by child thread");
        printThreadLocalState("Child thread (after setting own value)");
    }

    /**
     * Prints the thread's type, name, and current ThreadLocal value with role context.
     */
    private static void printThreadLocalState(String role) {
        Thread t = Thread.currentThread();
        String type = t.isVirtual() ? "Virtual Thread" : "Platform Thread";
        System.out.printf(
                "%s [%s] | Thread name = %s | ThreadLocal value = \"%s\"%n",
                role, type, t.getName(), threadLocal.get()
        );
    }
}
