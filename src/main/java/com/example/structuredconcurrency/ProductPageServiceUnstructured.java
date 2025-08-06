package com.example.structuredconcurrency;

import com.example.util.CommonUtil;

import java.util.Set;
import java.util.concurrent.*;

/**
 * Demonstrates thread leak due to unstructured concurrency.
 */
public class ProductPageServiceUnstructured {

    private final ExecutorService executor = Executors.newFixedThreadPool(20, new NamedThreadFactory("UnstructuredPool-"));

    public void loadProductPage(String productId, boolean simulateInventoryHang) {
        int activeThreadsBefore = Thread.activeCount();

        System.out.println("\n=== [UNSTRUCTURED] Loading product page for: " + productId + " ===");

        try {
            Future<ProductDetails> productFuture = executor.submit(() -> {
                System.out.println("[Unstructured] → ProductDetails thread: " + Thread.currentThread().getName());
                Thread.sleep(1000);
                return new ProductDetails(productId, "Wireless Headphones");
            });

            Future<InventoryStatus> inventoryFuture = executor.submit(() -> {
                System.out.println("[Unstructured] → InventoryStatus thread: " + Thread.currentThread().getName());
                if (simulateInventoryHang) {
                    Thread.sleep(60_000); // Simulate hang
                } else {
                    Thread.sleep(1000);
                }
                return new InventoryStatus(productId, 10);
            });

            Future<CustomerReviews> reviewsFuture = executor.submit(() -> {
                System.out.println("[Unstructured] → CustomerReviews thread: " + Thread.currentThread().getName());
                Thread.sleep(1000);
                return new CustomerReviews(productId, 4.7);
            });

            // Simulate timeout of 2 seconds
            ProductDetails product = productFuture.get(2, TimeUnit.SECONDS);
            InventoryStatus inventory = inventoryFuture.get(2, TimeUnit.SECONDS);
            CustomerReviews reviews = reviewsFuture.get(2, TimeUnit.SECONDS);

            System.out.println("[Unstructured] Page Loaded: " + product + ", " + inventory + ", " + reviews);

        } catch (Exception e) {
            System.err.println("[Unstructured] ❌ Exception: " + e.getMessage());
            // ❗ Futures are NOT cancelled → thread leak risk!
        }

        int activeThreadsAfter = Thread.activeCount();
        System.out.println("[Unstructured] 🧵 Threads Before: " + activeThreadsBefore + ", After: " + activeThreadsAfter);

        printLivePoolThreads("UnstructuredPool-");
    }

    public static void main(String[] args) throws InterruptedException {
        CommonUtil.waitForUserInput();
        ProductPageServiceUnstructured service = new ProductPageServiceUnstructured();

        for (int i = 0; i < 5; i++) {
            service.loadProductPage("P100" + i, true); // Simulate hang in inventory
        }

        Thread.sleep(10_000); // Allow hung threads to accumulate

        System.out.println("\n[Unstructured] 🧾 FINAL Thread Count: " + Thread.activeCount());
    }

    private static void printLivePoolThreads(String prefix) {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        threads.stream()
                .filter(t -> t.getName().startsWith(prefix) && t.isAlive())
                .forEach(t -> System.out.println("🟡 Still alive → " + t.getName()));
    }

    // Sample DTOs
    record ProductDetails(String id, String name) {}
    record InventoryStatus(String id, int quantity) {}
    record CustomerReviews(String id, double rating) {}

    // Thread factory with naming for easier identification
    static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private int count = 1;

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, prefix + count++);
        }
    }
}

