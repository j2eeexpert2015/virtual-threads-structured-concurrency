package com.example.dummyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.dummyapp.ServerConstants.PLATFORM_THREAD_POOL_SIZE;

public class BackendForPT {

    private final int port;
    private final int delayMs;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public BackendForPT(int port, int delayMs) {
        this.port = port;
        this.delayMs = delayMs;
        // Platform threads with fixed thread pool
        this.executor = Executors.newFixedThreadPool(100);
        System.out.println("Backend using PLATFORM THREADS (pool size: 100)");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[BACKEND] Server started on port " + port + " with " + delayMs + "ms delay");
            System.out.println("[BACKEND] Using PLATFORM THREADS (pool size: " + PLATFORM_THREAD_POOL_SIZE + ")");
            System.out.println("[BACKEND] Ready to accept connections...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[BACKEND] Accepted connection from " + clientSocket.getRemoteSocketAddress());
                    executor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[BACKEND] Accept error: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             var input = clientSocket.getInputStream();
             var output = clientSocket.getOutputStream()) {

            // Read HTTP request with better error handling
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);

            // Handle empty or invalid requests
            if (bytesRead <= 0) {
                System.err.println("Warning: No data received from client");
                return;
            }

            String request = new String(buffer, 0, bytesRead);

            // Validate request has minimum required content
            if (request.trim().isEmpty() || !request.contains("HTTP")) {
                System.err.println("Warning: Invalid HTTP request received");
                sendErrorResponse(output, 400, "Bad Request");
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
            return "{\"userId\":\"user123\",\"status\":\"authenticated\",\"threadType\":\"platform\"}";
        } else if (path.startsWith("/permissions")) {
            return "{\"canAccess\":true,\"roles\":[\"user\"],\"threadType\":\"platform\"}";
        } else if (path.startsWith("/data")) {
            return "{\"result\":\"success\",\"data\":[1,2,3,4,5],\"threadType\":\"platform\"}";
        } else {
            return "{\"error\":\"Not found\",\"threadType\":\"platform\"}";
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
        String body = "{\"error\":\"" + statusText + "\",\"threadType\":\"platform\"}";
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
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int delay = args.length > 1 ? Integer.parseInt(args[1]) : 333;

        BackendForPT backendServer = new BackendForPT(port, delay);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(backendServer::stop));

        try {
            backendServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}