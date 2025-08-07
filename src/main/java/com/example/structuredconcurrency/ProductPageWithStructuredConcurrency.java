package com.example.structuredconcurrency;

import com.example.util.CommonUtil;

import java.util.concurrent.*;

/**
 * ProductPageWithStructuredConcurrency
 *
 * Demonstrates how structured concurrency (StructuredTaskScope.ShutdownOnFailure)
 * solves common unstructured concurrency issues.
 *
 * ▶ Scenario 1: Failure in one subtask cancels others
 * ▶ Scenario 2: User interrupt cancels subtasks
 * ▶ Scenario 3: Failure + blocking = tasks canceled early
 * ▶ Scenario 4: Proper propagation of user cancellation
 */
public class ProductPageWithStructuredConcurrency {

    public static void main(String[] args) throws Exception {
        System.out.println("PID: " + ProcessHandle.current().pid());
        ProductPageWithStructuredConcurrency productPageWithStructuredConcurrency = new ProductPageWithStructuredConcurrency();

        productPageWithStructuredConcurrency.runScenario1_FailureCancelsOthers();
        productPageWithStructuredConcurrency.runScenario2_CancellationPropagation();
        productPageWithStructuredConcurrency.runScenario3_AvoidWastedTimeOnFailure();
    }

    // Scenario 1: Failure in one subtask cancels others
    public void runScenario1_FailureCancelsOthers() {
        System.out.println("\n=== [Scenario 1] Failure in One Subtask Cancels Others ===");
        try {
            loadProductPage("P-FAIL-1", false, true, false);
        } catch (Exception e) {
            System.out.println("❌ Caught in parent: " + e.getMessage());
        }
    }

    // Scenario 2: User interrupt cancels all subtasks
    public void runScenario2_CancellationPropagation() throws Exception {
        System.out.println("\n=== [Scenario 2] Cancellation Propagation to Subtasks ===");

        Thread parent = new Thread(() -> {
            try {
                loadProductPage("P-CANCEL", false, false, false);
                System.out.println("→ [Parent Thread] Completed");
            } catch (InterruptedException e) {
                System.out.println("⚠️ [Parent Thread] Interrupted.");
            } catch (Exception e) {
                System.out.println("❌ [Parent Thread] Failed: " + e.getMessage());
            }
        });

        parent.start();
        Thread.sleep(200); // Let child tasks begin
        System.out.println(">>> Interrupting parent thread...");
        parent.interrupt();
        parent.join();
    }

    // Scenario 3: Failure + blocking – wasted time avoided
    public void runScenario3_AvoidWastedTimeOnFailure() throws Exception {
        System.out.println("\n=== [Scenario 3] Failure + Blocking = Canceled Early ===");
        try {
            loadProductPage("P-WASTE", false, true, true);
        } catch (Exception e) {
            System.out.println("❌ Caught in parent: " + e.getMessage());
        }
    }

    // === Structured Concurrency Implementation ===
    public ProductPageData loadProductPage(String productId, boolean simulateHang, boolean simulateFailure, boolean simulateReviewDelay)
            throws Exception {

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var productTask = scope.fork(() -> getProductDetails(productId));
            var inventoryTask = scope.fork(() -> getInventoryStatus(productId, simulateHang, simulateFailure));
            var reviewTask = scope.fork(() -> ReviewService.getReviews(productId, simulateReviewDelay));

            scope.join();             // Wait for all tasks or fail-fast
            scope.throwIfFailed();    // Throw if any task failed

            return new ProductPageData(
                    productTask.get(),
                    inventoryTask.get(),
                    reviewTask.get()
            );
        }
    }

    // === Simulated Services with Interruption Awareness ===

    private ProductDetails getProductDetails(String productId) throws InterruptedException {
        System.out.println("→ [Product Service] STARTED");

        for (int i = 1; i <= 5; i++) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("⚠️ [Product Service] INTERRUPTED at step " + i);
                throw new InterruptedException("Product Service interrupted");
            }
            Thread.sleep(100);
        }
        System.out.println("✓ [Product Service] COMPLETED");
        return new ProductDetails(productId, "Wireless Headphones", "Premium noise-canceling headphones");
    }

    private InventoryStatus getInventoryStatus(String productId, boolean hang, boolean fail)
            throws InterruptedException {
        System.out.println("→ [Inventory Service] STARTED");
        if (hang) {
            System.out.println("!!! [Inventory Service] HANGING indefinitely...");
            Thread.sleep(Long.MAX_VALUE);
        }
        if (fail) {
            Thread.sleep(300);
            System.out.println("✗ [Inventory Service] THROWING exception");
            throw new RuntimeException("Inventory service failed for product: " + productId);
        }

        for (int i = 1; i <= 4; i++) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("⚠️ [Inventory Service] INTERRUPTED at step " + i);
                throw new InterruptedException("Inventory Service interrupted");
            }
            Thread.sleep(100);
        }

        System.out.println("✓ [Inventory Service] COMPLETED");
        return new InventoryStatus(productId, 12, true);
    }

    static class ReviewService {
        public static CustomerReviews getReviews(String productId, boolean delay) throws InterruptedException {
            System.out.println("→ [Review Service] STARTED");
            int totalDelay = delay ? 5000 : 300;
            int step = 100;
            for (int i = 1; i <= totalDelay / step; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("⚠️ [Review Service] INTERRUPTED at step " + i);
                    throw new InterruptedException("Review Service interrupted");
                }
                Thread.sleep(step);
            }
            System.out.println("✓ [Review Service] COMPLETED");
            return new CustomerReviews(productId, 4.6, 248);
        }
    }

    // === DTOs ===

    record ProductDetails(String id, String name, String description) {}
    record InventoryStatus(String id, int quantity, boolean available) {}
    record CustomerReviews(String id, double rating, int reviewCount) {}

    record ProductPageData(ProductDetails product, InventoryStatus inventory, CustomerReviews reviews) {
        @Override
        public String toString() {
            return product.name() + " | Available: " + inventory.available() +
                    " | Rating: " + reviews.rating() + " (" + reviews.reviewCount() + " reviews)";
        }
    }
}
