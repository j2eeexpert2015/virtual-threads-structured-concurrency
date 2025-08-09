package com.example.scopedvalue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

/**
 * End-to-end examples of:
 *  - ScopedValue.newInstance()
 *  - ScopedValue.runWhere(...)
 *  - ScopedValue.where(...).run(...)
 *  - ScopedValue.where(...).call(...)
 *  - Composing multiple where(...) bindings
 *  - Using StructuredTaskScope with ScopedValue (capture/restore)
 *
 * Requires: JDK 21 (run with --enable-preview only if your toolchain insists for structured scope).
 */
public class ScopedValueApiExamples {

    // Create reusable placeholders (unbound until used inside a scope)
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    private static final ScopedValue<Integer> TENANT_ID  = ScopedValue.newInstance();

    public static void main(String[] args) {
        System.out.println("=== 1) runWhere: bind and run (Runnable) ===");
        exampleRunWhere();

        System.out.println("\n=== 2) where(...).run(): same as runWhere but with a binder ===");
        exampleWhereRun();

        System.out.println("\n=== 3) where(...).call(): bind and return a value (Callable) ===");
        String result = exampleWhereCall();
        System.out.println("Returned from call(): " + result);

        System.out.println("\n=== 4) Compose multiple where(...) bindings, then run ===");
        exampleComposeBindings();

        System.out.println("\n=== 5) StructuredTaskScope + ScopedValue (capture to children) ===");
        exampleStructuredScope();
    }

    // ---------------------------------------------------------------------
    // 1) runWhere: one-liner bind + run (Runnable)
    // ---------------------------------------------------------------------
    private static void exampleRunWhere() {
        ScopedValue.runWhere(REQUEST_ID, "REQ-001", () -> {
            log("Inside runWhere for REQUEST_ID");
            // REQUEST_ID is visible here; TENANT_ID is unbound
            System.out.println("REQUEST_ID: " + REQUEST_ID.get());
            System.out.println("TENANT_ID bound? " + TENANT_ID.isBound());
        });
        // Outside scope → unbound again
        System.out.println("After runWhere -> REQUEST_ID bound? " + REQUEST_ID.isBound());
    }

    // ---------------------------------------------------------------------
    // 2) where(...).run(): same behavior, but you keep a binder (flexible)
    // ---------------------------------------------------------------------
    private static void exampleWhereRun() {
        var binder = ScopedValue.where(REQUEST_ID, "REQ-002");
        binder.run(() -> {
            log("Inside where(...).run()");
            System.out.println("REQUEST_ID: " + REQUEST_ID.get());
        });
        System.out.println("After where(...).run() -> REQUEST_ID bound? " + REQUEST_ID.isBound());
    }

    // ---------------------------------------------------------------------
    // 3) where(...).call(): bind and compute a return value
    // ---------------------------------------------------------------------
    private static String exampleWhereCall() {
        try {
            return ScopedValue.where(REQUEST_ID, "REQ-003")
                    .call(() -> {
                        log("Inside where(...).call()");
                        return "computed-with-" + REQUEST_ID.get();
                    });
        } catch (Exception e) { // call() can throw checked exceptions from your Callable
            throw new RuntimeException("call() failed", e);
        }
    }

    // ---------------------------------------------------------------------
    // 4) Compose multiple bindings before run()
    // ---------------------------------------------------------------------
    private static void exampleComposeBindings() {
        ScopedValue.where(REQUEST_ID, "REQ-004")
                .where(TENANT_ID, 42)
                .run(() -> {
                    log("Inside composed where(...).run()");
                    System.out.println("REQUEST_ID: " + REQUEST_ID.get());
                    System.out.println("TENANT_ID : " + TENANT_ID.get());
                });
        System.out.println("After composed run() -> REQUEST_ID bound? " + REQUEST_ID.isBound());
        System.out.println("After composed run() -> TENANT_ID  bound? " + TENANT_ID.isBound());
    }

    // ---------------------------------------------------------------------
    // 5) StructuredTaskScope + ScopedValue
    //    - Children forked via scope.fork(...) capture current bindings.
    //    - A child can override within a nested scope; parent binding remains intact.
    // ---------------------------------------------------------------------
    private static void exampleStructuredScope() {
        ScopedValue.where(REQUEST_ID, "REQ-SCOPE")
                .where(TENANT_ID, 7)
                .run(() -> {
                    log("Parent (before children)");
                    System.out.println("REQUEST_ID: " + REQUEST_ID.get() + ", TENANT_ID: " + TENANT_ID.get());

                    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                        var fetchUserTask = scope.fork(() -> {
                            log("Child-1: initial read");
                            System.out.println("REQUEST_ID: " + REQUEST_ID.get() + ", TENANT_ID: " + TENANT_ID.get());

                            // Override within a nested scope for this child only
                            ScopedValue.where(REQUEST_ID, "REQ-CHILD-1-OVERRIDE").run(() -> {
                                log("Child-1: inside nested override");
                                System.out.println("REQUEST_ID: " + REQUEST_ID.get() + ", TENANT_ID: " + TENANT_ID.get());
                            });

                            log("Child-1: after nested override");
                            System.out.println("REQUEST_ID: " + REQUEST_ID.get() + ", TENANT_ID: " + TENANT_ID.get());
                            return "UserData";
                        });

                        var fetchOrdersTask = scope.fork(() -> {
                            log("Child-2: initial read");
                            System.out.println("REQUEST_ID: " + REQUEST_ID.get() + ", TENANT_ID: " + TENANT_ID.get());
                            return "OrderData";
                        });

                        scope.join();          // wait for both
                        scope.throwIfFailed(); // propagate child failure if any

                        // JDK 21: get() declares ExecutionException
                        String userData  = fetchUserTask.get();
                        String orderData = fetchOrdersTask.get();
                        System.out.println("Results gathered => user=" + userData + ", orders=" + orderData);

                    } catch (InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Parent task failed", e);
                    }

                    log("Parent (after children)");
                    System.out.println("REQUEST_ID: " + REQUEST_ID.get() + ", TENANT_ID: " + TENANT_ID.get());
                });
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static void log(String label) {
        Thread t = Thread.currentThread();
        String kind = t.isVirtual() ? "Virtual Thread" : "Platform Thread";
        System.out.printf("%s [%s] | thread=%s%n", label, kind, t.getName());
    }
}
