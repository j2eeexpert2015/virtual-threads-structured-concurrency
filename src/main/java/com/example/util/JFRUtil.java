package com.example.util;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordingFile;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedThread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Utility class for managing JFR (Java Flight Recorder) recordings.
 * Provides methods for starting, stopping, and analyzing JFR recordings
 * with a strong focus on virtual thread events.
 */
public class JFRUtil {

    // Default configurations (legacy behavior)
    private static final String DEFAULT_OUTPUT_DIR = "jfr-recordings";
    private static final boolean DEFAULT_CLEANUP_ON_START = true;
    private static final String DEFAULT_RECORDING_NAME = "VirtualThreadDemo";

    private static Recording currentRecording;

    // Instance fields for builder-based usage
    private final String outputDir;
    private final boolean cleanupOnStart;
    private final String recordingName;

    // Private constructor to enforce builder usage
    private JFRUtil(Builder builder) {
        this.outputDir = builder.outputDir;
        this.cleanupOnStart = builder.cleanupOnStart;
        this.recordingName = builder.recordingName;
    }

    // ======================
    // Legacy Static Methods
    // ======================

    public static Recording startVirtualThreadRecording() {
        return startVirtualThreadRecording(DEFAULT_RECORDING_NAME);
    }

    public static Recording startVirtualThreadRecording(String recordingName) {
        if (DEFAULT_CLEANUP_ON_START) {
            cleanupJfrDirectory(DEFAULT_OUTPUT_DIR);
        }

        if (currentRecording != null) {
            System.out.println("⚠️ JFR recording already running, stopping previous recording...");
            stopRecording();
        }

        currentRecording = createVirtualThreadRecording(recordingName);
        currentRecording.start();

        System.out.println("🚀 JFR recording started: " + currentRecording.getName());
        return currentRecording;
    }

    public static Path stopRecording() {
        return stopRecording(DEFAULT_OUTPUT_DIR);
    }

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

    // ======================
    // Builder-Based Usage
    // ======================

    public void start() {
        if (cleanupOnStart) {
            cleanupJfrDirectory(outputDir);
        }

        if (currentRecording != null) {
            System.out.println("⚠️ JFR recording already running, stopping previous recording...");
            stop();
        }

        currentRecording = createVirtualThreadRecording(recordingName);
        currentRecording.start();

        System.out.println("🚀 JFR recording started: " + currentRecording.getName());
    }

    public Path stop() {
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

    public Path recordAndAnalyze(Runnable codeToExecute) {
        start();
        try {
            codeToExecute.run();
        } finally {
            Path jfrFile = stop();
            if (jfrFile != null) {
                analyzeRecording(jfrFile);
            }
            return jfrFile;
        }
    }

    // ======================
    // ANALYSIS METHODS
    // ======================

    public static void analyzeRecording(Path jfrFile) {
        System.out.println("\n📊 Analyzing JFR Recording: " + jfrFile.getFileName());
        System.out.println("================================");

        Map<String, Integer> eventCounts = new HashMap<>();

        try (RecordingFile recordingFile = new RecordingFile(jfrFile)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent event = recordingFile.readEvent();
                String eventType = event.getEventType().getName();
                eventCounts.merge(eventType, 1, Integer::sum);
            }
        } catch (IOException e) {
            System.err.println("❌ Error analyzing JFR file: " + e.getMessage());
            return;
        }

        printSummary(eventCounts);
        analyzePinnedEvents(jfrFile);
    }

