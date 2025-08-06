package com.example.structuredconcurrency;

/**
 * Common utility class for simulating task delays and checking cancellation.
 * Used by both structured and unstructured concurrency implementations
 * to ensure fair comparison.
 */
public class TaskSimulationUtil {

    // Standard delays for different services
    public static final int PRODUCT_DETAILS_DELAY_MS = 3000;
    public static final int INVENTORY_STATUS_DELAY_MS = 2000;
    public static final int INVENTORY_FAILURE_DELAY_MS = 1000;
    public static final int CUSTOMER_REVIEWS_DELAY_MS = 4000;
    public static final int CUSTOMER_REVIEWS_SLOW_DELAY_MS = 10000;
    public static final int CANCELLATION_CHECK_INTERVAL_MS = 100;

    /**
     * Simulates a delay with periodic cancellation checks.
     * Checks for thread interruption every 100ms to be responsive to cancellation.
     *
     * @param taskName Name of the task for logging
     * @param delayMs Total delay in milliseconds
     * @param logPrefix Prefix for log messages (e.g., "[STRUCTURED]" or "[UNSTRUCTURED]")
     * @return true if completed successfully, false if cancelled
     */
    public static boolean simulateDelayWithCancellationCheck(String taskName, int delayMs, String logPrefix) {
        int checkInterval = CANCELLATION_CHECK_INTERVAL_MS;
        int iterations = delayMs / checkInterval;

        try {
            for (int i = 0; i < iterations; i++) {
                // Check if thread has been interrupted
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println(logPrefix + " " + taskName + " detected cancellation signal!");
                    return false;
                }
                Thread.sleep(checkInterval);
            }

            // Sleep for any remaining time
            int remaining = delayMs % checkInterval;
            if (remaining > 0) {
                Thread.sleep(remaining);
            }
            return true;

        } catch (InterruptedException e) {
            System.out.println(logPrefix + " " + taskName + " was interrupted!");
            Thread.currentThread().interrupt(); // Preserve interrupt status
            return false;
        }
    }

    /**
     * Simple delay without cancellation checks (for demonstrating unresponsive behavior).
     *
     * @param taskName Name of the task for logging
     * @param delayMs Delay in milliseconds
     * @param logPrefix Prefix for log messages
     * @return true if completed, false if interrupted
     */
    public static boolean simulateSimpleDelay(String taskName, int delayMs, String logPrefix) {
        try {
            Thread.sleep(delayMs);
            return true;
        } catch (InterruptedException e) {
            System.out.println(logPrefix + " " + taskName + " was interrupted!");
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
