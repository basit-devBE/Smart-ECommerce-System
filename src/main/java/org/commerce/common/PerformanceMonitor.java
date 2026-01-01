package org.commerce.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for measuring and tracking query/operation performance.
 * Used to document measurable improvements as required by Epic 3.
 */
public class PerformanceMonitor {
    
    private static final Map<String, PerformanceMetrics> metricsStore = new HashMap<>();
    private static boolean enabled = true;
    
    /**
     * Starts timing an operation.
     * 
     * @param operationName The name of the operation to track
     * @return The start time in nanoseconds
     */
    public static long startTiming(String operationName) {
        return System.nanoTime();
    }
    
    /**
     * Ends timing and records the operation duration.
     * 
     * @param operationName The name of the operation
     * @param startTime The start time from startTiming()
     * @return The duration in milliseconds
     */
    public static long endTiming(String operationName, long startTime) {
        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        
        if (enabled) {
            recordMetric(operationName, durationMillis);
            logPerformance(operationName, durationMillis);
        }
        
        return durationMillis;
    }
    
    /**
     * Measures the execution time of an operation and returns both result and duration.
     * 
     * @param operationName The name of the operation
     * @param operation The operation to measure
     * @return TimedResult containing the result and duration
     */
    public static <T> TimedResult<T> measure(String operationName, java.util.function.Supplier<T> operation) {
        long startTime = startTiming(operationName);
        T result = operation.get();
        long duration = endTiming(operationName, startTime);
        return new TimedResult<>(result, duration);
    }
    
    /**
     * Records a metric for an operation.
     */
    private static void recordMetric(String operationName, long durationMillis) {
        PerformanceMetrics metrics = metricsStore.computeIfAbsent(
            operationName, 
            k -> new PerformanceMetrics()
        );
        metrics.addMeasurement(durationMillis);
    }
    
    /**
     * Logs performance information.
     */
    private static void logPerformance(String operationName, long durationMillis) {
        System.out.printf("[PERFORMANCE] %s completed in %d ms%n", operationName, durationMillis);
    }
    
    /**
     * Gets performance metrics for an operation.
     * 
     * @param operationName The operation name
     * @return The performance metrics, or null if not found
     */
    public static PerformanceMetrics getMetrics(String operationName) {
        return metricsStore.get(operationName);
    }
    
    /**
     * Gets all recorded metrics.
     */
    public static Map<String, PerformanceMetrics> getAllMetrics() {
        return new HashMap<>(metricsStore);
    }
    
    /**
     * Clears all recorded metrics.
     */
    public static void clearMetrics() {
        metricsStore.clear();
    }
    
    /**
     * Enables or disables performance monitoring.
     */
    public static void setEnabled(boolean enabled) {
        PerformanceMonitor.enabled = enabled;
    }
    
    /**
     * Prints a performance report to console.
     */
    public static void printReport() {
        System.out.println("\n============ PERFORMANCE REPORT ============");
        if (metricsStore.isEmpty()) {
            System.out.println("No performance data recorded.");
        } else {
            metricsStore.forEach((operation, metrics) -> {
                System.out.printf("\nOperation: %s%n", operation);
                System.out.printf("  Executions: %d%n", metrics.getCount());
                System.out.printf("  Average: %.2f ms%n", metrics.getAverageDuration());
                System.out.printf("  Min: %d ms%n", metrics.getMinDuration());
                System.out.printf("  Max: %d ms%n", metrics.getMaxDuration());
                System.out.printf("  Total: %d ms%n", metrics.getTotalDuration());
            });
        }
        System.out.println("============================================\n");
    }
    
    /**
     * Container for timed operation results.
     */
    public static class TimedResult<T> {
        private final T result;
        private final long durationMillis;
        
        public TimedResult(T result, long durationMillis) {
            this.result = result;
            this.durationMillis = durationMillis;
        }
        
        public T getResult() {
            return result;
        }
        
        public long getDurationMillis() {
            return durationMillis;
        }
    }
    
    /**
     * Stores performance metrics for an operation.
     */
    public static class PerformanceMetrics {
        private long count = 0;
        private long totalDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        private long maxDuration = 0;
        
        public void addMeasurement(long durationMillis) {
            count++;
            totalDuration += durationMillis;
            minDuration = Math.min(minDuration, durationMillis);
            maxDuration = Math.max(maxDuration, durationMillis);
        }
        
        public long getCount() {
            return count;
        }
        
        public long getTotalDuration() {
            return totalDuration;
        }
        
        public double getAverageDuration() {
            return count > 0 ? (double) totalDuration / count : 0;
        }
        
        public long getMinDuration() {
            return minDuration == Long.MAX_VALUE ? 0 : minDuration;
        }
        
        public long getMaxDuration() {
            return maxDuration;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PerformanceMetrics{count=%d, avg=%.2fms, min=%dms, max=%dms}", 
                count, getAverageDuration(), getMinDuration(), getMaxDuration()
            );
        }
    }
}
