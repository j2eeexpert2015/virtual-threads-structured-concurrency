package com.example.dummyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.dummyapp.ServerConstants.*;

public class BackendWithVirtualThreads {

    private final int port;
    private final int delayMs;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public BackendWithVirtualThreads(int port, int delayMs) {
        this.port = port;
        this.delayMs = delayMs;
        // Virtual threads - unlimited concurrency
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        System.out.println("Backend using VIRTUAL THREADS (unlimited concurrency)");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Backend server started on port " + port + " with " + delayMs + "ms delay");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             var input = clientSocket.getInputStream();
             var output = clientSocket.getOutputStream()) {

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
                sendErrorResponse(output, 400, BAD_REQUEST_ERROR);
                return;
            }

            // Extract path from request line
            String path = extractPath(request);
            String response = handleRequest(path);

            // Simulate processing delay
            Thread.sleep(delayMs);

            // Send HTTP response
            sendSuccessResponse(output, response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Request interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private String extractPath(String request) {
        try {
            String[] lines = request.split("\r\n");
            if (lines.length > 0) {
                String[] parts = lines[0].split(" ");
                if (parts.length >= 2) {
                    return parts[1];
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting path from request: " + e.getMessage());
        }
        return "/";
    }

    private String handleRequest(String path) {
        if (path.startsWith("/auth")) {
            return String.format(AUTH_RESPONSE_TEMPLATE, VIRTUAL_THREAD_TYPE);
        } else if (path.startsWith("/permissions")) {
            return String.format(PERMISSIONS_RESPONSE_TEMPLATE, VIRTUAL_THREAD_TYPE);
        } else if (path.startsWith("/data")) {
            return String.format(DATA_RESPONSE_TEMPLATE, VIRTUAL_THREAD_TYPE);
        } else {
            return String.format(ERROR_RESPONSE_TEMPLATE, NOT_FOUND_ERROR, VIRTUAL_THREAD_TYPE);
        }
    }

    private void sendSuccessResponse(OutputStream output, String body) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        output.write(httpResponse.getBytes());
        output.flush();
    }

    private void sendErrorResponse(OutputStream output, int statusCode, String statusText) throws IOException {
        String body = String.format(ERROR_RESPONSE_TEMPLATE, statusText, VIRTUAL_THREAD_TYPE);
        String httpResponse = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: application/json\r\n" +
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
        int port = args.length > 0 ? Integer.parseInt(args[0]) : BACKEND_VIRTUAL_PORT;
        int delay = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_BACKEND_DELAY_MS;

        BackendWithVirtualThreads backendServer = new BackendWithVirtualThreads(port, delay);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(backendServer::stop));

        try {
            backendServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}