    private static void analyzePinnedEvents(Path jfrFile) {
        System.out.println("\n📌 Detailed Virtual Thread Pinning Report");
        System.out.println("=========================================");

        Map<String, List<String>> carrierToPinnedEvents = new LinkedHashMap<>();

        try (RecordingFile recordingFile = new RecordingFile(jfrFile)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent event = recordingFile.readEvent();
                if (!"jdk.VirtualThreadPinned".equals(event.getEventType().getName())) {
                    continue;
                }

                RecordedThread carrier = event.getThread();
                String carrierName = carrier != null
                        ? carrier.getJavaName() + " (ID: " + carrier.getJavaThreadId() + ")"
                        : "Unknown Carrier";

                StringBuilder eventDetails = new StringBuilder();
                eventDetails.append("Pinned Virtual Thread Event")
                        .append("\n  Timestamp: ").append(event.getStartTime());

                if (event.getThread() != null) {
                    eventDetails.append("\n  Thread: ").append(event.getThread().getJavaName())
                            .append(" (ID: ").append(event.getThread().getJavaThreadId()).append(")");
                }

                if (event.getStackTrace() != null) {
                    eventDetails.append("\n  Stack Trace:\n");
                    for (RecordedFrame frame : event.getStackTrace().getFrames()) {
                        eventDetails.append("    at ")
                                .append(frame.getMethod())
                                .append(" (")
                                .append(frame.getType())
                                .append(":")
                                .append(frame.getLineNumber())
                                .append(")\n");
                    }
                }

                carrierToPinnedEvents
                        .computeIfAbsent(carrierName, k -> new ArrayList<>())
                        .add(eventDetails.toString());
            }
        } catch (IOException e) {
            System.err.println("❌ Error analyzing pinned events: " + e.getMessage());
        }

        if (carrierToPinnedEvents.isEmpty()) {
            System.out.println("No pinning events detected.");
        } else {
            carrierToPinnedEvents.forEach((carrier, events) -> {
                System.out.println("\nCarrier Thread: " + carrier);
                System.out.println("------------------------------------");
                events.forEach(System.out::println);
            });
        }
    }

    // ======================
    // HELPER METHODS
    // ======================

    private static void cleanupJfrDirectory(String dir) {
        File directory = new File(dir);
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles((d, name) -> name.endsWith(".jfr"));
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    System.out.println("🧹 Deleted old JFR file: " + file.getName());
                }
            }
        }
    }

    private static Recording createVirtualThreadRecording(String baseName) {
        Recording recording = new Recording();

        recording.enable("jdk.VirtualThreadStart").withStackTrace().withThreshold(Duration.ZERO);
        recording.enable("jdk.VirtualThreadEnd").withStackTrace().withThreshold(Duration.ZERO);
        recording.enable("jdk.VirtualThreadPinned").withStackTrace().withThreshold(Duration.ZERO);
        recording.enable("jdk.VirtualThreadSubmitFailed").withStackTrace();

        recording.enable("jdk.ThreadStart").withStackTrace();
        recording.enable("jdk.ThreadEnd").withStackTrace();
        recording.enable("jdk.ThreadSleep").withThreshold(Duration.ofMillis(1));

        recording.enable("jdk.JavaMonitorEnter").withThreshold(Duration.ofMillis(1));
        recording.enable("jdk.JavaMonitorWait").withThreshold(Duration.ofMillis(1));

        recording.setMaxAge(Duration.ofMinutes(10));
        recording.setName(baseName + "-" + Instant.now().getEpochSecond());

        return recording;
    }

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

    private static void printSummary(Map<String, Integer> eventCounts) {
        System.out.println("\n📈 Event Summary:");
        System.out.println("==================");

        eventCounts.entrySet().stream()
                .filter(entry -> entry.getKey().contains("Thread"))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("%-30s: %d%n", entry.getKey(), entry.getValue()));

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

    public static boolean isRecording() {
        return currentRecording != null;
    }

    // ======================
    // Builder
    // ======================
    public static class Builder {
        private String outputDir = DEFAULT_OUTPUT_DIR;
        private boolean cleanupOnStart = DEFAULT_CLEANUP_ON_START;
        private String recordingName = DEFAULT_RECORDING_NAME;

        public Builder outputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder cleanupOnStart(boolean cleanupOnStart) {
            this.cleanupOnStart = cleanupOnStart;
            return this;
        }

        public Builder recordingName(String recordingName) {
            this.recordingName = recordingName;
            return this;
        }

        public JFRUtil build() {
            return new JFRUtil(this);
        }
    }
}
