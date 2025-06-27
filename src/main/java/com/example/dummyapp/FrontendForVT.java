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
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

import static com.example.dummyapp.ServerConstants.*;

public class FrontendForVT {

    private static final Logger logger = Logger.getLogger(FrontendForVT.class.getName());

    private final int port;
    private final ExecutorService executor;
    private final HttpClient httpClient;
    private final String backendUrl;
    private volatile boolean running = true;
    private final AtomicLong requestCount = new AtomicLong(0);

    public FrontendForVT(int port, String backendUrl) {
        this.port = port;
        this.backendUrl = backendUrl;

        // Always use Virtual Threads
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .connectTimeout(Duration.ofSeconds(HTTP_CONNECT_TIMEOUT_SECONDS))
                .build();

        logger.info("Frontend initialized with VIRTUAL THREADS");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info(String.format("Server started on port %d | Backend: %s", port, backendUrl));

            while (running) {
                Socket clientSocket = serverSocket.accept();
                long reqId = requestCount.incrementAndGet();
                executor.submit(() -> handleClient(clientSocket, reqId));
            }
        }
    }

    private void handleClient(Socket clientSocket, long requestId) {
        try (clientSocket;
             InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {

            byte[] buffer = new byte[HTTP_BUFFER_SIZE];
            int bytesRead = input.read(buffer);

            if (bytesRead <= 0) {
                logger.warning("Empty request received");
                return;
            }

            String request = new String(buffer, 0, bytesRead);

            if (request.trim().isEmpty() || !request.contains("HTTP")) {
                logger.warning("Invalid HTTP request");
                sendHttpResponse(output, 400,
                        String.format(ERROR_RESPONSE_TEMPLATE, BAD_REQUEST_ERROR, VIRTUAL_THREAD_TYPE),
                        "application/json");
                return;
            }

            if (request.contains("GET /api/process")) {
                long startTime = System.currentTimeMillis();
                String response = processRequest();
                long duration = System.currentTimeMillis() - startTime;

                logger.info(String.format("REQ[%d] processed in %dms", requestId, duration));
                sendHttpResponse(output, 200, response, "application/json");
            } else {
                String errorResponse = String.format(ERROR_RESPONSE_TEMPLATE, "Endpoint not found", VIRTUAL_THREAD_TYPE);
                sendHttpResponse(output, 404, errorResponse, "application/json");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Error handling request[%d]: %s", requestId, e.getMessage()), e);
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
            logger.severe("Backend processing failed: " + e.getMessage());
            return String.format(
                    "{\"status\":\"error\",\"message\":\"%s\",\"threadType\":\"%s\"}",
                    e.getMessage(), VIRTUAL_THREAD_TYPE
            );
        }
    }

    private String callBackend(String endpoint) throws Exception {
        long start = System.currentTimeMillis();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .timeout(Duration.ofSeconds(HTTP_REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        long duration = System.currentTimeMillis() - start;

        if (response.statusCode() != 200) {
            logger.warning(String.format("Backend %s failed: %d (%dms)", endpoint, response.statusCode(), duration));
            throw new RuntimeException("Backend call failed: " + response.statusCode());
        }

        logger.fine(String.format("Backend %s: %dms", endpoint, duration));
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
        logger.info("Server stopped");
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : FRONTEND_VIRTUAL_PORT;
        String backendUrl = args.length > 1 ? args[1] : BACKEND_VIRTUAL_URL;

        // Set log level to INFO for minimal logging
        Logger.getLogger("com.example.dummyapp").setLevel(Level.INFO);

        FrontendForVT frontendServer = new FrontendForVT(port, backendUrl);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(frontendServer::stop));

        try {
            frontendServer.start();
        } catch (IOException e) {
            logger.severe("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}