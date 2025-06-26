package com.example.jmhbenchmark;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ThreadBenchmark {

    private static final int TASK_COUNT = 1000;

    // Simulate an I/O-bound task (e.g., network call)
    private void simulateIOTask(Blackhole blackhole) throws InterruptedException {
        Thread.sleep(10); // Simulate 10ms I/O delay
        blackhole.consume(System.currentTimeMillis()); // Prevent optimization
    }

    @Benchmark
    public void virtualThreads(Blackhole blackhole) throws InterruptedException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < TASK_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        simulateIOTask(blackhole);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    @Benchmark
    public void traditionalThreads(Blackhole blackhole) throws InterruptedException {
        try (ExecutorService executor = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < TASK_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        simulateIOTask(blackhole);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}
