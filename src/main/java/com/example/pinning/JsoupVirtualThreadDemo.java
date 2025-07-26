package com.example.pinning;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates Jsoup usage with Virtual Threads and jdk.tracePinnedThreads.
 */
public class JsoupVirtualThreadDemo {
    public static void main(String[] args) {
        System.setProperty("jdk.tracePinnedThreads", "full");

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // Create multiple concurrent requests to increase chance of pinning
        for (int i = 0; i < 10; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Starting request " + requestId + " on " + Thread.currentThread());

                    // This should potentially cause pinning according to the GitHub issue
                    Document doc = Jsoup.connect("https://httpbin.org/delay/2")
                            .timeout(5000)
                            .get();

                    System.out.println("Completed request " + requestId + " - Title: " + doc.title());
                } catch (Exception e) {
                    System.err.println("Error in request " + requestId + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Give JFR time to flush
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}