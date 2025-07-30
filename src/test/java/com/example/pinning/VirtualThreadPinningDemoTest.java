package com.example.pinning;

import me.escoffier.loom.loomunit.LoomUnitExtension;
import me.escoffier.loom.loomunit.ShouldNotPin;
import me.escoffier.loom.loomunit.ShouldPin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Tests for VirtualThreadPinningDemo using LoomUnit.
 *
 * LoomUnit runs these test methods in virtual threads to check for pinning.
 */
@ExtendWith(LoomUnitExtension.class)
public class VirtualThreadPinningDemoTest {

    /**
     * This test should detect pinning because the method uses `synchronized`
     * and performs a blocking call (`Thread.sleep`).
     */
    @Test
    //@ShouldPin
    @ShouldNotPin
    void testPinningWithSynchronized() {
        ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();
        vtExecutor.submit(VirtualThreadPinning::simulateBlockingWorkWithSynchronized);
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
    }
    @Test
    @ShouldNotPin
    void testPinningWithReentrantlock() {
        ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();
        vtExecutor.submit(VirtualThreadPinning::simulateBlockingWithReEntrantLock);
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
    }
}
