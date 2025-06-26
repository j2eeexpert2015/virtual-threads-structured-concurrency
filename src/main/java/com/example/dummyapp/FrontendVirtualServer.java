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

import static com.example.dummyapp.ServerConstants.*;

public class FrontendVirtualServer {

    private final int port;
    private final ExecutorService executor;
    private final HttpClient httpClient;
    private final String backendUrl;
    private volatile boolean running = true;

    public FrontendVirtualServer(int port, String backendUrl) {
        this.port = port;
        this.backendUrl = backendUrl;

        // Always use Virtual Threads
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .connectTimeout(Duration.ofSeconds(HTTP_CONNECT_TIMEOUT_SECONDS))
                .build();

        System.out.println("Frontend using VIRTUAL THREADS (unlimited concurrency)");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Virtual Frontend server started on port " + port);
            System.out.println("Backend URL: " + backendUrl);
            System.out.println("Thread Type: " + VIRTUAL_THREAD_TYPE);
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

            // Read HTTP request with better error handling
            byte[] buffer = new byte[HTTP_BUFFER_SIZE];
            int bytesRead = input.read(buffer);

            // Handle empty or invalid requests
            if (bytesRead <= 0) {
                System.err.println(NO_DATA_WARNING);
                return;
            }

            String request = new String(buffer, 0, bytesRead);

            // Validate request has minimum required content
            if (request.trim().isEmpty() || !request.contains("HTTP")) {
                System.err.println(INVALID_HTTP_WARNING);
                sendHttpResponse(output, 400,
                        String.format(ERROR_RESPONSE_TEMPLATE, BAD_REQUEST_ERROR, VIRTUAL_THREAD_TYPE),
                        "application/json");
                return;
            }

            // Check if it's our API endpoint
            if (request.contains("GET /api/process")) {
                String response = processRequest();
                sendHttpResponse(output, 200, response, "application/json");
            } else {
                String errorResponse = String.format(ERROR_RESPONSE_TEMPLATE, "Endpoint not found", VIRTUAL_THREAD_TYPE);
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
                    "{\"status\":\"success\",\"totalTime\":%d,\"threadType\":\"%s\",\"auth\":%s,\"permissions\":%s,\"data\":%s}",
                    totalTime, VIRTUAL_THREAD_TYPE, authResponse, permResponse, dataResponse
            );

        } catch (Exception e) {
            return String.format(
                    "{\"status\":\"error\",\"message\":\"%s\",\"threadType\":\"%s\"}",
                    e.getMessage(), VIRTUAL_THREAD_TYPE
            );
        }
    }

    private String callBackend(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .timeout(Duration.ofSeconds(HTTP_REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Backend call failed: " + response.statusCode());
        }

        return response.body();
    }

    private void sendHttpResponse(OutputStream output, int statusCode, String body, String contentType) throws IOException {
        String statusText = statusCode == 200 ? "OK" :
                statusCode == 404 ? "Not Found" :
                        statusCode == 400 ? "Bad Request" : "Error";

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
        int port = args.length > 0 ? Integer.parseInt(args[0]) : FRONTEND_PORT;
        String backendUrl = args.length > 1 ? args[1] : BACKEND_VIRTUAL_URL;

        System.out.println("=== Virtual Frontend Server Configuration ===");
        System.out.println("Port: " + port);
        System.out.println("Thread Type: " + VIRTUAL_THREAD_TYPE);
        System.out.println("Backend URL: " + backendUrl);
        System.out.println("==============================================\n");

        FrontendVirtualServer frontendServer = new FrontendVirtualServer(port, backendUrl);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(frontendServer::stop));

        try {
            frontendServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start Virtual Frontend server: " + e.getMessage());
        }
    }
}