package com.example.structuredconcurrency;

import java.util.concurrent.StructuredTaskScope;

public class ProductAvailabilityWithShutdownOnSuccess {

    public static void main(String[] args) throws Exception {
        ProductAvailabilityWithShutdownOnSuccess demo = new ProductAvailabilityWithShutdownOnSuccess();
        demo.runAvailabilityCheck();
    }

    // Run the scenario where we check multiple sources and return the first success
    public void runAvailabilityCheck() throws Exception {
        System.out.println("\n=== [SCENARIO] Availability Check from Redundant Sources (ShutdownOnSuccess) ===");

        String productId = "P-FAST";

        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<InventoryStatus>()) {

            scope.fork(() -> getInventoryFromSource("Warehouse-A", productId, 200, true));
            scope.fork(() -> getInventoryFromSource("Warehouse-B", productId, 1500, true));
            scope.fork(() -> getInventoryFromSource("Partner-API", productId, 1000, false));

            scope.join(); // wait for any successful task

            InventoryStatus result = scope.result();
            if (result == null) {
                System.out.println("❌ No inventory source returned success");
            } else {
                System.out.println("✅ First available source responded: " + result);
            }
        }
    }

    // Simulate inventory source call with configurable delay and success/failure
    private InventoryStatus getInventoryFromSource(String source, String productId, int delayMs, boolean available)
            throws InterruptedException {

        String thread = Thread.currentThread().getName();
        System.out.printf("→ [%s] STARTED on %s (delay=%dms)%n", source, thread, delayMs);

        Thread.sleep(delayMs);

        if (Thread.currentThread().isInterrupted()) {
            System.out.printf("⚠️ [%s] INTERRUPTED on %s%n", source, thread);
            throw new InterruptedException(source + " was interrupted");
        }

        if (!available) {
            System.out.printf("✗ [%s] FAILED → Inventory not available%n", source);
            throw new RuntimeException("Inventory not available from " + source);
        }

        System.out.printf("✓ [%s] SUCCESS → Available%n", source);
        return new InventoryStatus(productId, source, 20, true);
    }

    // Simple DTO to track source info
    record InventoryStatus(String productId, String source, int quantity, boolean available) {
        @Override
        public String toString() {
            return String.format("[productId=%s, source=%s, quantity=%d, available=%s]",
                    productId, source, quantity, available);
        }
    }
}
