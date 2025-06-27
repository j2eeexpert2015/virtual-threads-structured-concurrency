package com.example.dummyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

import static com.example.dummyapp.ServerConstants.*;

public class BackendForVT {

    private static final Logger logger = Logger.getLogger(BackendForVT.class.getName());

    private final int port;
    private final int delayMs;
    private final ExecutorService executor;
    private volatile boolean running = true;
    private final AtomicLong requestCount = new AtomicLong(0);

    public BackendForVT(int port, int delayMs) {
        this.port = port;
        this.delayMs = delayMs;
        // Virtual threads - unlimited concurrency
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        logger.info("Backend initialized with VIRTUAL THREADS");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info(String.format("Backend started on port %d | Delay: %dms", port, delayMs));

            while (running) {
                Socket clientSocket = serverSocket.accept();
                long reqId = requestCount.incrementAndGet();
                executor.submit(() -> handleClient(clientSocket, reqId));
            }
        }
    }

    private void handleClient(Socket clientSocket, long requestId) {
        long startTime = System.currentTimeMillis();
        String path = null;

        try (clientSocket;
             var input = clientSocket.getInputStream();
             var output = clientSocket.getOutputStream()) {

            // Read HTTP request with better error handling
            byte[] buffer = new byte[HTTP_BUFFER_SIZE];
            int bytesRead = input.read(buffer);

            // Handle empty or invalid requests
            if (bytesRead <= 0) {
                logger.warning("Empty request received");
                return;
            }

            String request = new String(buffer, 0, bytesRead);

            // Validate request has minimum required content
            if (request.trim().isEmpty() || !request.contains("HTTP")) {
                logger.warning("Invalid HTTP request");
                sendErrorResponse(output, 400, BAD_REQUEST_ERROR);
                return;
            }

            // Extract path from request line
            path = extractPath(request);
            String response = handleRequest(path);

            // Simulate processing delay
            Thread.sleep(delayMs);

            // Send HTTP response
            sendSuccessResponse(output, response);

            long duration = System.currentTimeMillis() - startTime;
            logger.info(String.format("REQ[%d] %s processed in %dms", requestId, path, duration));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning(String.format("Request[%d] interrupted: %s", requestId, e.getMessage()));
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.log(Level.SEVERE, String.format("Error handling request[%d] %s after %dms: %s",
                    requestId, path != null ? path : "unknown", duration, e.getMessage()), e);
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
            logger.warning("Error extracting path from request: " + e.getMessage());
        }
        return "/";
    }

    private String handleRequest(String path) {
        String response;

        if (path.startsWith("/auth")) {
            response = String.format(AUTH_RESPONSE_TEMPLATE, VIRTUAL_THREAD_TYPE);
            logger.fine("Auth endpoint called");
        } else if (path.startsWith("/permissions")) {
            response = String.format(PERMISSIONS_RESPONSE_TEMPLATE, VIRTUAL_THREAD_TYPE);
            logger.fine("Permissions endpoint called");
        } else if (path.startsWith("/data")) {
            response = String.format(DATA_RESPONSE_TEMPLATE, VIRTUAL_THREAD_TYPE);
            logger.fine("Data endpoint called");
        } else {
            response = String.format(ERROR_RESPONSE_TEMPLATE, NOT_FOUND_ERROR, VIRTUAL_THREAD_TYPE);
            logger.warning("Unknown endpoint requested: " + path);
        }

        return response;
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

        logger.warning(String.format("Sent error response: %d %s", statusCode, statusText));
    }

    public void stop() {
        running = false;
        executor.shutdown();
        logger.info(String.format("Backend stopped after processing %d requests", requestCount.get()));
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : BACKEND_VIRTUAL_PORT;
        int delay = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_BACKEND_DELAY_MS;

        // Set log level to INFO for minimal logging
        Logger.getLogger("com.example.dummyapp").setLevel(Level.INFO);

        BackendForVT backendServer = new BackendForVT(port, delay);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(backendServer::stop));

        try {
            backendServer.start();
        } catch (IOException e) {
            logger.severe("Failed to start backend server: " + e.getMessage());
            System.exit(1);
        }
    }
}