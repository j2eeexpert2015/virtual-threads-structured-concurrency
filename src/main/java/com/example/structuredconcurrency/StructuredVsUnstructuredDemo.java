package com.example.structuredconcurrency;

/**
 *
 * This runner demonstrates side-by-side comparison between:
 * - ProductPageWithUnstructuredConcurrency
 * - ProductPageWithStructuredConcurrency
 *
 * Scenarios:
 * 1. Failure in one task
 * 2. Parent thread interrupted
 * 3. Failure + blocking = wasted time
 *
 */
public class StructuredVsUnstructuredDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== DEMO: Unstructured vs Structured Concurrency ===");

        // === Scenario 1: Failure doesn't cancel others ===
        System.out.println("\n================== SCENARIO 1 ==================");
        System.out.println(">>> Unstructured: Failure in one task");
        new ProductPageWithUnstructuredConcurrency().runScenario1_FailureDoesNotCancelOthers();

        System.out.println("\n>>> Structured: Failure cancels others");
        new ProductPageWithStructuredConcurrency().runScenario1_FailureCancelsOthers();

        // === Scenario 2: Parent interruption ===
        System.out.println("\n================== SCENARIO 2 ==================");
        System.out.println(">>> Unstructured: Parent thread interrupted");
        new ProductPageWithUnstructuredConcurrency().runScenario2_NoCancellationPropagation();

        System.out.println("\n>>> Structured: Parent interruption cancels subtasks");
        new ProductPageWithStructuredConcurrency().runScenario2_CancellationPropagation();

        // === Scenario 3: Failure + blocking ===
        System.out.println("\n================== SCENARIO 3 ==================");
        System.out.println(">>> Unstructured: Failure + blocking wastes time");
        new ProductPageWithUnstructuredConcurrency().runScenario3_FailurePlusBlockingWastesTime();

        System.out.println("\n>>> Structured: Failure + blocking exits early");
        new ProductPageWithStructuredConcurrency().runScenario3_AvoidWastedTimeOnFailure();
    }
}

