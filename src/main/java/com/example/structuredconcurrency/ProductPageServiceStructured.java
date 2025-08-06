package com.example.structuredconcurrency;

import com.example.util.CommonUtil;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Fixes the thread leak using structured concurrency.
 */
public class ProductPageServiceStructured {

    public void loadProductPage(String productId, boolean simulateInventoryHang) throws InterruptedException {
        int activeThreadsBefore = Thread.activeCount();

        System.out.println("\n=== [STRUCTURED] Loading product page for: " + productId + " ===");

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var productFuture = scope.fork(() -> {
                System.out.println("[Structured] → ProductDetails thread: " + Thread.currentThread().getName());
                Thread.sleep(1000);
                return new ProductDetails(productId, "Wireless Headphones");
            });

            var inventoryFuture = scope.fork(() -> {
                System.out.println("[Structured] → InventoryStatus thread: " + Thread.currentThread().getName());
                if (simulateInventoryHang) {
                    Thread.sleep(60_000); // Simulate hang
                } else {
                    Thread.sleep(1000);
                }
                return new InventoryStatus(productId, 10);
            });

            var reviewsFuture = scope.fork(() -> {
                System.out.println("[Structured] → CustomerReviews thread: " + Thread.currentThread().getName());
                Thread.sleep(1000);
                return new CustomerReviews(productId, 4.7);
            });

            scope.joinUntil(Instant.now().plusSeconds(2)); // 2s timeout
            scope.throwIfFailed(); // Cancel all if one fails or times out

            System.out.println("[Structured] Page Loaded: " +
                    productFuture.get() + ", " +
                    inventoryFuture.get() + ", " +
                    reviewsFuture.get());

        } catch (Exception e) {
            System.err.println("[Structured] ✅ Scope Handled Exception: " + e.getMessage());
        }

        int activeThreadsAfter = Thread.activeCount();
        System.out.println("[Structured] 🧵 Threads Before: " + activeThreadsBefore + ", After: " + activeThreadsAfter);

        printLiveVirtualThreads();
    }

    public static void main(String[] args) throws InterruptedException {
        CommonUtil.waitForUserInput();
        ProductPageServiceStructured service = new ProductPageServiceStructured();

        for (int i = 0; i < 5; i++) {
            service.loadProductPage("P200" + i, true); // Simulate inventory hang
        }

        Thread.sleep(10_000); // Let everything clean up

        System.out.println("\n[Structured] 🧾 FINAL Thread Count: " + Thread.activeCount());
    }

    private static void printLiveVirtualThreads() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        threads.stream()
                .filter(t -> t.getName().contains("Virtual") && t.isAlive())
                .forEach(t -> System.out.println("🟢 Clean → " + t.getName()));
    }

    // Sample DTOs
    record ProductDetails(String id, String name) {}
    record InventoryStatus(String id, int quantity) {}
    record CustomerReviews(String id, double rating) {}
}

