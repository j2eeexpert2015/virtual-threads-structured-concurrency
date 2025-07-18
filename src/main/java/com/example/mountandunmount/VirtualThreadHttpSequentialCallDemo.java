package com.example.mountandunmount;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Demonstrates creating and joining 5 virtual threads sequentially.
 *
 * To enable JFR recording for virtual threads, use:
 * -XX:StartFlightRecording=filename=VirtualThreadHttpSequentialCallDemo.jfr,settings=./jfr-config/virtual-threads.jfc
 */
public class VirtualThreadHttpSequentialCallDemo {

    public static void main(String[] args) throws InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // Create and join each virtual thread
        Thread vt1 = Thread.ofVirtual().name("VT-HttpTask-1")
                .start(() -> makeHttpCall(client, 1));
        vt1.join();
        System.out.println("Joined VT-HttpTask-1");

        Thread vt2 = Thread.ofVirtual().name("VT-HttpTask-2")
                .start(() -> makeHttpCall(client, 2));
        vt2.join();
        System.out.println("Joined VT-HttpTask-2");

        Thread vt3 = Thread.ofVirtual().name("VT-HttpTask-3")
                .start(() -> makeHttpCall(client, 3));
        vt3.join();
        System.out.println("Joined VT-HttpTask-3");

        Thread vt4 = Thread.ofVirtual().name("VT-HttpTask-4")
                .start(() -> makeHttpCall(client, 4));
        vt4.join();
        System.out.println("Joined VT-HttpTask-4");

        Thread vt5 = Thread.ofVirtual().name("VT-HttpTask-5")
                .start(() -> makeHttpCall(client, 5));
        vt5.join();
        System.out.println("Joined VT-HttpTask-5");

        System.out.println("All virtual threads completed.");
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

            // Sleep for better visibility in JFR
            System.out.println("Task " + taskId + " sleeping for 3 seconds to remain visible in JMC...");
            Thread.sleep(3000);

            System.out.println("Task " + taskId + " finishing now.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

