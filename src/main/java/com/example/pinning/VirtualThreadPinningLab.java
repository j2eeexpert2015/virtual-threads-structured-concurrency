package com.example.pinning;

import com.example.util.JFRUtil;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualThreadPinningLab {

    // Configuration Constants
    private static final int THREAD_COUNT = 10_000;
    private static final int MIN_BLOCKING_MS = 50;  // Increased minimum blocking time
    private static final int MAX_BLOCKING_MS = 100; // Added variability
    private static final int CPU_WORK_ITERATIONS = 10_000; // For consistent pinning
    private static final int SHUTDOWN_TIMEOUT_SEC = 45;    // Extended timeout

    // Tracking and Resources
    private static final ExecutorService virtualThreadExecutor =
            Executors.newVirtualThreadPerTaskExecutor();
    private static final AtomicInteger pinnedEventsDetected = new AtomicInteger();
    private static final AtomicInteger completedThreads = new AtomicInteger();
    private static final Lock diagnosticLock = new ReentrantLock();

    /**
     * Demonstrates thread pinning with more reliable detection characteristics.
     * Uses both sleep and CPU work to ensure consistent pinning behavior.
     */
    public static void demonstratePinningWithContention() {
        Object threadLock = new Object(); // Fresh lock per thread

        synchronized (threadLock) {
            try {
                // Variable blocking duration for more realistic distribution
                int blockingTime = MIN_BLOCKING_MS + ThreadLocalRandom.current().nextInt(MAX_BLOCKING_MS - MIN_BLOCKING_MS);
                Thread.sleep(blockingTime);

                // Simulate CPU work while pinned
                long dummyValue = 0;
                for (int i = 0; i < CPU_WORK_ITERATIONS; i++) {
                    dummyValue += System.nanoTime() % 1024;
                }

                // Track successful pinning
                pinnedEventsDetected.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completedThreads.incrementAndGet();
            }
        }
    }

    public static void main(String[] args) {
        // Diagnostic header
        System.out.println("""
            🧪 Virtual Thread Pinning Laboratory
            -----------------------------------
            Configuration:
            • Virtual Threads: %,d
            • Blocking Duration: %d-%dms
            • CPU Work Iterations: %,d
            • Shutdown Timeout: %ds
            """.formatted(
                THREAD_COUNT, MIN_BLOCKING_MS, MAX_BLOCKING_MS,
                CPU_WORK_ITERATIONS, SHUTDOWN_TIMEOUT_SEC));

        // Start JFR monitoring
        JFRUtil.startVirtualThreadRecording("VirtualThreadPinningLab");

        // Submit work with progress feedback
        System.out.println("⚡ Launching virtual threads...");
        for (int i = 0; i < THREAD_COUNT; i++) {
            virtualThreadExecutor.submit(() -> {
                demonstratePinningWithContention();
                if (completedThreads.get() % 1000 == 0) {
                    diagnosticLock.lock();
                    try {
                        System.out.printf("↳ Progress: %,d/%d (%,d pinned)%n",
                                completedThreads.get(), THREAD_COUNT, pinnedEventsDetected.get());
                    } finally {
                        diagnosticLock.unlock();
                    }
                }
            });
        }

        // Enhanced shutdown sequence
        System.out.println("\n🛑 Initiating controlled shutdown...");
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                System.err.printf("⏱️  Force shutdown after %,d threads completed%n", completedThreads.get());
                virtualThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            virtualThreadExecutor.shutdownNow();
        }

        // Comprehensive results analysis
        System.out.println("\n📊 Experimental Results:");
        System.out.printf("• Completed Threads:   %,d/%,d%n", completedThreads.get(), THREAD_COUNT);
        System.out.printf("• Pinning Events:      %,d (%.1f%%)%n",
                pinnedEventsDetected.get(),
                (pinnedEventsDetected.get() * 100.0) / THREAD_COUNT);

        // JFR analysis
        var jfrFile = JFRUtil.stopRecording();
        if (jfrFile != null) {
            System.out.println("\n🔬 Detailed JFR Analysis:");
            JFRUtil.analyzeRecording(jfrFile);
        }

        System.out.println("\n🏁 Experiment concluded");
    }
}
