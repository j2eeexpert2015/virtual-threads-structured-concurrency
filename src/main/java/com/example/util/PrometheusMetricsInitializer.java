package com.example.util;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.CollectorRegistry;

/**
 * Utility class to initialize Prometheus metrics registry and expose metrics over HTTP.
 * Also starts JFR virtual thread metrics streaming and JVM thread metrics.
 */
public class PrometheusMetricsInitializer {

    private static HTTPServer server;
    private static PrometheusMeterRegistry registry;
    private static JFRVirtualThreadMetrics jfrMetrics;

    public static void initialize() {
        try {
            // Use default Prometheus collector registry
            CollectorRegistry prometheusRegistry = CollectorRegistry.defaultRegistry;

            // Create Micrometer PrometheusMeterRegistry
            registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, prometheusRegistry, Clock.SYSTEM);

            // Register JVM metrics for platform threads
            new JvmThreadMetrics().bindTo(registry);
            new ClassLoaderMetrics().bindTo(registry);

            // Start JFR-based virtual thread metrics
            jfrMetrics = new JFRVirtualThreadMetrics(registry);
            jfrMetrics.startJfrStream();

            // Start Prometheus HTTP server to expose /metrics on port 8081
            server = new HTTPServer(8081);

            System.out.println("Prometheus metrics available at http://localhost:8081/metrics");

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Prometheus + JFR metrics", e);
        }
    }

    public static PrometheusMeterRegistry getRegistry() {
        return registry;
    }
}
