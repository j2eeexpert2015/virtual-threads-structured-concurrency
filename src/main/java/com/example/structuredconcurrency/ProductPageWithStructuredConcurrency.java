package com.example.structuredconcurrency;

import java.util.concurrent.*;

/**
 * ProductPageWithStructuredConcurrency
 *
 * This demo uses structured concurrency (StructuredTaskScope) to manage child tasks.
 *
 * ▶ Four scenarios are demonstrated:
 *    1. All succeed – Normal operation.
 *    2. Inventory fails – All subtasks canceled immediately.
 *    3. Inventory hangs – Tasks canceled as soon as failure is detected.
 *    4. User cancels parent thread – Cancellation propagates to child tasks.
 *
 * ▶ Observe behavior via logs or JFR/VisualVM: no thread leaks.
 * ▶ Child threads clearly print when interrupted.
 */
public class ProductPageWithStructuredConcurrency {

    public static void main(String[] args) throws Exception {
        ProductPageWithStructuredConcurrency demo = new ProductPageWithStructuredConcurrency();

        //demo.runSuccessScenario();
        //demo.runFailureScenario();
        //demo.runHangScenario();
        demo.runUserCancellationScenario();
    }

    public void runSuccessScenario() throws Exception {
        System.out.println("\n=== [Scenario 1] All Services Succeed ===");
        ProductPageData data = loadProductPage("P-SUCCESS", false, false);
        System.out.println("[Result] " + data);
    }

    public void runFailureScenario() {
        System.out.println("\n=== [Scenario 2] Inventory Service Fails ===");
        try {
            loadProductPage("P-FAIL", false, true);
        } catch (Exception e) {
            System.out.println("❌ Caught exception: " + e.getMessage());
        }
    }

    public void runHangScenario() throws Exception {
        System.out.println("\n=== [Scenario 3] Inventory Service Hangs ===");
        loadProductPage("P-HANG", true, false);
    }

    public void runUserCancellationScenario() throws Exception {
        System.out.println("\n=== [Scenario 4] User Cancels Parent Thread ===");

        Thread parent = new Thread(() -> {
            try {
                ProductPageData data = loadProductPage("P-CANCEL", false, false);
                System.out.println("→ [Parent Thread] Completed: " + data);
            } catch (InterruptedException e) {
                System.out.println("⚠️ [Parent Thread] Interrupted.");
            } catch (Exception e) {
                System.out.println("❌ [Parent Thread] Failed: " + e.getMessage());
            }
        });

        parent.start();
        Thread.sleep(200);
        System.out.println(">>> Simulating user cancellation...");
        parent.interrupt();
        parent.join();
    }

    public ProductPageData loadProductPage(String productId, boolean simulateHang, boolean simulateFailure) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            var productTask = scope.fork(() -> getProductDetails(productId));
            var inventoryTask = scope.fork(() -> getInventoryStatus(productId, simulateHang, simulateFailure));
            var reviewTask = scope.fork(() -> ReviewService.getReviews(productId));

            scope.join();
            scope.throwIfFailed();

            return new ProductPageData(
                    productTask.get(),
                    inventoryTask.get(),
                    reviewTask.get()
            );
        }
    }

    // ===== Simulated Services with Interruption Logging =====

    private ProductDetails getProductDetails(String productId) throws InterruptedException {
        String name = Thread.currentThread().getName();
        System.out.println("→ [Product Service] STARTED on " + name);
        try {
            for (int i = 1; i <= 5; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("⚠️ [Product Service] INTERRUPTED at step " + i);
                    throw new InterruptedException("Product Service interrupted");
                }
                Thread.sleep(100);
            }
            System.out.println("✓ [Product Service] COMPLETED");
            return new ProductDetails(productId, "Wireless Headphones", "Premium noise-canceling headphones");
        } catch (InterruptedException e) {
            System.out.println("✗ [Product Service] EXITING due to interruption");
            throw e;
        }
    }

    private InventoryStatus getInventoryStatus(String productId, boolean hang, boolean fail) throws InterruptedException {
        String name = Thread.currentThread().getName();
        System.out.println("→ [Inventory Service] STARTED on " + name);
        try {
            if (hang) {
                System.out.println("!!! [Inventory Service] HANGING indefinitely...");
                Thread.sleep(Long.MAX_VALUE);
            }

            if (fail) {
                Thread.sleep(300);
                System.out.println("✗ [Inventory Service] THROWING exception");
                throw new RuntimeException("Inventory service failed");
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
        } catch (InterruptedException e) {
            System.out.println("✗ [Inventory Service] EXITING due to interruption");
            throw e;
        }
    }

    static class ReviewService {
        public static CustomerReviews getReviews(String productId) throws InterruptedException {
            String name = Thread.currentThread().getName();
            System.out.println("→ [Review Service] STARTED on " + name);
            try {
                for (int i = 1; i <= 3; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("⚠️ [Review Service] INTERRUPTED at step " + i);
                        throw new InterruptedException("Review Service interrupted");
                    }
                    Thread.sleep(100);
                }
                System.out.println("✓ [Review Service] COMPLETED");
                return new CustomerReviews(productId, 4.6, 248);
            } catch (InterruptedException e) {
                System.out.println("✗ [Review Service] EXITING due to interruption");
                throw e;
            }
        }
    }

    // ===== DTOs =====

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
