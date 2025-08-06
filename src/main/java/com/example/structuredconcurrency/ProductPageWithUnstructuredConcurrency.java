package com.example.structuredconcurrency;

import java.util.concurrent.*;

/**
 * ProductPageWithUnstructuredConcurrency
 *
 * This demo shows issues with unstructured concurrency using ExecutorService.
 *
 * ▶ Four scenarios are demonstrated:
 *    1. All succeed – Normal operation.
 *    2. Inventory fails – Other services still run; no cancellation.
 *    3. Inventory hangs – JVM stays alive; thread leak.
 *    4. User cancels parent thread – Subtasks continue unaffected.
 *
 * ▶ Use VisualVM or JFR to observe thread state and leaks (especially Scenario 3).
 */
public class ProductPageWithUnstructuredConcurrency {

    // Using virtual threads for better observability and resource cost
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) throws Exception {
        ProductPageWithUnstructuredConcurrency demo = new ProductPageWithUnstructuredConcurrency();

        //demo.runSuccessScenario();
        //demo.runFailureScenario();
        //demo.runHangScenario();
        demo.runUserCancellationScenario();

        demo.executor.close(); // This will not interrupt hung threads
    }

    // === Scenario 1: All succeed ===
    public void runSuccessScenario() throws Exception {
        System.out.println("\n=== [Scenario 1] All Services Succeed ===");
        ProductPageData data = loadProductPage("P-SUCCESS", false, false);
        System.out.println("[Result] " + data);
    }

    // === Scenario 2: Inventory fails ===
    public void runFailureScenario() {
        System.out.println("\n=== [Scenario 2] Inventory Service Fails ===");
        try {
            loadProductPage("P-FAIL", false, true);
        } catch (Exception e) {
            System.out.println("❌ Caught exception in parent: " + e.getMessage());
        }
    }

    // === Scenario 3: Inventory hangs (thread leak) ===
    public void runHangScenario() throws Exception {
        System.out.println("\n=== [Scenario 3] Inventory Service Hangs ===");
        loadProductPage("P-HANG", true, false);
    }

    // === Scenario 4: User interrupts parent thread, children unaffected ===
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
        Thread.sleep(200); // Let child tasks begin
        System.out.println(">>> Simulating user cancellation (interrupting parent)...");
        parent.interrupt();
        parent.join();
    }

    // === Page loading logic ===
    public ProductPageData loadProductPage(String productId, boolean simulateHang, boolean simulateFailure)
            throws Exception {

        Future<ProductDetails> productFuture = executor.submit(() -> getProductDetails(productId));
        Future<InventoryStatus> inventoryFuture = executor.submit(() ->
                getInventoryStatus(productId, simulateHang, simulateFailure));
        Future<CustomerReviews> reviewsFuture = executor.submit(() ->
                ReviewService.getReviews(productId));

        // No cancellation coordination here
        ProductDetails product = productFuture.get();
        InventoryStatus inventory = inventoryFuture.get();
        CustomerReviews reviews = reviewsFuture.get();

        return new ProductPageData(product, inventory, reviews);
    }

    // === Simulated Services with Interruption Awareness ===

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

    private InventoryStatus getInventoryStatus(String productId, boolean hang, boolean fail)
            throws InterruptedException {
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
