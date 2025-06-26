package com.example.platformvsvirtual.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class VirtualThreadsBenchmark {

    private static final int LIGHT_LOAD_TASK_COUNT = 1000;
    private static final int HEAVY_LOAD_TASK_COUNT = 10000;

    // For collecting metrics
    private OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    // Static storage for results (for visualization)
    public static final AtomicLong traditionalLightTime = new AtomicLong();
    public static final AtomicLong virtualLightTime = new AtomicLong();
    public static final AtomicLong traditionalHeavyTime = new AtomicLong();
    public static final AtomicLong virtualHeavyTime = new AtomicLong();

    @Benchmark
    public void traditionalThreadsLightLoad() throws InterruptedException {
        long result = runBenchmark(LIGHT_LOAD_TASK_COUNT,
                Executors.newFixedThreadPool(100));
        traditionalLightTime.set(result);
    }

    @Benchmark
    public void virtualThreadsLightLoad() throws InterruptedException {
        long result = runBenchmark(LIGHT_LOAD_TASK_COUNT,
                Executors.newVirtualThreadPerTaskExecutor());
        virtualLightTime.set(result);
    }

    @Benchmark
    public void traditionalThreadsHeavyLoad() throws InterruptedException {
        long result = runBenchmark(HEAVY_LOAD_TASK_COUNT,
                Executors.newFixedThreadPool(200));
        traditionalHeavyTime.set(result);
    }

    @Benchmark
    public void virtualThreadsHeavyLoad() throws InterruptedException {
        long result = runBenchmark(HEAVY_LOAD_TASK_COUNT,
                Executors.newVirtualThreadPerTaskExecutor());
        virtualHeavyTime.set(result);
    }

    private long runBenchmark(int taskCount, ExecutorService executorService)
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(taskCount);
        long startTime = System.nanoTime();
        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();

        for (int i = 0; i < taskCount; i++) {
            executorService.submit(() -> {
                performTask();
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        long endTime = System.nanoTime();
        long endMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long executionTime = (endTime - startTime) / 1_000_000; // Convert to ms
        long memoryUsed = (endMemory - startMemory) / (1024 * 1024); // Convert to MB

        System.out.printf("Tasks: %d, Execution Time: %d ms, Memory Used: %d MB%n",
                taskCount, executionTime, memoryUsed);

        return executionTime;
    }

    private void performTask() {
        try {
            // Simulate I/O bound work
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VirtualThreadsBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results.json")
                .build();

        new Runner(opt).run();

        // Generate visualization after benchmarks complete
        BenchmarkVisualizer.generateCharts();
    }
}
