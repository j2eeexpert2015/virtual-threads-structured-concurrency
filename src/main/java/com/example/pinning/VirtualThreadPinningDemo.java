package com.example.pinning;

import com.example.util.CommonUtil;
import com.example.util.JFRUtil;
import com.example.util.JFRUtilWithJFC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * VirtualThreadPinningDemo
 *
 * VM Options for running with JFR monitoring:
 *
 *  -XX:StartFlightRecording=filename=pinning_demo.jfr,settings=./jfr-config/virtual-threads.jfc,dumponexit=true -Djdk.tracePinnedThreads=full
 *
 * Notes:
 * 1. The `virtual-threads.jfc` file should be located in the `jfr-config` folder under the project.
 * 2. After execution, open `pinning_demo.jfr` in Java Mission Control (JMC) to analyze pinning events,
 *    particularly `jdk.VirtualThreadPinned`.
 * 3. `-Djdk.tracePinnedThreads=full` will log the first pinning event per call site to stdout.
 * 4. A small delay is added at the end to ensure JFR flushes the recorded data before JVM shutdown.
 */
public class VirtualThreadPinningDemo {

    // Simulates a blocking operation
    public static void simulateBlockingWithWait() {
        try {
            System.out.println("Blocking task started");
            Thread.sleep(21000); // Simulates blocking I/O or delay
            System.out.println("Blocking task finished");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Simulates a blocking operation
    public static void simulateBlockingWithReEntrantLock() {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            // Blocking I/O while holding the lock
            Thread.sleep(10000); // or any blocking operation
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            lock.unlock();
        }
    }

    // This method causes pinning because of the synchronized keyword
    public static synchronized void simulateBlockingWorkWithSynchronized() {
        simulateBlockingWithWait();
    }

    private static final ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) {
        System.out.println("🚀 Starting JFR recording for virtual thread monitoring...");
        //JFRUtil.startVirtualThreadRecording("VirtualThreadJFRDemo");
        JFRUtilWithJFC.startRecording();
        System.out.println("Running Java Version: " + System.getProperty("java.version"));
        //CommonUtil.waitForUserInput();
        // Enable detailed pinning event logging (commented here as it's in VM options)
        //System.setProperty("jdk.tracePinnedThreads", "full");
        System.out.println("=== Virtual Thread Pinning Demo ===");
        //vtExecutor.submit(VirtualThreadPinningDemo::simulateBlockingWorkWithSynchronized);

        for (int i = 1; i <= 10000; i++) {
            int id = i;
            vtExecutor.submit(VirtualThreadPinningDemo::simulateBlockingWorkWithSynchronized);
            //vtExecutor.submit(VirtualThreadPinningDemo::simulateBlockingWithReEntrantLock);

        }


        // Shutdown and wait for tasks to complete instead of Thread.sleep(10000)
        vtExecutor.shutdown();
        try {
            if (!vtExecutor.awaitTermination(15, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown - tasks took too long.");
                vtExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Add small delay to ensure JFR flushes the recording
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //CommonUtil.waitForUserInput();
        System.out.println("\n📊 Stopping JFR recording and analyzing results...");
        //var jfrFile = JFRUtil.stopRecording();
        var jfrFile = JFRUtilWithJFC.stopRecording();
        if (jfrFile != null) {
            JFRUtil.analyzeRecording(jfrFile);
        }
        System.out.println("=== Demo Finished ===");
    }
}
