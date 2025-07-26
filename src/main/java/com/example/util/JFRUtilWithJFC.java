package com.example.util;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing JFR (Java Flight Recorder) using a .jfc configuration file.
 * Provides default static methods for quick usage and a builder for customization.
 */
public class JFRUtilWithJFC {

    private static final String DEFAULT_JFC_PATH = "jfr-config/virtual-threads.jfc";
    private static final String DEFAULT_OUTPUT_DIR = "jfr-recordings";
    private static final String DEFAULT_RECORDING_NAME = "JFRRecording";
    private static final boolean DEFAULT_CLEANUP_ON_START = true;

    private final String jfcPath;
    private final String outputDir;
    private final boolean cleanupOnStart;
    private final String recordingName;

    private Recording currentRecording;

    // Private constructor for builder
    private JFRUtilWithJFC(Builder builder) {
        this.jfcPath = builder.jfcPath;
        this.outputDir = builder.outputDir;
        this.cleanupOnStart = builder.cleanupOnStart;
        this.recordingName = builder.recordingName;
    }

    // ====== Static default-driven API ======

    private static final JFRUtilWithJFC DEFAULT_INSTANCE = new Builder().build();

    public static Recording startRecording() {
        DEFAULT_INSTANCE.start();
        return DEFAULT_INSTANCE.currentRecording;
    }

    public static Path stopRecording() {
        return DEFAULT_INSTANCE.stop();
    }

    public static Path recordAndAnalyze(Runnable code) {
        return DEFAULT_INSTANCE.runAndAnalyze(code);
    }

    // ====== Instance methods ======

    public void start() {
        validateJfcFile();
        if (cleanupOnStart) cleanupJfrDirectory(outputDir);
        stop();
        try {
            Configuration config = Configuration.create(new File(jfcPath).toPath());
            currentRecording = new Recording(config);
            currentRecording.setName(recordingName + "-" + Instant.now().getEpochSecond());
            currentRecording.start();
            System.out.println("🚀 JFR recording started with JFC: " + jfcPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start JFR recording: " + e.getMessage(), e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Path stop() {
        if (currentRecording == null) return null;
        try {
            currentRecording.stop();
            Path file = saveRecording();
            currentRecording.close();
            currentRecording = null;
            System.out.println("⏹️ JFR recording stopped and saved to: " + file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Error stopping JFR recording: " + e.getMessage(), e);
        }
    }

    // Renamed to avoid conflict with static method
    public Path runAndAnalyze(Runnable code) {
        start();
        try {
            code.run();
        } finally {
            Path jfrFile = stop();
            if (jfrFile != null) analyzeRecording(jfrFile);
            return jfrFile;
        }
    }

    public void analyzeRecording(Path jfrFile) {
        System.out.println("\n📊 Analyzing JFR Recording: " + jfrFile.getFileName());
        Map<String, Integer> counts = new HashMap<>();
        try (RecordingFile rf = new RecordingFile(jfrFile)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent event = rf.readEvent();
                counts.merge(event.getEventType().getName(), 1, Integer::sum);
            }
        } catch (IOException e) {
            System.err.println("❌ Error analyzing JFR file: " + e.getMessage());
        }
        counts.forEach((k, v) -> System.out.println(k + ": " + v));
    }

    private Path saveRecording() throws IOException {
        String filename = recordingName + "-" + Instant.now().getEpochSecond() + ".jfr";
        Path path = Paths.get(outputDir, filename);
        path.getParent().toFile().mkdirs();
        currentRecording.dump(path);
        return path;
    }

    private void cleanupJfrDirectory(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) return;
        File[] files = directory.listFiles((d, n) -> n.endsWith(".jfr"));
        if (files != null) for (File f : files) if (f.delete()) System.out.println("🧹 Deleted: " + f.getName());
    }

    private void validateJfcFile() {
        File f = new File(jfcPath);
        if (!f.exists()) throw new RuntimeException("❌ JFC file not found: " + f.getAbsolutePath());
    }

    // ====== Builder ======
    public static class Builder {
        private String jfcPath = DEFAULT_JFC_PATH;
        private String outputDir = DEFAULT_OUTPUT_DIR;
        private boolean cleanupOnStart = DEFAULT_CLEANUP_ON_START;
        private String recordingName = DEFAULT_RECORDING_NAME;

        public Builder jfcPath(String path) { this.jfcPath = path; return this; }
        public Builder outputDir(String dir) { this.outputDir = dir; return this; }
        public Builder cleanupOnStart(boolean val) { this.cleanupOnStart = val; return this; }
        public Builder recordingName(String name) { this.recordingName = name; return this; }

        public JFRUtilWithJFC build() { return new JFRUtilWithJFC(this); }
    }
}
