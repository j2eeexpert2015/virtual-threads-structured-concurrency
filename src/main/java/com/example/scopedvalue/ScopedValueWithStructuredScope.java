package com.example.scopedvalue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

/**
 * ScopedValue + StructuredTaskScope (JDK 21).
 * - Parent binds a ScopedValue.
 * - Child tasks see that binding automatically.
 * - A child can override it in a nested scope; it reverts after exiting.
 */
public class ScopedValueWithStructuredScope {

    private static final ScopedValue<String> REQUEST_CONTEXT = ScopedValue.newInstance();

    public static void main(String[] args) {
        System.out.println("=== ScopedValue + StructuredTaskScope Demo ===\n");
        runParentTaskWithChildren();
    }

    private static void runParentTaskWithChildren() {
        ScopedValue.where(REQUEST_CONTEXT, "Request from Parent Thread").run(() -> {
            logScopedValue("Parent thread (before starting children)");

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var fetchUserTask = scope.fork(() -> {
                    logScopedValue("Child-1: Fetch User (initial read)");
                    ScopedValue.where(REQUEST_CONTEXT, "Overridden by Child-1").run(() ->
                            logScopedValue("Child-1: Fetch User (inside nested scope)")
                    );
                    logScopedValue("Child-1: Fetch User (after nested scope)");
                    return "User Data";
                });

                var fetchOrdersTask = scope.fork(() -> {
                    logScopedValue("Child-2: Fetch Orders (initial read)");
                    return "Order Data";
                });

                // Wait for children and propagate any failure
                scope.join();
                scope.throwIfFailed();

                // JDK 21: get() still declares ExecutionException
                String userData  = fetchUserTask.get();
                String orderData = fetchOrdersTask.get();
                logResult("Results gathered", userData, orderData);

            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parent task failed", e);
            }

            logScopedValue("Parent thread (after children finished)");
        });
    }

    // ---------- helpers ----------

    private static void logScopedValue(String message) {
        Thread t = Thread.currentThread();
        String threadType = t.isVirtual() ? "Virtual Thread" : "Platform Thread";
        String value = REQUEST_CONTEXT.isBound() ? REQUEST_CONTEXT.get() : "unbound";
        System.out.printf(
                "%s [%s] | Thread name = %s | ScopedValue = \"%s\"%n",
                message, threadType, t.getName(), value
        );
    }

    private static void logResult(String label, String userData, String orderData) {
        System.out.printf("%s => UserData: \"%s\", OrderData: \"%s\"%n",
                label, userData, orderData);
    }
}
