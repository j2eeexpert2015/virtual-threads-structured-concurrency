package com.example.structuredconcurrency;

import java.util.concurrent.*;

/**
 * Unstructured Concurrency using ExecutorService.
 *
 * Demonstrates issues:
 * 1. No automatic task cancellation on failure.
 * 2. Manual handling of interruption.
 * 3. No built-in timeout enforcement.
 */
public class ProductPageServiceUnstructured {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final String LOG_PREFIX = "[UNSTRUCTURED]";

    public ProductPageData loadProductPage(String productId, boolean simulateFailure, boolean simulateUserCancel, boolean simulateTimeout)
            throws ExecutionException, InterruptedException {

        Future<ProductDetails> productFuture = executorService.submit(() -> {
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

        Future<InventoryStatus> inventoryFuture = executorService.submit(() -> {
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

        Future<CustomerReviews> reviewsFuture = executorService.submit(() -> {
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

        if (simulateUserCancel) {
            Thread.currentThread().interrupt(); // simulate user cancel
        }

        // Note: In unstructured concurrency, if one fails, others continue
        // The get() calls will block until each future completes or fails
        ProductDetails productDetails = productFuture.get();
        InventoryStatus inventoryStatus = inventoryFuture.get();
        CustomerReviews customerReviews = reviewsFuture.get();

        return new ProductPageData(productDetails, inventoryStatus, customerReviews);
    }

    public void shutdown() {
        System.out.println(LOG_PREFIX + " Shutting down executor service...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println(LOG_PREFIX + " Force shutting down executor service...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // DTOs
    public record ProductPageData(ProductDetails details, InventoryStatus inventory, CustomerReviews reviews) {}
    public record ProductDetails(String id, String name, String description) {}
    public record InventoryStatus(String productId, int stockCount, boolean available) {}
    public record CustomerReviews(String productId, double averageRating, int totalReviews) {}
}
