package com.example.structuredconcurrency;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

/**
 * Structured Concurrency with ShutdownOnFailure.
 *
 * Handles:
 * 1. Cancelling remaining tasks on failure.
 * 2. Handling user interruption.
 * 3. Enforcing timeout using joinUntil().
 */
public class ProductPageServiceStructured {

    private static final String LOG_PREFIX = "[STRUCTURED]";

    public ProductPageData loadProductPage(String productId, boolean simulateFailure, boolean simulateTimeout)
            throws InterruptedException, ExecutionException, TimeoutException {

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            StructuredTaskScope.Subtask<ProductDetails> productFuture =
                    scope.fork(() -> {
                        System.out.println(LOG_PREFIX + " Starting Product Details...");
                        boolean completed = TaskSimulationUtil.simulateDelayWithCancellationCheck(
                                "Product Details",
                                TaskSimulationUtil.PRODUCT_DETAILS_DELAY_MS,
                                LOG_PREFIX
                        );
                        if (!completed) {
                            throw new CancellationException("Product Details cancelled");
                        }
                        System.out.println(LOG_PREFIX + " Completed Product Details");
                        return new ProductDetails(productId, "Wireless Headphones", "Premium noise-canceling headphones");
                    });

            StructuredTaskScope.Subtask<InventoryStatus> inventoryFuture =
                    scope.fork(() -> {
                        System.out.println(LOG_PREFIX + " Starting Inventory Status" + (simulateFailure ? " (will fail)" : "") + "...");
                        int delay = simulateFailure ?
                                TaskSimulationUtil.INVENTORY_FAILURE_DELAY_MS :
                                TaskSimulationUtil.INVENTORY_STATUS_DELAY_MS;

                        boolean completed = TaskSimulationUtil.simulateDelayWithCancellationCheck(
                                "Inventory Status",
                                delay,
                                LOG_PREFIX
                        );

                        if (!completed) {
                            throw new CancellationException("Inventory Status cancelled");
                        }

                        if (simulateFailure) {
                            System.out.println(LOG_PREFIX + " Inventory Status failed!");
                            throw new RuntimeException("Inventory service failed!");
                        }

                        System.out.println(LOG_PREFIX + " Completed Inventory Status");
                        return new InventoryStatus(productId, 15, true);
                    });

            StructuredTaskScope.Subtask<CustomerReviews> reviewsFuture =
                    scope.fork(() -> {
                        System.out.println(LOG_PREFIX + " Starting Customer Reviews" + (simulateTimeout ? " (slow)" : "") + "...");
                        int delay = simulateTimeout ?
                                TaskSimulationUtil.CUSTOMER_REVIEWS_SLOW_DELAY_MS :
                                TaskSimulationUtil.CUSTOMER_REVIEWS_DELAY_MS;

                        boolean completed = TaskSimulationUtil.simulateDelayWithCancellationCheck(
                                "Customer Reviews",
                                delay,
                                LOG_PREFIX
                        );

                        if (!completed) {
                            throw new CancellationException("Customer Reviews cancelled");
                        }

                        System.out.println(LOG_PREFIX + " Completed Customer Reviews");
                        return new CustomerReviews(productId, 4.5, 1250);
                    });

            // Handle timeout scenario differently
            if (simulateTimeout) {
                Instant deadline = Instant.now().plus(Duration.ofSeconds(3));
                scope.joinUntil(deadline);

                // Check if we timed out
                if (!scope.isShutdown()) {
                    throw new TimeoutException("Structured concurrency timed out after 3 seconds!");
                }
            } else {
                // For non-timeout scenarios, just join (wait for all or failure)
                scope.join();
            }

            // This will throw if any task failed
            scope.throwIfFailed();

            // Get results - will throw if task was cancelled
            return new ProductPageData(
                    productFuture.get(),
                    inventoryFuture.get(),
                    reviewsFuture.get()
            );
        }
    }

    // DTOs - same as unstructured version
    public record ProductPageData(ProductDetails details, InventoryStatus inventory, CustomerReviews reviews) {}
    public record ProductDetails(String id, String name, String description) {}
    public record InventoryStatus(String productId, int stockCount, boolean available) {}
    public record CustomerReviews(String productId, double averageRating, int totalReviews) {}
}
