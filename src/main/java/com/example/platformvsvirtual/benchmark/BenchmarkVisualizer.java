package com.example.platformvsvirtual.benchmark;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class BenchmarkVisualizer {

    public static void generateCharts() {
        try {
            generateExecutionTimeChart();
            generateComparisonChart();
            System.out.println("Charts generated successfully!");
            System.out.println("- execution-time-comparison.png");
            System.out.println("- performance-comparison.png");
        } catch (IOException e) {
            System.err.println("Error generating charts: " + e.getMessage());
        }
    }

    private static void generateExecutionTimeChart() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Get values from benchmark results
        long tradLight = VirtualThreadsBenchmark.traditionalLightTime.get();
        long virtLight = VirtualThreadsBenchmark.virtualLightTime.get();
        long tradHeavy = VirtualThreadsBenchmark.traditionalHeavyTime.get();
        long virtHeavy = VirtualThreadsBenchmark.virtualHeavyTime.get();

        dataset.addValue(tradLight, "Traditional Threads", "Light Load (1K tasks)");
        dataset.addValue(virtLight, "Virtual Threads", "Light Load (1K tasks)");
        dataset.addValue(tradHeavy, "Traditional Threads", "Heavy Load (10K tasks)");
        dataset.addValue(virtHeavy, "Virtual Threads", "Heavy Load (10K tasks)");

        JFreeChart chart = ChartFactory.createBarChart(
                "Java Virtual Threads vs Traditional Threads - Execution Time",
                "Load Type",
                "Execution Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        customizeChart(chart);

        ChartUtils.saveChartAsPNG(new File("execution-time-comparison.png"),
                chart, 800, 600);
    }

    private static void generateComparisonChart() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Calculate performance improvement percentages
        long tradLight = VirtualThreadsBenchmark.traditionalLightTime.get();
        long virtLight = VirtualThreadsBenchmark.virtualLightTime.get();
        long tradHeavy = VirtualThreadsBenchmark.traditionalHeavyTime.get();
        long virtHeavy = VirtualThreadsBenchmark.virtualHeavyTime.get();

        double lightImprovement = tradLight > 0 ?
                ((double)(tradLight - virtLight) / tradLight) * 100 : 0;
        double heavyImprovement = tradHeavy > 0 ?
                ((double)(tradHeavy - virtHeavy) / tradHeavy) * 100 : 0;

        dataset.addValue(lightImprovement, "Performance Improvement", "Light Load");
        dataset.addValue(heavyImprovement, "Performance Improvement", "Heavy Load");

        JFreeChart chart = ChartFactory.createBarChart(
                "Virtual Threads Performance Improvement",
                "Load Type",
                "Improvement (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        customizeChart(chart);

        ChartUtils.saveChartAsPNG(new File("performance-comparison.png"),
                chart, 800, 600);
    }

    private static void customizeChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Set colors
        renderer.setSeriesPaint(0, new Color(70, 130, 180)); // Steel Blue
        renderer.setSeriesPaint(1, new Color(50, 205, 50)); // Lime Green

        // Set background
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Set font
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
    }

    public static void main(String[] args) {
        // For testing visualization without running benchmarks
        // Set some dummy values
        VirtualThreadsBenchmark.traditionalLightTime.set(150);
        VirtualThreadsBenchmark.virtualLightTime.set(80);
        VirtualThreadsBenchmark.traditionalHeavyTime.set(2500);
        VirtualThreadsBenchmark.virtualHeavyTime.set(1200);

        generateCharts();
    }
}
