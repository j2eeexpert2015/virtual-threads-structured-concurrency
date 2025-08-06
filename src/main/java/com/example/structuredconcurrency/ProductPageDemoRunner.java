package com.example.structuredconcurrency;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Demo runner for comparing Unstructured and Structured Concurrency scenarios.
 *
 * Both implementations use the SAME TaskSimulationUtil methods to ensure
 * fair comparison. The difference in behavior comes purely from the
 * concurrency model, not from different implementations.
 */
public class ProductPageDemoRunner {

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("\n Program exited cleanly")));

        // === UNSTRUCTURED CONCURRENCY DEMOS ===
        System.out.println("==================================================");
        System.out.println("        UNSTRUCTURED CONCURRENCY DEMOS");
        System.out.println("==================================================");
        System.out.println("Using ExecutorService - manual task management");

        //runUnstructuredServiceFailure();
        //Thread.sleep(1000); // Small pause between demos


        //runUnstructuredUserCancellation();
        //Thread.sleep(1000);
        /*
        runUnstructuredTimeout();
        Thread.sleep(500);
        */
        // === STRUCTURED CONCURRENCY DEMOS ===
        System.out.println("\n==================================================");
        System.out.println("         STRUCTURED CONCURRENCY DEMOS");
        System.out.println("==================================================");
        System.out.println("Using StructuredTaskScope - automatic task lifecycle management");

        /*
        runStructuredServiceFailure();
        Thread.sleep(1000);
        */
        runStructuredUserCancellation();
        Thread.sleep(1000);

        //runStructuredTimeout();
    }

    // ---------------- Helper method for formatting time ----------------

    private static String formatElapsedTime(long elapsedMs) {
        double seconds = elapsedMs / 1000.0;
        return String.format("%.1f seconds", seconds);
    }

    // ---------------- Unstructured Demos ----------------

    static void runUnstructuredServiceFailure() {
        System.out.println("\n--- UNSTRUCTURED: SERVICE FAILURE ---");
        System.out.println("Scenario: Inventory fails after 1s, Product(3s) and Reviews(4s) continue");
        ProductPageServiceUnstructured service = new ProductPageServiceUnstructured();
        long startTime = System.currentTimeMillis();

        try {
            service.loadProductPage("P100", true, false, false);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.err.println("Exception after " + formatElapsedTime(elapsed) + ": " + e.getClass().getSimpleName());
            System.out.println("ISSUE: Other tasks completed despite failure in one!");
        } finally {
            service.shutdown();
        }
    }

    static void runUnstructuredUserCancellation() {
        System.out.println("\n--- UNSTRUCTURED: USER CANCELLATION ---");
        System.out.println("Scenario: Main thread interrupted after 0.5s");
        ProductPageServiceUnstructured service = new ProductPageServiceUnstructured();
        long startTime = System.currentTimeMillis();

        try {
            Thread mainThread = Thread.currentThread();
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    System.out.println(">>> Simulating user cancellation...");
                    mainThread.interrupt();
                } catch (InterruptedException ignored) {}
            }).start();

            service.loadProductPage("P200", false, true, false);
        } catch (InterruptedException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.err.println(">>> Interrupted after " + formatElapsedTime(elapsed));
            System.out.println(">>> ISSUE: Background tasks may continue running!");
        } catch (Exception e) {
            System.err.println(">>> Exception: " + e.getClass().getSimpleName());
        } finally {
            Thread.interrupted(); // clear interrupt flag
            service.shutdown();
        }
    }

    static void runUnstructuredTimeout() {
        System.out.println("\n--- UNSTRUCTURED: TIMEOUT ---");
        System.out.println("Scenario: Reviews takes 10s (no timeout mechanism)");
        ProductPageServiceUnstructured service = new ProductPageServiceUnstructured();
        long startTime = System.currentTimeMillis();

        try {
            service.loadProductPage("P300", false, false, true);
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Completed after " + formatElapsedTime(elapsed));
            System.out.println("ISSUE: No timeout enforcement - waited full 10 seconds!");
        } catch (Exception e) {
            System.err.println("Exception: " + e.getClass().getSimpleName());
        } finally {
            service.shutdown();
        }
    }

    // ---------------- Structured Demos ----------------

    static void runStructuredServiceFailure() {
        System.out.println("\n--- STRUCTURED: SERVICE FAILURE ---");
        System.out.println("Scenario: Inventory fails after 1s, other tasks should be cancelled");
        long startTime = System.currentTimeMillis();

        try {
            ProductPageServiceStructured service = new ProductPageServiceStructured();
            service.loadProductPage("P400", true, false);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.err.println("Exception after " + formatElapsedTime(elapsed) + ": " + e.getClass().getSimpleName());
            System.out.println("SUCCESS: All tasks cancelled when one failed!");
            System.out.println("Saved compute: ~3 seconds of work avoided");
        }
    }

    static void runStructuredUserCancellation() {
        System.out.println("\n--- STRUCTURED: USER CANCELLATION ---");
        System.out.println("Scenario: Main thread interrupted after 0.5s");
        long startTime = System.currentTimeMillis();

        Thread mainThread = Thread.currentThread();
        Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(500);
                System.out.println(">>> Simulating user cancellation...");
                mainThread.interrupt();
            } catch (InterruptedException ignored) {}
        });

        try {
            interrupter.start();
            ProductPageServiceStructured service = new ProductPageServiceStructured();
            service.loadProductPage("P500", false, false);
        } catch (InterruptedException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.err.println(">>> Interrupted after " + formatElapsedTime(elapsed));
            System.out.println(">>> SUCCESS: All tasks cancelled immediately!");
        } catch (Exception e) {
            System.err.println(">>> Exception: " + e.getClass().getSimpleName());
        } finally {
            Thread.interrupted(); // clear interrupt flag
            try {
                interrupter.join();
            } catch (InterruptedException ignored) {}
        }
    }

    static void runStructuredTimeout() {
        System.out.println("\n--- STRUCTURED: TIMEOUT ---");
        System.out.println("Scenario: Reviews takes 10s, timeout set to 3s");
        long startTime = System.currentTimeMillis();

        try {
            ProductPageServiceStructured service = new ProductPageServiceStructured();
            service.loadProductPage("P600", false, true);
        } catch (TimeoutException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.err.println(">>> Timeout after " + formatElapsedTime(elapsed) + ": " + e.getMessage());
            System.out.println(">>> SUCCESS: Enforced 3-second timeout!");
            System.out.println(">>> All tasks cancelled at timeout boundary");
        } catch (Exception e) {
            System.err.println(">>> Exception: " + e.getClass().getSimpleName());
        }
    }
}