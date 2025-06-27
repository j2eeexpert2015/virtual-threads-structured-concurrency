package com.example.dummyapp;

/**
 * Centralized configuration constants for all server components.
 * Modify values here to change ports, delays, and other settings across all services.
 */
public final class ServerConstants {

    // Prevent instantiation
    private ServerConstants() {}

    // === PORT CONFIGURATION ===
    public static final int BACKEND_PLATFORM_PORT = 8080;
    public static final int BACKEND_VIRTUAL_PORT = 8081;
    public static final int FRONTEND_PLATFORM_PORT = 9000;
    public static final int FRONTEND_VIRTUAL_PORT = 9001;

    // === BACKEND CONFIGURATION ===
    public static final int DEFAULT_BACKEND_DELAY_MS = 333;
    public static final int PLATFORM_THREAD_POOL_SIZE = 100;

    // === FRONTEND CONFIGURATION ===
    public static final int DEFAULT_PLATFORM_THREAD_POOL_SIZE = 1000;
    public static final String BACKEND_PLATFORM_URL = "http://localhost:" + BACKEND_PLATFORM_PORT;
    public static final String BACKEND_VIRTUAL_URL = "http://localhost:" + BACKEND_VIRTUAL_PORT;

    // === HTTP CLIENT CONFIGURATION ===
    public static final int HTTP_CONNECT_TIMEOUT_SECONDS = 5;
    public static final int HTTP_REQUEST_TIMEOUT_SECONDS = 10;

    // === HEALTH CHECK CONFIGURATION ===
    public static final int HEALTH_CHECK_CONNECT_TIMEOUT_SECONDS = 3;
    public static final int HEALTH_CHECK_REQUEST_TIMEOUT_SECONDS = 5;

    // === RESPONSE CONFIGURATION ===
    public static final String PLATFORM_THREAD_TYPE = "platform";
    public static final String VIRTUAL_THREAD_TYPE = "virtual";

    // === BUFFER SIZES ===
    public static final int HTTP_BUFFER_SIZE = 1024;

    // === ERROR MESSAGES ===
    public static final String NO_DATA_WARNING = "Warning: No data received from client";
    public static final String INVALID_HTTP_WARNING = "Warning: Invalid HTTP request received";
    public static final String BAD_REQUEST_ERROR = "Bad Request";
    public static final String NOT_FOUND_ERROR = "Not found";

    // === JSON RESPONSE TEMPLATES ===
    public static final String AUTH_RESPONSE_TEMPLATE =
            "{\"userId\":\"user123\",\"status\":\"authenticated\",\"threadType\":\"%s\"}";

    public static final String PERMISSIONS_RESPONSE_TEMPLATE =
            "{\"canAccess\":true,\"roles\":[\"user\"],\"threadType\":\"%s\"}";

    public static final String DATA_RESPONSE_TEMPLATE =
            "{\"result\":\"success\",\"data\":[1,2,3,4,5],\"threadType\":\"%s\"}";

    public static final String ERROR_RESPONSE_TEMPLATE =
            "{\"error\":\"%s\",\"threadType\":\"%s\"}";
}