package com.example.pinning;
import me.escoffier.loom.loomunit.LoomUnitExtension;
import me.escoffier.loom.loomunit.ShouldNotPin;
import me.escoffier.loom.loomunit.ShouldPin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.*;

@ExtendWith(LoomUnitExtension.class)
public class HighBlockingIOExampleTest {

    @Test
    //@ShouldNotPin
    @ShouldPin
    public void test() throws Exception {
        System.out.println("PID: " + ProcessHandle.current().pid());
        // Enable detailed pinning diagnostics
        //System.setProperty("jdk.tracePinnedThreads", "full");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Object sharedLock = new Object();
        BlockingQueue<String> taskQueue = new ArrayBlockingQueue<>(10);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    doCpuWork(300);
                    waitForTask(taskQueue, taskId); // YELLOW - Monitor wait (5%)
                    synchronizedWork(sharedLock, taskId); // SALMON - Synchronized block (10%)
                    blockingNetworkCall(); // RED - Blocking I/O (70%)
                    doCpuWork(600); // GREEN - Final CPU (10%)
                    System.out.println("Task " + taskId + " completed.");
                } catch (Exception e) {
                    System.out.println("Task " + taskId + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        long end = System.currentTimeMillis();
        System.out.println("Total time: " + (end - start) + " ms");
        System.out.println("=== EXECUTION COMPLETE ===");

    }

    private static void waitForTask(BlockingQueue<String> queue, int taskId) throws Exception {
        String task = queue.poll(500, TimeUnit.MILLISECONDS);
        if (task == null) queue.offer("work-" + taskId, 100, TimeUnit.MILLISECONDS);
    }

    private static void synchronizedWork(Object lock, int taskId) {
        synchronized (lock) {
            try {
                Thread.sleep(200);
                double result = 0;
                for (int i = 0; i < 100000; i++) result += Math.sqrt(i);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void blockingNetworkCall() throws Exception {
        try {
            URL url = new URL("https://httpbin.org/delay/8");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                while (reader.readLine() != null);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Network timeout (still blocking I/O)");
        }
    }

    private static void doCpuWork(long durationMs) {
        long start = System.currentTimeMillis();
        long result = 0;
        while (System.currentTimeMillis() - start < durationMs) {
            for (int i = 0; i < 10000; i++) {
                result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
            }
        }
        if (result == Long.MAX_VALUE) System.out.println("Unlikely result: " + result);
    }
}

