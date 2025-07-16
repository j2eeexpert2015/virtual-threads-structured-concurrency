package com.example.virtualthreadcreation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.util.concurrent.*;

public class VirtualThreadBatchProcessor {

    private static final int TASK_COUNT = 1000;
    private static final boolean USE_IO = true; // Toggle between I/O and CPU-bound tasks
    private static final String EXTERNAL_API_URL = "https://postman-echo.com/delay/1";
    //private static final String EXTERNAL_API_URL = "https://randomuser.me/api";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Virtual Thread Batch Processor Started ===");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        long start = System.nanoTime();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < TASK_COUNT; i++) {
            int taskId = i;
            executor.submit(() -> processTask(taskId));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES); // Increase for large TASK_COUNT
        long end = System.nanoTime();

        System.out.printf("=== Virtual Thread Batch Processor Completed in %d ms ===%n", (end - start) / 1_000_000);
    }

    private static void processTask(int taskId) {
        if (USE_IO) {
            performIOBoundTask(taskId);
        } else {
            performCpuBoundTask(taskId);
        }
    }

    private static void performIOBoundTask(int taskId) {
        long start = System.nanoTime();
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(new URI(EXTERNAL_API_URL)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long end = System.nanoTime();
            System.out.printf("Task %05d completed on %s (isVirtual=%b), status: %d, time: %d ms%n",
                    taskId, Thread.currentThread().getName(),
                    Thread.currentThread().isVirtual(),
                    response.statusCode(),
                    (end - start) / 1_000_000);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            System.err.printf("Task %05d failed: %s%n", taskId, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static void performCpuBoundTask(int taskId) {
        long start = System.nanoTime();
        long result = 0;
        for (int i = 0; i < 10_000_000; i++) {
            result += Math.sqrt(i);
        }
        long end = System.nanoTime();
        System.out.printf("Task %05d completed on %s (isVirtual=%b), time: %d ms%n",
                taskId, Thread.currentThread().getName(),
                Thread.currentThread().isVirtual(),
                (end - start) / 1_000_000);
    }
}
