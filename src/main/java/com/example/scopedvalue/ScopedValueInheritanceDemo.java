package com.example.scopedvalue;

/**
 * Demonstrates that ScopedValue automatically flows from a parent thread
 * to child threads (both platform and virtual) within the defined scope.
 */
public class ScopedValueInheritanceDemo {

    private static final ScopedValue<String> scopedValue = ScopedValue.newInstance();

    public static void main(String[] args) {
        System.out.println("=== Step 3: ScopedValue Inheritance ===\n");

        System.out.println("▶ Running with Platform Threads");
        demonstrateScopedValueInheritance(false);

        System.out.println("\n" + "-".repeat(80) + "\n");

        System.out.println("▶ Running with Virtual Threads");
        demonstrateScopedValueInheritance(true);
    }

    private static void demonstrateScopedValueInheritance(boolean useVirtualThreads) {
        ScopedValue.where(scopedValue, "Value set by parent thread")
                .run(() -> {
                    printScopedValueState("Parent thread");

                    Thread childThread = useVirtualThreads
                            ? Thread.ofVirtual().name("Child-Virtual-Thread").unstarted(ScopedValueInheritanceDemo::childTask)
                            : new Thread(ScopedValueInheritanceDemo::childTask, "Child-Platform-Thread");

                    childThread.start();
                    try {
                        childThread.join();  // Runnable can't throw checked exceptions, so catch here
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // preserve interrupt status
                        throw new RuntimeException(ie);
                    }

                    printScopedValueState("Parent thread (after child finished)");
                });
    }

    private static void childTask() {
        printScopedValueState("Child thread (initial read)");

        ScopedValue.where(scopedValue, "Value set by child thread")
                .run(() -> printScopedValueState("Child thread (after setting own value in nested scope)"));

        printScopedValueState("Child thread (after nested scope)");
    }

    private static void printScopedValueState(String role) {
        Thread t = Thread.currentThread();
        String type = t.isVirtual() ? "Virtual Thread" : "Platform Thread";
        String value = scopedValue.isBound() ? scopedValue.get() : "unbound";
        System.out.printf(
                "%s [%s] | Thread name = %s | ScopedValue = \"%s\"%n",
                role, type, t.getName(), value
        );
    }
}
