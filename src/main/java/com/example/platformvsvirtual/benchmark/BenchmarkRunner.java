package com.example.platformvsvirtual.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {

    public static void main(String[] args) {
        System.out.println("Starting Java Virtual Threads Benchmark...");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        System.out.println("=".repeat(60));

        try {
            runBenchmarks();
            generateVisualizations();
            printSummary();
        } catch (Exception e) {
            System.err.println("Error running benchmarks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VirtualThreadsBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results.json")
                .build();

        new Runner(opt).run();
    }

    private static void generateVisualizations() {
        System.out.println("\nGenerating visualizations...");
        BenchmarkVisualizer.generateCharts();
    }

    private static void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("BENCHMARK SUMMARY");
        System.out.println("=".repeat(60));

        long tradLight = VirtualThreadsBenchmark.traditionalLightTime.get();
        long virtLight = VirtualThreadsBenchmark.virtualLightTime.get();
        long tradHeavy = VirtualThreadsBenchmark.traditionalHeavyTime.get();
        long virtHeavy = VirtualThreadsBenchmark.virtualHeavyTime.get();

        System.out.println("Light Load (1,000 tasks):");
        System.out.printf("  Traditional Threads: %d ms%n", tradLight);
        System.out.printf("  Virtual Threads:     %d ms%n", virtLight);
        System.out.printf("  Improvement:         %.1f%%%n",
                calculateImprovement(tradLight, virtLight));

        System.out.println("\nHeavy Load (10,000 tasks):");
        System.out.printf("  Traditional Threads: %d ms%n", tradHeavy);
        System.out.printf("  Virtual Threads:     %d ms%n", virtHeavy);
        System.out.printf("  Improvement:         %.1f%%%n",
                calculateImprovement(tradHeavy, virtHeavy));

        System.out.println("\nFiles generated:");
        System.out.println("  - benchmark-results.json (JMH results)");
        System.out.println("  - execution-time-comparison.png");
        System.out.println("  - performance-comparison.png");
        System.out.println("\n" + "=".repeat(60));
    }

    private static double calculateImprovement(long traditional, long virtual) {
        if (traditional == 0) return 0;
        return ((double)(traditional - virtual) / traditional) * 100;
    }
}
