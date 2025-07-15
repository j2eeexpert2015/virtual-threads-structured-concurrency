package com.example.util;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordingFile;
import jdk.jfr.consumer.RecordedEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing JFR (Java Flight Recorder) recordings
 * Provides simple methods to start, stop, and analyze JFR recordings
 * with focus on virtual thread monitoring.
 */
public class JFRUtil {

    private static final String DEFAULT_OUTPUT_DIR = "jfr-recordings";
    private static Recording currentRecording;

    /**
     * Starts a JFR recording optimized for virtual thread monitoring
     *
     * @return the started Recording instance
     */
    public static Recording startVirtualThreadRecording() {
        return startVirtualThreadRecording("VirtualThreadDemo");
    }

    /**
     * Starts a JFR recording with a custom name
     *
     * @param recordingName base name for the recording
     * @return the started Recording instance
     */
    public static Recording startVirtualThreadRecording(String recordingName) {
        if (currentRecording != null) {
            System.out.println("⚠️ JFR recording already running, stopping previous recording");
            stopRecording();
        }

        currentRecording = createVirtualThreadRecording(recordingName);
        currentRecording.start();

        System.out.println("🚀 JFR recording started: " + currentRecording.getName());
        return currentRecording;
    }

    /**
     * Stops the current recording and saves it to file
     *
     * @return Path to the saved JFR file, or null if no recording was active
     */
    public static Path stopRecording() {
        return stopRecording(DEFAULT_OUTPUT_DIR);
    }

