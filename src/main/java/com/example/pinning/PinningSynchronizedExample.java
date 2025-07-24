package com.example.pinning;

import java.util.concurrent.Executors;

public class PinningSynchronizedExample {
    private static final Object lock = new Object();
    public static void main(String[] args) throws InterruptedException {
        // Platform thread holds the lock
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("Platform thread acquired lock 🛑");
                try {
                    Thread.sleep(2000); // Simulate long operation
                    System.out.println("Platform thread released lock ✅");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
        Thread.sleep(100); // Ensure platform thread gets lock first
        // Virtual thread will pin while waiting
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                System.out.println("Virtual thread attempting to acquire lock ⏳");
                synchronized (lock) {
                    System.out.println("Virtual thread acquired lock! 🎉");
                }
            });
        }
    }
}
