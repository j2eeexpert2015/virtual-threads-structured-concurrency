package com.example.dummyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendServer {

    private final int port;
    private final int delayMs;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public BackendServer(int port, int delayMs) {
        this.port = port;
        this.delayMs = delayMs;
        this.executor = Executors.newCachedThreadPool();
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

            // Read HTTP request (simplified)
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            String request = new String(buffer, 0, bytesRead);

            // Extract path from request line
            String path = extractPath(request);
            String response = handleRequest(path);

            // Simulate processing delay
            Thread.sleep(delayMs);

            // Send HTTP response
            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + response.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    response;

            output.write(httpResponse.getBytes());
            output.flush();

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private String extractPath(String request) {
        String[] lines = request.split("\r\n");
        if (lines.length > 0) {
            String[] parts = lines[0].split(" ");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return "/";
    }

    private String handleRequest(String path) {
        if (path.startsWith("/auth")) {
            return "{\"userId\":\"user123\",\"status\":\"authenticated\"}";
        } else if (path.startsWith("/permissions")) {
            return "{\"canAccess\":true,\"roles\":[\"user\"]}";
        } else if (path.startsWith("/data")) {
            return "{\"result\":\"success\",\"data\":[1,2,3,4,5]}";
        } else {
            return "{\"error\":\"Not found\"}";
        }
    }

    public void stop() {
        running = false;
        executor.shutdown();
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int delay = args.length > 1 ? Integer.parseInt(args[1]) : 333;

        BackendServer backendServer = new BackendServer(port, delay);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(backendServer::stop));

        try {
            backendServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}