    /**
     * Stops the current recording and saves it to specified directory
     *
     * @param outputDir directory to save the JFR file
     * @return Path to the saved JFR file, or null if no recording was active
     */
    public static Path stopRecording(String outputDir) {
        if (currentRecording == null) {
            System.out.println("⚠️ No active JFR recording to stop");
            return null;
        }

        try {
            currentRecording.stop();
            Path savedFile = saveRecording(currentRecording, outputDir);
            currentRecording.close();
            currentRecording = null;

            System.out.println("⏹️ JFR recording stopped and saved to: " + savedFile.toAbsolutePath());
            return savedFile;

        } catch (IOException e) {
            System.err.println("❌ Error saving JFR recording: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convenience method: start recording, execute code, stop and analyze
     *
     * @param recordingName name for the recording
     * @param codeToExecute code to run while recording
     * @return Path to the saved JFR file
     */
    public static Path recordAndAnalyze(String recordingName, Runnable codeToExecute) {
        startVirtualThreadRecording(recordingName);

        try {
            codeToExecute.run();
        } finally {
            Path jfrFile = stopRecording();
            if (jfrFile != null) {
                analyzeRecording(jfrFile);
            }
            return jfrFile;
        }
    }

    /**
     * Analyzes a JFR recording file and prints virtual thread statistics
     *
     * @param jfrFile path to the JFR file
     */
    public static void analyzeRecording(Path jfrFile) {
        System.out.println("\n📊 Analyzing JFR Recording: " + jfrFile.getFileName());
        System.out.println("================================");

        Map<String, Integer> eventCounts = new HashMap<>();

        try (RecordingFile recordingFile = new RecordingFile(jfrFile)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent event = recordingFile.readEvent();
                String eventType = event.getEventType().getName();

                eventCounts.merge(eventType, 1, Integer::sum);

                // Print detailed info for virtual thread events
                if (eventType.contains("VirtualThread")) {
                    printVirtualThreadEvent(event);
                }
            }

            printSummary(eventCounts);

        } catch (IOException e) {
            System.err.println("❌ Error analyzing JFR file: " + e.getMessage());
        }
    }

    /**
     * Creates a JFR recording configured for virtual thread monitoring
     */
    private static Recording createVirtualThreadRecording(String baseName) {
        Recording recording = new Recording();

        // Virtual thread lifecycle events
        recording.enable("jdk.VirtualThreadStart")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        recording.enable("jdk.VirtualThreadEnd")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        // Virtual thread specific events (if available)
        recording.enable("jdk.VirtualThreadPinned")
                .withStackTrace()
                .withThreshold(Duration.ofMillis(1));

        recording.enable("jdk.VirtualThreadSubmitFailed")
                .withStackTrace();

        // General thread events for comparison
        recording.enable("jdk.ThreadStart").withStackTrace();
        recording.enable("jdk.ThreadEnd").withStackTrace();
        recording.enable("jdk.ThreadSleep").withThreshold(Duration.ofMillis(1));

        // Monitor synchronization events
        recording.enable("jdk.JavaMonitorEnter").withThreshold(Duration.ofMillis(1));
        recording.enable("jdk.JavaMonitorWait").withThreshold(Duration.ofMillis(1));

        recording.setMaxAge(Duration.ofMinutes(10));
        recording.setName(baseName + "-" + Instant.now().getEpochSecond());

        return recording;
    }

    /**
     * Saves the recording to a file
     */
    private static Path saveRecording(Recording recording, String outputDir) throws IOException {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String filename = String.format("%s-%s.jfr",
                recording.getName().replaceAll("[^a-zA-Z0-9-]", ""),
                timestamp);

        Path outputPath = Paths.get(outputDir, filename);
        outputPath.getParent().toFile().mkdirs();

        recording.dump(outputPath);
        return outputPath;
    }

    /**
     * Prints detailed information about virtual thread events
     */
    private static void printVirtualThreadEvent(RecordedEvent event) {
        String eventType = event.getEventType().getName();
        String threadName = event.getThread() != null ? event.getThread().getJavaName() : "Unknown";
        long threadId = event.getThread() != null ? event.getThread().getJavaThreadId() : -1;

        String emoji = getEventEmoji(eventType);
        System.out.printf("%s %s: %s (ID: %d)%n", emoji, eventType, threadName, threadId);
    }

    /**
     * Returns appropriate emoji for event types
     */
    private static String getEventEmoji(String eventType) {
        return switch (eventType) {
            case "jdk.VirtualThreadStart" -> "🟢";
            case "jdk.VirtualThreadEnd" -> "🔴";
            case "jdk.VirtualThreadPinned" -> "📌";
            case "jdk.VirtualThreadSubmitFailed" -> "❌";
            default -> "🧵";
        };
    }

    /**
     * Prints summary statistics from the recording
     */
    private static void printSummary(Map<String, Integer> eventCounts) {
        System.out.println("\n📈 Event Summary:");
        System.out.println("==================");

        eventCounts.entrySet().stream()
                .filter(entry -> entry.getKey().contains("Thread"))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("%-30s: %d%n", entry.getKey(), entry.getValue()));

        // Calculate virtual thread statistics
        int virtualStarted = eventCounts.getOrDefault("jdk.VirtualThreadStart", 0);
        int virtualEnded = eventCounts.getOrDefault("jdk.VirtualThreadEnd", 0);
        int platformStarted = eventCounts.getOrDefault("jdk.ThreadStart", 0) - virtualStarted;

        System.out.println("\n🎯 Key Metrics:");
        System.out.println("================");
        System.out.printf("Virtual Threads Created: %d%n", virtualStarted);
        System.out.printf("Virtual Threads Completed: %d%n", virtualEnded);
        System.out.printf("Platform Threads Created: %d%n", Math.max(0, platformStarted));

        if (virtualStarted > 0) {
            double completionRate = (double) virtualEnded / virtualStarted * 100;
            System.out.printf("Completion Rate: %.1f%%%n", completionRate);
        }
    }

    /**
     * Prints detailed carrier thread analysis
     */
    private static void printCarrierThreadAnalysis(Map<String, CarrierThreadInfo> carrierStats) {
        if (carrierStats.isEmpty()) {
            System.out.println("\n🚛 No carrier thread information found");
            return;
        }

        System.out.println("\n🚛 Carrier Thread Analysis:");
        System.out.println("============================");

        carrierStats.values().stream()
                .sorted((a, b) -> Integer.compare(b.virtualThreadsHosted, a.virtualThreadsHosted))
                .forEach(carrier -> {
                    System.out.printf("🔧 Carrier: %s (ID: %d)%n", carrier.name, carrier.id);
                    System.out.printf("   └─ Virtual Threads Hosted: %d%n", carrier.virtualThreadsHosted);
                    System.out.printf("   └─ Total Events: %d%n", carrier.totalEvents);
                });

        int totalCarriers = carrierStats.size();
        int totalVirtualThreadsHosted = carrierStats.values().stream()
                .mapToInt(c -> c.virtualThreadsHosted)
                .sum();

        System.out.printf("\n📈 Carrier Summary:%n");
        System.out.printf("Total Carrier Threads: %d%n", totalCarriers);
        System.out.printf("Average VTs per Carrier: %.1f%n",
                totalCarriers > 0 ? (double) totalVirtualThreadsHosted / totalCarriers : 0);
    }

    /**
     * Helper class to track carrier thread information
     */
    private static class CarrierThreadInfo {
        final String name;
        final long id;
        int virtualThreadsHosted = 0;
        int totalEvents = 0;

        CarrierThreadInfo(String name, long id) {
            this.name = name;
            this.id = id;
        }
    }

    /**
     * Gets the current active recording
     */
    public static Recording getCurrentRecording() {
        return currentRecording;
    }

    /**
     * Checks if a recording is currently active
     */
    public static boolean isRecording() {
        return currentRecording != null;
    }
}