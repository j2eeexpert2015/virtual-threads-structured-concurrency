package com.example.pinning;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class NoPinningReentrantLockExample {
    private static final ReentrantLock lock = new ReentrantLock();
    public static void main(String[] args) throws InterruptedException {
        // Platform thread holds the lock
        new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Platform thread acquired lock 🛑");
                Thread.sleep(2000); // Simulate long operation
                System.out.println("Platform thread released lock ✅");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }).start();
        Thread.sleep(100); // Ensure platform thread gets lock first
        // Virtual thread unmounts while waiting
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                System.out.println("Virtual thread attempting to acquire lock ⏳");
                lock.lock();
                try {
                    System.out.println("Virtual thread acquired lock! 🎉");
                } finally {
                    lock.unlock(); // Always unlock in finally block
                }
            });
        }
    }
}
