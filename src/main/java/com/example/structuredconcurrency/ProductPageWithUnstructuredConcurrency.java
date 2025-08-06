package com.example.structuredconcurrency;

import java.util.concurrent.*;

/**
 * Demonstrates problems with unstructured concurrency using ExecutorService.
 *
 * ▶ Scenario 1: Failure in one subtask doesn't cancel others
 * ▶ Scenario 2: No interruption/cancellation propagation - User interrupts parent thread, but children continue
 * ▶ Scenario 3: Failure + blocking = wasted time
 */
public class ProductPageWithUnstructuredConcurrency {

    // Using virtual threads to simplify thread analysis in tools like VisualVM and JFR
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) throws Exception {
        ProductPageWithUnstructuredConcurrency productPageWithUnstructuredConcurrency = new ProductPageWithUnstructuredConcurrency();

        //productPageWithUnstructuredConcurrency.runScenario1_FailureDoesNotCancelOthers();
        //productPageWithUnstructuredConcurrency.runScenario2_NoCancellationPropagation();
        productPageWithUnstructuredConcurrency.runScenario3_FailurePlusBlockingWastesTime();

        productPageWithUnstructuredConcurrency.executor.close(); // Does not affect already running tasks
    }

    // Scenario 1: Failure in one task doesn't cancel others
    public void runScenario1_FailureDoesNotCancelOthers() {
        System.out.println("\n=== [Scenario 1] Failure in One Subtask Doesn't Cancel Others ===");
        try {
            loadProductPage("P-FAIL-1", false, true, false);
        } catch (Exception e) {
            System.out.println("❌ Caught in parent: " + e.getMessage());
        }
    }

    // Scenario 2: User interrupt doesn't affect child tasks
    public void runScenario2_NoCancellationPropagation() throws Exception {
        System.out.println("\n=== [Scenario 2] No Interruption/Cancellation Propagation ===");
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
        Thread.sleep(200); // Let tasks start
        System.out.println(">>> Simulating user cancellation...");
        parent.interrupt();
        parent.join();
    }

    // Scenario 3: Failure + blocking = wasted time
    public void runScenario3_FailurePlusBlockingWastesTime() throws Exception {
        System.out.println("\n=== [Scenario 3] Failure + Blocking = Wasted Time ===");

        Future<ProductDetails> productFuture = executor.submit(() -> getProductDetails("P-WASTE"));
        Future<InventoryStatus> inventoryFuture = executor.submit(() -> {
            Thread.sleep(300); // simulate delay before failure
            System.out.println("✗ [Inventory Service] Failing...");
            throw new RuntimeException("Inventory failure");
        });

        Future<CustomerReviews> reviewsFuture = executor.submit(() -> {
            System.out.println("→ [Review Service] Blocking for 5 seconds...");
            Thread.sleep(5000); // simulate slow blocking call
            System.out.println("✓ [Review Service] Done");
            return new CustomerReviews("P-WASTE", 4.4, 100);
        });

        try {
            ProductDetails product = productFuture.get();
            InventoryStatus inventory = inventoryFuture.get(); // throws exception here
            CustomerReviews reviews = reviewsFuture.get();     // still blocks
            System.out.println("[Result] " + new ProductPageData(product, inventory, reviews));
        } catch (Exception e) {
            System.out.println("❌ Exception occurred: " + e.getMessage());
        }
    }


    // Common task runner
    public ProductPageData loadProductPage(String productId, boolean simulateHang, boolean simulateFailure, boolean simulateReviewDelay)
            throws Exception {

        Future<ProductDetails> productFuture = executor.submit(() -> getProductDetails(productId));
        Future<InventoryStatus> inventoryFuture = executor.submit(() ->
                getInventoryStatus(productId, simulateHang, simulateFailure));
        Future<CustomerReviews> reviewsFuture = executor.submit(() ->
                ReviewService.getReviews(productId, simulateReviewDelay));

        ProductDetails product = productFuture.get();
        InventoryStatus inventory = inventoryFuture.get();
        CustomerReviews reviews = reviewsFuture.get();

        return new ProductPageData(product, inventory, reviews);
    }

    // Simulated product service
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

    // Simulated inventory service
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

    // Simulated review service
    static class ReviewService {
        public static CustomerReviews getReviews(String productId, boolean delay) throws InterruptedException {
            System.out.println("→ [Review Service] STARTED");
            if (delay) {
                Thread.sleep(5000); // long blocking delay
            } else {
                Thread.sleep(300);
            }
            System.out.println("✓ [Review Service] COMPLETED");
            return new CustomerReviews(productId, 4.6, 248);
        }
    }

    // DTOs
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
