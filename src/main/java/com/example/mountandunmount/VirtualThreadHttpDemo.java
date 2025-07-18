package com.example.mountandunmount;

import com.example.util.JFRUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Demonstrates using virtual threads for concurrent HTTP calls.
 *
 * To enable JFR recording for virtual threads, use this JVM argument:
 * -XX:StartFlightRecording=filename=VirtualThreadHttpDemo.jfr,settings=./jfr-config/virtual-threads.jfc
 */
public class VirtualThreadHttpDemo {

    public static void main(String[] args) throws InterruptedException {
        // Optionally start JFR recording programmatically using JFRUtil.
        // JFRUtil.startVirtualThreadRecording("VirtualThreadHttpDemo");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        for (int i = 0; i < 5; i++) {
            int taskId = i;
            Thread.ofVirtual()
                    .name("VT-HttpTask-" + taskId)  // Custom name for easy identification in JMC
                    .start(() -> makeHttpCall(client, taskId));
        }

        // Wait to allow all tasks to complete
        Thread.sleep(15000); // increased slightly to ensure full capture
    }

    private static void makeHttpCall(HttpClient client, int taskId) {
        try {
            System.out.println("Starting HTTP request in " + Thread.currentThread()
                    + " (ID=" + Thread.currentThread().getId() + ")");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://httpbin.org/delay/2"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Task " + taskId + " completed. Response length: " + response.body().length());

            // Keep the virtual thread alive for JFR sampling
            System.out.println("Task " + taskId + " sleeping for 5 seconds to remain visible in JMC...");
            Thread.sleep(5000);  // Artificial delay to keep thread alive

            System.out.println("Task " + taskId + " finishing now.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
