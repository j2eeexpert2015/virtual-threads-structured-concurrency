package com.example.dummyapp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.example.dummyapp.ServerConstants.*;

public class ServerHealthChecker {
    /*

    public static void main(String[] args) {
        System.out.println("=== Server Health Check ===\n");

        // Test services by making actual HTTP requests instead of socket connections
        System.out.println("--- Testing Services ---");

        boolean backendPlatform = testEndpoint(BACKEND_PLATFORM_URL + "/auth", "Backend Platform");
        boolean backendVirtual = testEndpoint(BACKEND_VIRTUAL_URL + "/auth", "Backend Virtual");
        boolean frontend = testEndpoint("http://localhost:" + FRONTEND_PORT + "/api/process", "Frontend Server");

        // Enhanced summary
        System.out.println("\n--- Summary ---");
        System.out.println((backendPlatform ? "✓" : "✗") + " Backend Platform (" + BACKEND_PLATFORM_PORT + "): " + (backendPlatform ? "Running" : "Down"));
        System.out.println((backendVirtual ? "✓" : "✗") + " Backend Virtual (" + BACKEND_VIRTUAL_PORT + "): " + (backendVirtual ? "Running" : "Down"));
        System.out.println((frontend ? "✓" : "✗") + " Frontend Server (" + FRONTEND_PORT + "): " + (frontend ? "Running" : "Down"));

        // Status evaluation
        int totalRunning = (backendPlatform ? 1 : 0) + (backendVirtual ? 1 : 0) + (frontend ? 1 : 0);
        boolean hasBackend = backendPlatform || backendVirtual;

        if (frontend && hasBackend) {
            System.out.println("\n🎉 System ready! Frontend can connect to available backend(s).");
            System.out.println("Test the system: curl http://localhost:" + FRONTEND_PORT + "/api/process");
        } else if (frontend && !hasBackend) {
            System.out.println("\n⚠ Frontend running but no backends available. Frontend will return errors.");
        } else if (!frontend && hasBackend) {
            System.out.println("\n⚠ Backend(s) running but frontend is down.");
        } else {
            System.out.println("\n❌ No services running. Check your setup.");
        }

        if (totalRunning < 2) {
            printStartupCommands();
        }
    }

    private static boolean testEndpoint(String url, String name) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(HEALTH_CHECK_CONNECT_TIMEOUT_SECONDS))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(HEALTH_CHECK_REQUEST_TIMEOUT_SECONDS))
                    .GET()
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long time = System.currentTimeMillis() - start;

            boolean success = response.statusCode() == 200;
            String status = success ? "✓" : "✗";
            System.out.printf("%s %s: HTTP %d (%dms)%n", status, name, response.statusCode(), time);

            // Show detailed response information
            if (success) {
                String body = response.body();

                // Show thread type from response if available
                if (body.contains("\"threadType\"")) {
                    String threadType = body.contains(PLATFORM_THREAD_TYPE) ? "[Platform]" : "[Virtual]";
                    System.out.printf("    %s Thread type confirmed%n", threadType);
                }

                // Show response content summary
                if (body.length() > 100) {
                    System.out.printf("    Response: %s...%n", body.substring(0, 100));
                } else {
                    System.out.printf("    Response: %s%n", body);
                }

                // For frontend, show backend call timing if available
                if (name.contains("Frontend") && body.contains("\"totalTime\"")) {
                    try {
                        String totalTimeStr = body.replaceAll(".*\"totalTime\":(\\d+).*", "$1");
                        System.out.printf("    Backend calls took: %sms%n", totalTimeStr);
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }

                // Show response size
                System.out.printf("    Response size: %d bytes%n", body.length());

            } else {
                System.out.printf("    Error: HTTP %d response%n", response.statusCode());
                String body = response.body();
                if (body != null && !body.trim().isEmpty()) {
                    if (body.length() > 50) {
                        System.out.printf("    Error details: %s...%n", body.substring(0, 50));
                    } else {
                        System.out.printf("    Error details: %s%n", body);
                    }
                }
            }

            return success;

        } catch (Exception e) {
            System.out.printf("✗ %s: Not running%n", name);
            System.out.printf("    Connection error: %s%n", e.getMessage());
            return false;
        }
    }

    private static void printStartupCommands() {
        System.out.println("\nQuick start commands:");
        System.out.println("Backend Platform:    java --enable-preview -cp build com.example.dummyapp.BackendServerWithPlatformThreads " + BACKEND_PLATFORM_PORT + " " + DEFAULT_BACKEND_DELAY_MS);
        System.out.println("Backend Virtual:     java --enable-preview -cp build com.example.dummyapp.BackendServerWithVirtualThreads " + BACKEND_VIRTUAL_PORT + " " + DEFAULT_BACKEND_DELAY_MS);
        System.out.println("Frontend (Platform): java --enable-preview -cp build com.example.dummyapp.FrontendServer " + FRONTEND_PORT + " " + PLATFORM_THREAD_TYPE + " " + BACKEND_PLATFORM_URL + " " + DEFAULT_PLATFORM_THREAD_POOL_SIZE);
        System.out.println("Frontend (Virtual):  java --enable-preview -cp build com.example.dummyapp.FrontendServer " + FRONTEND_PORT + " " + VIRTUAL_THREAD_TYPE + " " + BACKEND_VIRTUAL_URL);
    }


     */
}