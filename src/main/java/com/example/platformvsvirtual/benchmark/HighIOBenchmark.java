package com.example.platformvsvirtual.benchmark;

import com.example.platformvsvirtual.workload.HighIOWorkloadRunner;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class HighIOBenchmark {

    private static final int TASK_COUNT = 10_000;
    private static final int IO_SLEEP_MS = 100;

    @Benchmark
    public void platformThreads() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        HighIOWorkloadRunner.runIOWorkload(executor, TASK_COUNT, IO_SLEEP_MS);
    }

    @Benchmark
    public void virtualThreads() throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        HighIOWorkloadRunner.runIOWorkload(executor, TASK_COUNT, IO_SLEEP_MS);
    }
}
