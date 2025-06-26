package com.example.platformvsvirtual.benchmark;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class JsonBenchmarkVisualizer {

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static void main(String[] args) {
        try {
            generateChartsFromJson("benchmark-results.json");
        } catch (Exception e) {
            System.err.println("Error generating charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generateChartsFromJson(String jsonFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode results = mapper.readTree(new File(jsonFilePath));

        Map<String, Double> benchmarkResults = extractResults(results);

        if (benchmarkResults.size() < 4) {
            System.err.println("Warning: Expected 4 benchmark results, found " + benchmarkResults.size());
        }

        printResults(benchmarkResults);
        generateExecutionTimeChart(benchmarkResults);
        generateImprovementChart(benchmarkResults);

        System.out.println("\nCharts generated successfully:");
        System.out.println("- execution-time-chart.png");
        System.out.println("- improvement-chart.png");
    }

    private static Map<String, Double> extractResults(JsonNode results) {
        Map<String, Double> benchmarkResults = new HashMap<>();

        for (JsonNode result : results) {
            String benchmark = result.get("benchmark").asText();
            double score = result.get("primaryMetric").get("score").asDouble();

            // Extract the method name from the full benchmark path
            String methodName = benchmark.substring(benchmark.lastIndexOf('.') + 1);
            benchmarkResults.put(methodName, score);
        }

        return benchmarkResults;
    }

    private static void printResults(Map<String, Double> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BENCHMARK RESULTS SUMMARY");
        System.out.println("=".repeat(80));

        double tradLight = results.getOrDefault("traditionalThreadsLightLoad", 0.0);
        double virtLight = results.getOrDefault("virtualThreadsLightLoad", 0.0);
        double tradHeavy = results.getOrDefault("traditionalThreadsHeavyLoad", 0.0);
        double virtHeavy = results.getOrDefault("virtualThreadsHeavyLoad", 0.0);

        System.out.println("Light Load Results:");
        System.out.printf("  Traditional Threads: %s ms%n", df.format(tradLight));
        System.out.printf("  Virtual Threads:     %s ms%n", df.format(virtLight));
        System.out.printf("  Virtual threads are %.1fx FASTER%n", tradLight / virtLight);
        System.out.printf("  Performance improvement: %.1f%%%n",
                calculateImprovement(tradLight, virtLight));

        System.out.println("\nHeavy Load Results:");
        System.out.printf("  Traditional Threads: %s ms%n", df.format(tradHeavy));
        System.out.printf("  Virtual Threads:     %s ms%n", df.format(virtHeavy));
        System.out.printf("  Virtual threads are %.1fx FASTER%n", tradHeavy / virtHeavy);
        System.out.printf("  Performance improvement: %.1f%%%n",
                calculateImprovement(tradHeavy, virtHeavy));

        System.out.println("\n" + "=".repeat(80));
    }

    private static void generateExecutionTimeChart(Map<String, Double> results) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double tradLight = results.getOrDefault("traditionalThreadsLightLoad", 0.0);
        double virtLight = results.getOrDefault("virtualThreadsLightLoad", 0.0);
        double tradHeavy = results.getOrDefault("traditionalThreadsHeavyLoad", 0.0);
        double virtHeavy = results.getOrDefault("virtualThreadsHeavyLoad", 0.0);

        dataset.addValue(tradLight, "Traditional Threads", "Light Load\n(1K tasks)");
        dataset.addValue(virtLight, "Virtual Threads", "Light Load\n(1K tasks)");
        dataset.addValue(tradHeavy, "Traditional Threads", "Heavy Load\n(10K tasks)");
        dataset.addValue(virtHeavy, "Virtual Threads", "Heavy Load\n(10K tasks)");

        JFreeChart chart = ChartFactory.createBarChart(
                "Java Virtual Threads vs Traditional Threads - Execution Time",
                "Load Type",
                "Execution Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        customizeChart(chart);

        ChartUtils.saveChartAsPNG(new File("execution-time-chart.png"), chart, 900, 600);
    }

    private static void generateImprovementChart(Map<String, Double> results) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double tradLight = results.getOrDefault("traditionalThreadsLightLoad", 0.0);
        double virtLight = results.getOrDefault("virtualThreadsLightLoad", 0.0);
        double tradHeavy = results.getOrDefault("traditionalThreadsHeavyLoad", 0.0);
        double virtHeavy = results.getOrDefault("virtualThreadsHeavyLoad", 0.0);

        double lightImprovement = calculateImprovement(tradLight, virtLight);
        double heavyImprovement = calculateImprovement(tradHeavy, virtHeavy);
        double lightSpeedup = tradLight / virtLight;
        double heavySpeedup = tradHeavy / virtHeavy;

        dataset.addValue(lightImprovement, "Performance Improvement %", "Light Load");
        dataset.addValue(heavyImprovement, "Performance Improvement %", "Heavy Load");
        dataset.addValue(lightSpeedup, "Speed Multiplier", "Light Load");
        dataset.addValue(heavySpeedup, "Speed Multiplier", "Heavy Load");

        JFreeChart chart = ChartFactory.createBarChart(
                "Virtual Threads Performance Gains",
                "Load Type",
                "Improvement Factor",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        customizeImprovementChart(chart);

        ChartUtils.saveChartAsPNG(new File("improvement-chart.png"), chart, 900, 600);
    }

    private static void customizeChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Set colors - Traditional threads in red, Virtual threads in green
        renderer.setSeriesPaint(0, new Color(220, 50, 50));   // Red for traditional
        renderer.setSeriesPaint(1, new Color(50, 180, 50));   // Green for virtual

        // Customize appearance
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        // Set fonts
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
        plot.getDomainAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));
        plot.getRangeAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));
    }

    private static void customizeImprovementChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Set colors - Different shades of blue/green for improvement metrics
        renderer.setSeriesPaint(0, new Color(70, 130, 180));  // Steel Blue for %
        renderer.setSeriesPaint(1, new Color(34, 139, 34));   // Forest Green for multiplier

        // Customize appearance
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        // Set fonts
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
        plot.getDomainAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));
        plot.getRangeAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));
    }

    private static double calculateImprovement(double traditional, double virtual) {
        if (traditional <= 0 || virtual <= 0) return 0;
        return ((traditional - virtual) / traditional) * 100;
    }
}
