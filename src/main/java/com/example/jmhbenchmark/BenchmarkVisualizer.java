package com.example.jmhbenchmark;



import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BenchmarkVisualizer {
    public static void main(String[] args) {
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Read JMH results from JSON
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("results.json")) {
            JSONArray results = (JSONArray) parser.parse(reader);
            for (Object obj : results) {
                JSONObject result = (JSONObject) obj;
                String benchmarkName = ((String) result.get("benchmark")).split("\\.")[2];
                JSONObject primaryMetric = (JSONObject) result.get("primaryMetric");
                double score = ((Number) primaryMetric.get("score")).doubleValue();
                dataset.addValue(score, benchmarkName, "Execution Time");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return;
        }

        // Create bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Virtual Threads vs. Traditional Threads Benchmark",
                "Thread Type",
                "Average Execution Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Display chart in a frame
        ChartFrame frame = new ChartFrame("Benchmark Results", chart);
        frame.pack();
        frame.setVisible(true);
    }
}
