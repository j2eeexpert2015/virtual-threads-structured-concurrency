package com.example.pinning;


import com.example.util.CommonUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * VM Options for running with JFR monitoring:
 *  -XX:StartFlightRecording=filename=pinning_demo.jfr,settings=./jfr-config/virtual-threads.jfc -Djdk.tracePinnedThreads=full
 *
 * Notes:
 * 1. The `virtual-threads.jfc` file should be located in the `jfr-config` folder under the project.
 * 2. After execution, open `pinning_demo.jfr` in Java Mission Control (JMC) to analyze pinning events,
 *    particularly `jdk.VirtualThreadPinned`.
 * 3. `-Djdk.tracePinnedThreads=full` will log the first pinning event per call site to stdout.
 */
public class VirtualThreadPinning {

    // Sleep duration for simulating blocking
    private static final int SLEEP_TIME_MS = 10000;

    // Simulates a blocking operation
    public static void simulateBlockingWithWait() {
        try {
            System.out.println("[" + Thread.currentThread().getName() + "] Blocking task started");
            Thread.sleep(SLEEP_TIME_MS); // Simulates blocking I/O or delay
            System.out.println("[" + Thread.currentThread().getName() + "] Blocking task finished");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // This method causes pinning because of the synchronized keyword
    public static  synchronized void simulateBlockingWorkWithSynchronized() {
        simulateBlockingWithWait();
    }
    // Simulates a blocking operation with a ReentrantLock
    public static void simulateBlockingWithReEntrantLock() {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            System.out.println("[" + Thread.currentThread().getName() + "] Blocking with ReentrantLock");
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    // Custom ThreadFactory to assign names to virtual threads
    private static final ThreadFactory namedVirtualThreadFactory =
            Thread.ofVirtual().name("VT-PinningDemo-", 0).factory();

    private static final ExecutorService vtExecutor = Executors.newThreadPerTaskExecutor(namedVirtualThreadFactory);

    public static void main(String[] args) {
        //CommonUtil.waitForUserInput();
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < 250; i++) {
            final int taskId = i;
            vtExecutor.submit(() -> {
                System.out.println("Task " + taskId + " started on [" + Thread.currentThread().getName() + "]");
                //simulateBlockingWorkWithSynchronized();
                simulateBlockingWithReEntrantLock();
                System.out.println("Task " + taskId + " completed");
            });
        }
        // Shutdown and wait for tasks to complete
        vtExecutor.shutdown();
        try {
            if (!vtExecutor.awaitTermination(SLEEP_TIME_MS + 5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Forcing shutdown - tasks took too long.");
                vtExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //CommonUtil.waitForUserInput();
    }
}
