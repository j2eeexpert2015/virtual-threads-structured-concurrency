package com.example.dummyapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrontendServer {

    private final int port;
    private final ExecutorService executor;
    private final HttpClient httpClient;
    private final String backendUrl;
    private volatile boolean running = true;

    public FrontendServer(int port, String threadType, String backendUrl, int platformThreads) {
        this.port = port;
        this.backendUrl = backendUrl;

        if ("platform".equals(threadType)) {
            this.executor = Executors.newFixedThreadPool(platformThreads);
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            System.out.println("Using Platform Threads (pool size: " + platformThreads + ")");
        } else {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
            this.httpClient = HttpClient.newBuilder()
                    .executor(Executors.newVirtualThreadPerTaskExecutor())
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            System.out.println("Using Virtual Threads");
        }
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Frontend server started on port " + port);
            System.out.println("Backend URL: " + backendUrl);
            System.out.println("Test with: curl http://localhost:" + port + "/api/process");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {

            // Read HTTP request
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            String request = new String(buffer, 0, bytesRead);

            // Check if it's our API endpoint
            if (request.contains("GET /api/process")) {
                String response = processRequest();
                sendHttpResponse(output, 200, response, "application/json");
            } else {
                String errorResponse = "{\"error\":\"Endpoint not found\"}";
                sendHttpResponse(output, 404, errorResponse, "application/json");
            }

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private String processRequest() {
        try {
            long startTime = System.currentTimeMillis();

            // Make 3 sequential calls to backend services
            String authResponse = callBackend("/auth");
            String permResponse = callBackend("/permissions");
            String dataResponse = callBackend("/data");

            long totalTime = System.currentTimeMillis() - startTime;

            return String.format(
                    "{\"status\":\"success\",\"totalTime\":%d,\"auth\":%s,\"permissions\":%s,\"data\":%s}",
                    totalTime, authResponse, permResponse, dataResponse
            );

        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private String callBackend(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Backend call failed: " + response.statusCode());
        }

        return response.body();
    }

    private void sendHttpResponse(OutputStream output, int statusCode, String body, String contentType) throws IOException {
        String statusText = statusCode == 200 ? "OK" : "Not Found";
        String httpResponse = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        output.write(httpResponse.getBytes());
        output.flush();
    }

    public void stop() {
        running = false;
        executor.shutdown();
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9000;
        String threadType = args.length > 1 ? args[1] : "virtual"; // "platform" or "virtual"
        String backendUrl = args.length > 2 ? args[2] : "http://localhost:8080";
        int platformThreads = args.length > 3 ? Integer.parseInt(args[3]) : 1000;

        FrontendServer frontendServer = new FrontendServer(port, threadType, backendUrl, platformThreads);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(frontendServer::stop));

        try {
            frontendServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}