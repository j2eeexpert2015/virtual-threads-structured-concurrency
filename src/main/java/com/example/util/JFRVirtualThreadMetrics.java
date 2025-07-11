package com.example.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jdk.jfr.consumer.RecordingStream;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors JFR virtual thread events and exposes metrics using Micrometer.
 * Fixed version that properly handles JFR event data extraction.
 */
public class JFRVirtualThreadMetrics {

    private final Counter startCounter;
    private final Counter endCounter;
    private final Counter pinningCounter;
    private final Timer pinnedTimer;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final CountDownLatch startupLatch = new CountDownLatch(1);
    private RecordingStream recordingStream;

    public JFRVirtualThreadMetrics(MeterRegistry registry) {
        this.startCounter = Counter.builder("jfr_virtual_thread_starts_total")
                .description("Total number of virtual thread starts")
                .register(registry);

        this.endCounter = Counter.builder("jfr_virtual_thread_ends_total")
                .description("Total number of virtual thread terminations")
                .register(registry);

        this.pinningCounter = Counter.builder("jfr_virtual_thread_pinned_events_total")
                .description("Total number of virtual thread pinning events")
                .register(registry);

        this.pinnedTimer = Timer.builder("jfr_virtual_thread_pinned_seconds")
                .description("Duration of virtual thread pinning events")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void startJfrStream() {
        Thread jfrThread = new Thread(() -> {
            try {
                recordingStream = new RecordingStream();

                System.out.println("📡 JFR RecordingStream initializing...");
                System.out.println("🧩 Java Version: " + System.getProperty("java.version"));

                // Configure virtual thread start events
                recordingStream.enable("jdk.VirtualThreadStart").withThreshold(Duration.ofMillis(0));
                recordingStream.onEvent("jdk.VirtualThreadStart", event -> {
                    try {
                        // Don't try to extract thread ID - just log the event and increment counter
                        System.out.println("🟢 VirtualThreadStart event captured!");
                        startCounter.increment();
                    } catch (Exception e) {
                        System.out.println("⚠️ Error processing VirtualThreadStart: " + e.getMessage());
                    }
                });

                // Configure virtual thread end events
                recordingStream.enable("jdk.VirtualThreadEnd").withThreshold(Duration.ofMillis(0));
                recordingStream.onEvent("jdk.VirtualThreadEnd", event -> {
                    try {
                        System.out.println("🔴 VirtualThreadEnd event captured!");
                        endCounter.increment();
                    } catch (Exception e) {
                        System.out.println("⚠️ Error processing VirtualThreadEnd: " + e.getMessage());
                    }
                });

                // Configure virtual thread pinning events
                recordingStream.enable("jdk.VirtualThreadPinned").withThreshold(Duration.ofMillis(0));
                recordingStream.onEvent("jdk.VirtualThreadPinned", event -> {
                    try {
                        System.out.println("📌 VirtualThreadPinned event captured!");
                        pinningCounter.increment();
                        if (event.getDuration() != null && !event.getDuration().isZero()) {
                            pinnedTimer.record(event.getDuration());
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ Error processing VirtualThreadPinned: " + e.getMessage());
                    }
                });

                System.out.println("✅ JFR events configured successfully");

                isRunning.set(true);
                startupLatch.countDown(); // Signal that JFR is ready

                System.out.println("🚀 Starting JFR RecordingStream...");
                recordingStream.start(); // This blocks until stream is closed

            } catch (Exception e) {
                System.out.println("❌ JFR stream failed: " + e.getMessage());
                e.printStackTrace();
                startupLatch.countDown(); // Release waiting threads even on error
            }
        }, "jfr-metrics-thread");

        // Don't set as daemon - we want it to stay alive
        jfrThread.start();

        // Wait for JFR to be ready
        try {
            startupLatch.await();
            Thread.sleep(500); // Additional buffer time
            System.out.println("✅ JFR stream is ready for event capture");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void stop() {
        if (recordingStream != null) {
            recordingStream.close();
            isRunning.set(false);
            System.out.println("🛑 JFR stream stopped");
        }
    }
}