package com.example.threadlocal;

import java.util.concurrent.StructuredTaskScope;

/**
 * Unified demonstration of ThreadLocal vs InheritableThreadLocal across:
 *  1) Platform child thread
 *  2) Virtual child thread
 *  3) StructuredTaskScope with virtual children
 *
 * ThreadLocal: child does NOT see parent's value.
 * InheritableThreadLocal: child receives a COPY of the parent's value at start time.
 *
 * To see ThreadLocal tracing, run with JVM argument:
 *   java -Djdk.traceVirtualThreadLocals=true com.example.threadlocal.ThreadLocalInheritanceProblem
 *
 * Or in IDE, add VM option: -Djdk.traceVirtualThreadLocals=true
 */
public class ThreadLocalInheritanceProblem {

    public static void main(String[] args) throws Exception {
        // NOTE: Setting this at runtime has NO EFFECT - must be set at JVM startup!
        // System.setProperty("jdk.traceVirtualThreadLocals", "true"); // ❌ TOO LATE!

        System.out.println("=== Plain ThreadLocal ===");
        ThreadLocal<String> requestContext = ThreadLocal.withInitial(() -> "default");
        //runScenariosFor(requestContext);

        System.out.println("\n=== InheritableThreadLocal (copy-on-start) ===");
        InheritableThreadLocal<String> inheritableRequestContext = new InheritableThreadLocal<>() {
            @Override protected String initialValue() { return "default"; }
        };
        runScenariosFor(inheritableRequestContext);
    }

    // Runs all three scenarios for the supplied context holder
    private static void runScenariosFor(ThreadLocal<String> contextHolder) throws Exception {
        System.out.println("\n--- Platform child ---");
        runPlatformChildScenario(contextHolder);

        System.out.println("\n--- Virtual child ---");
        runVirtualChildScenario(contextHolder);

        System.out.println("\n--- StructuredTaskScope (virtual children) ---");
        runStructuredScopeScenario(contextHolder);
    }

    // Scenario 1: Parent → platform child
    private static void runPlatformChildScenario(ThreadLocal<String> contextHolder) throws InterruptedException {
        contextHolder.set("parent-value");
        printContext(contextHolder, "Parent");

        Thread child = new Thread(() -> runChildTask(contextHolder));
        child.start();
        child.join();

        printContext(contextHolder, "Parent (after child)");
        contextHolder.remove();
    }

    // Scenario 2: Parent → virtual child
    private static void runVirtualChildScenario(ThreadLocal<String> contextHolder) throws InterruptedException {
        contextHolder.set("parent-value");
        printContext(contextHolder, "Parent");

        Thread child = Thread.ofVirtual().start(() -> runChildTask(contextHolder));
        child.join();

        printContext(contextHolder, "Parent (after child)");
        contextHolder.remove();
    }

    // Scenario 3: Parent → multiple virtual children in a StructuredTaskScope
    private static void runStructuredScopeScenario(ThreadLocal<String> contextHolder) throws Exception {
        contextHolder.set("parent-value");
        printContext(contextHolder, "Parent");

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> { printContext(contextHolder, "Scope child 1 (before set)"); return null; });
            scope.fork(() -> {
                printContext(contextHolder, "Scope child 2 (before set)");
                contextHolder.set("scope-child-2-value");
                printContext(contextHolder, "Scope child 2 (after set)");
                contextHolder.remove();
                return null;
            });
            scope.fork(() -> { printContext(contextHolder, "Scope child 3 (before set)"); return null; });
            scope.join();
        }

        printContext(contextHolder, "Parent (after scope)");
        contextHolder.remove();
    }

    // Child task used by scenarios 1 & 2
    private static void runChildTask(ThreadLocal<String> contextHolder) {
        printContext(contextHolder, "Child (before set)");
        contextHolder.set("child-value");
        printContext(contextHolder, "Child (after set)");
        contextHolder.remove();
    }

    private static void printContext(ThreadLocal<String> contextHolder, String label) {
        System.out.println(label + " = " + contextHolder.get());
    }
}