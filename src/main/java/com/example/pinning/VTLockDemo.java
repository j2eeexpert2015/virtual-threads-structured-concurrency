package com.example.pinning;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class VTLockDemo {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final ExecutorService vtPool = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) {
        for (int i = 0; i < 100_000; i++) {
            vtPool.submit(() -> {
                lock.lock();
                try {
                    Thread.sleep(200); // Simulate any blocking operation
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
        }
        vtPool.shutdown();
    }
}