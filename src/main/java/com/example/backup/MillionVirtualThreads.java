package com.example.backup;


import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This program creates 1 million virtual threads and tracks:
 * 1. Which ForkJoinPool (default carrier thread pool) was used.
 * 2. Which platform (carrier) threads actually executed the virtual threads.
 *
 * It demonstrates how virtual threads are multiplexed onto a small number of real threads.
 */


public class MillionVirtualThreads {

    // Regex pattern to extract the ForkJoinPool name from the thread description (e.g., ForkJoinPool-1)
    private static final Pattern THREAD_POOL_PATTERN = Pattern.compile("ForkJoinPool-\\d+");
    // // Regex pattern to extract the worker thread name (carrier thread) from the thread description(e.g., worker-1)
    private static final Pattern WORKER_PATTERN = Pattern.compile("worker-\\d+");

    public static void main(String[] args) throws InterruptedException {
        // Thread-safe sets for unique pool and worker names
        Set<String> forkJoinPoolsUsed = ConcurrentHashMap.newKeySet();
        Set<String> platformWorkerThreadsUsed = ConcurrentHashMap.newKeySet();

        // Number of virtual threads to create
        final int NUMBER_OF_THREADS = 1_000_000;

        // Create 1 million virtual threads but don't start them yet
        var virtualThreads = IntStream.range(0, NUMBER_OF_THREADS)
                .mapToObj(i -> Thread.ofVirtual()
                        .unstarted(() -> {
                            // Inside each virtual thread, extract and record the names of the pool and carrier thread
                            // Get and store pool name
                            forkJoinPoolsUsed.add(getThreadPoolName());
                            // Get and store worker name
                            platformWorkerThreadsUsed.add(getWorkerName());
                        })
                )
                .toList();

        // Start timing
        Instant startTime = Instant.now();

        // Start all threads
        for (var thread : virtualThreads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (var thread : virtualThreads) {
            thread.join();
        }

        // Calculate time taken
        Instant endTime = Instant.now();
        long durationMs = Duration.between(startTime, endTime).toMillis();

        // Print results
        System.out.println("Virtual threads: " + NUMBER_OF_THREADS);
        System.out.println("CPU cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Time taken: " + durationMs + "ms");
        System.out.println("### Thread Pools");
        forkJoinPoolsUsed.forEach(System.out::println);
        System.out.println("### Workers (platform threads) count : " + platformWorkerThreadsUsed.size());
        platformWorkerThreadsUsed.forEach(System.out::println);
    }

    // Get pool name from thread info (e.g., ForkJoinPool-1)
    private static String getThreadPoolName() {
        String threadInfo = Thread.currentThread().toString();
        Matcher poolMatcher = THREAD_POOL_PATTERN.matcher(threadInfo);
        return poolMatcher.find() ? poolMatcher.group() : "pool not found";
    }

    // Get worker name from thread info (e.g., worker-2)
    private static String getWorkerName() {
        String threadInfo = Thread.currentThread().toString();
        Matcher workerMatcher = WORKER_PATTERN.matcher(threadInfo);
        return workerMatcher.find() ? workerMatcher.group() : "worker not found";
    }
}
