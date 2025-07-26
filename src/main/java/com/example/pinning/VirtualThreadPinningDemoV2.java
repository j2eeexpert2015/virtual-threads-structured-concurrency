package com.example.pinning;


import com.example.util.JFRUtil;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class VirtualThreadPinningDemoV2 {

        private static final int THREAD_COUNT = 10_000;
        private static final int BLOCKING_DURATION_MS = 10;
        private static final int SHUTDOWN_TIMEOUT_SEC = 30;

        private static final ExecutorService virtualThreadExecutor =
                Executors.newVirtualThreadPerTaskExecutor();
        private static final AtomicInteger pinnedThreadsCounter = new AtomicInteger();

        /**
         * Simulates a blocking operation while holding a thread-local lock
         * to demonstrate virtual thread pinning.
         */
        public static void demonstrateThreadPinning() {
            Object threadLocalLock = new Object(); // Each thread gets its own lock

            synchronized (threadLocalLock) {
                try {
                    // Simulate blocking operation while holding the lock
                    Thread.sleep(BLOCKING_DURATION_MS);
                    pinnedThreadsCounter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public static void main(String[] args) {
            System.out.println("🚀 Starting Virtual Thread Pinning Analysis");
            System.out.printf("• Creating %,d virtual threads%n", THREAD_COUNT);
            System.out.printf("• Each will block for %dms under synchronization%n", BLOCKING_DURATION_MS);

            JFRUtil.startVirtualThreadRecording("VirtualThreadPinningAnalysis");

            // Submit all tasks
            for (int i = 0; i < THREAD_COUNT; i++) {
                virtualThreadExecutor.submit(VirtualThreadPinningDemoV2::demonstrateThreadPinning);
            }

            // Handle graceful shutdown
            virtualThreadExecutor.shutdown();
            try {
                if (!virtualThreadExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                    System.err.println("⚠️ Warning: Not all tasks completed within timeout");
                    virtualThreadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                virtualThreadExecutor.shutdownNow();
            }

            // Analyze results
            System.out.println("\n📊 Pinning Results:");
            System.out.printf("• Expected pinned threads: %,d%n", THREAD_COUNT);
            System.out.printf("• Actual pinned threads detected: %,d%n", pinnedThreadsCounter.get());

            var jfrFile = JFRUtil.stopRecording();
            if (jfrFile != null) {
                System.out.println("\n🔍 Analyzing JFR recording...");
                JFRUtil.analyzeRecording(jfrFile);
            }

            System.out.println("\n✅ Analysis complete");
        }
    }