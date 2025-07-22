package com.example.mountandunmount;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class VirtualThreadProfiler {

    // Stores profiling results per task (taskId → TaskStats)
    // Sorted map ensures ordered output
    private final Map<Integer, TaskStats> taskStatsMap = new ConcurrentSkipListMap<>();

    // Start time for relative timing of all tasks
    private final long profilerStartTime = System.currentTimeMillis();

    // Whether to display simplified virtual thread names
    private final boolean useFriendlyThreadNames;

    // Default: full thread names
    public VirtualThreadProfiler() {
        this(false);
    }

    // Enable/disable simplified thread name output
    public VirtualThreadProfiler(boolean useFriendlyThreadNames) {
        this.useFriendlyThreadNames = useFriendlyThreadNames;
    }

    // Wraps a task to record timing and detect carrier thread switches
    public Runnable profile(int taskId, Runnable task) {
        return () -> {
            long threadStartTime = System.currentTimeMillis();
            String threadBefore = Thread.currentThread().toString();

            task.run();

            long threadEndTime = System.currentTimeMillis();
            String threadAfter = Thread.currentThread().toString();

            String virtualThreadName = extractVirtualThreadName(threadBefore);
            String carrierThreadNameBefore = extractCarrierThreadName(threadBefore);
            String carrierThreadNameAfter = extractCarrierThreadName(threadAfter);

            String threadSwitchNote = carrierThreadNameBefore.equals(carrierThreadNameAfter)
                    ? "❌ " + virtualThreadName + " remounted on same carrier thread ! " + carrierThreadNameBefore
                    : "✅ " + virtualThreadName + " hopped from " + carrierThreadNameBefore
                    + " to " + carrierThreadNameAfter;

            TaskStats previous = taskStatsMap.put(taskId,
                    new TaskStats(threadStartTime - profilerStartTime,
                            threadEndTime - profilerStartTime,
                            threadSwitchNote));

            if (previous != null) {
                throw new IllegalArgumentException("Duplicate task ID: " + taskId);
            }
        };
    }

    // Print timeline chart and thread switch notes for all tasks
    public void displayReport() {
        long maxEndTime = taskStatsMap.values().stream()
                .mapToLong(TaskStats::end)
                .max()
                .orElse(1L);
        double scaleFactor = 50.0 / maxEndTime;

        System.out.println("\n📊 Task Execution Timeline:\n");

        for (Map.Entry<Integer, TaskStats> entry : taskStatsMap.entrySet()) {
            int taskId = entry.getKey();
            TaskStats stats = entry.getValue();

            String leading = " ".repeat((int) (stats.start * scaleFactor));
            String bar = "*".repeat((int) ((stats.end - stats.start) * scaleFactor));
            String trailing = " ".repeat(Math.max(0, 50 - leading.length() - bar.length()));

            System.out.printf("Task %02d: %s%s%s  ->> %s%n",
                    taskId, leading, bar, trailing, stats.threadSwitchNote);
        }

        System.out.printf("%n🕒 Total Duration: %d ms | 🧵 Tasks: %d | 💻 CPU Cores: %d%n",
                System.currentTimeMillis() - profilerStartTime,
                taskStatsMap.size(),
                Runtime.getRuntime().availableProcessors());
    }

    // Extracts virtual thread name, e.g., "VirtualThread[#21]"
    private String extractVirtualThreadName(String threadString) {
        if (!useFriendlyThreadNames) return threadString;
        int slashIndex = threadString.indexOf('/');
        return slashIndex != -1 ? threadString.substring(0, slashIndex) : threadString;
    }

    // Extracts carrier thread name, e.g., "ForkJoinPool-1-worker-3"
    private String extractCarrierThreadName(String threadString) {
        int atIndex = threadString.indexOf('@');
        return atIndex != -1 ? threadString.substring(atIndex + 1) : "unknown";
    }

    // Per-task execution stats: start time, end time, and switch message
    private static class TaskStats {
        final long start;
        final long end;
        final String threadSwitchNote;

        TaskStats(long start, long end, String threadSwitchNote) {
            this.start = start;
            this.end = end;
            this.threadSwitchNote = threadSwitchNote;
        }

        long start() { return start; }

        long end() { return end; }
    }
}
