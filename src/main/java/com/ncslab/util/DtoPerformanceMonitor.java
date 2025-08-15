package com.ncslab.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance monitoring and optimization utility for DTO operations
 * Tracks metrics, performance, and provides optimization recommendations
 */
@Slf4j
public class DtoPerformanceMonitor {
    
    private static final Map<String, PerformanceMetrics> operationMetrics = new ConcurrentHashMap<>();
    private static final AtomicLong totalOperations = new AtomicLong(0);
    private static final long startTime = System.currentTimeMillis();
    
    /**
     * Performance metrics for a specific operation
     */
    public static class PerformanceMetrics {
        private final AtomicInteger executionCount = new AtomicInteger(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicInteger errorCount = new AtomicInteger(0);
        private final List<String> recentErrors = new ArrayList<>();
        
        public void recordExecution(long executionTime, boolean success) {
            executionCount.incrementAndGet();
            totalTime.addAndGet(executionTime);
            
            // Update min/max times
            updateMaxTime(executionTime);
            updateMinTime(executionTime);
            
            if (!success) {
                errorCount.incrementAndGet();
            }
        }
        
        public void recordError(String error) {
            synchronized (recentErrors) {
                recentErrors.add(error);
                // Keep only last 10 errors
                if (recentErrors.size() > 10) {
                    recentErrors.remove(0);
                }
            }
        }
        
        private void updateMaxTime(long time) {
            long currentMax;
            do {
                currentMax = maxTime.get();
                if (time <= currentMax) break;
            } while (!maxTime.compareAndSet(currentMax, time));
        }
        
        private void updateMinTime(long time) {
            long currentMin;
            do {
                currentMin = minTime.get();
                if (time >= currentMin || currentMin == Long.MAX_VALUE) break;
            } while (!minTime.compareAndSet(currentMin, time));
        }
        
        public double getAverageTime() {
            int count = executionCount.get();
            return count > 0 ? (double) totalTime.get() / count : 0.0;
        }
        
        public double getSuccessRate() {
            int count = executionCount.get();
            return count > 0 ? (double) (count - errorCount.get()) / count * 100 : 100.0;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("executionCount", executionCount.get());
            map.put("averageTime", getAverageTime());
            map.put("maxTime", maxTime.get() == Long.MAX_VALUE ? 0 : maxTime.get());
            map.put("minTime", minTime.get() == Long.MAX_VALUE ? 0 : minTime.get());
            map.put("totalTime", totalTime.get());
            map.put("errorCount", errorCount.get());
            map.put("successRate", getSuccessRate());
            synchronized (recentErrors) {
                map.put("recentErrors", new ArrayList<>(recentErrors));
            }
            return map;
        }
    }
    
    /**
     * Performance tracking context for measuring execution time
     */
    public static class PerformanceContext implements AutoCloseable {
        private final String operationName;
        private final long startTime;
        private boolean success = true;
        
        private PerformanceContext(String operationName) {
            this.operationName = operationName;
            this.startTime = System.nanoTime();
        }
        
        public void markError(String error) {
            this.success = false;
            getMetrics(operationName).recordError(error);
        }
        
        @Override
        public void close() {
            long executionTime = System.nanoTime() - startTime;
            long executionTimeMs = executionTime / 1_000_000; // Convert to milliseconds
            
            getMetrics(operationName).recordExecution(executionTimeMs, success);
            totalOperations.incrementAndGet();
            
            // Log slow operations
            if (executionTimeMs > 1000) { // Operations taking more than 1 second
                log.warn("Slow DTO operation detected: {} took {}ms", operationName, executionTimeMs);
            }
        }
    }
    
    /**
     * Start monitoring a DTO operation
     * @param operationName Name of the operation (e.g., "ModelJson.parse", "BlockJson.validate")
     * @return Performance context for automatic timing
     */
    public static PerformanceContext startOperation(String operationName) {
        return new PerformanceContext(operationName);
    }
    
    /**
     * Get or create metrics for an operation
     * @param operationName Operation name
     * @return PerformanceMetrics instance
     */
    private static PerformanceMetrics getMetrics(String operationName) {
        return operationMetrics.computeIfAbsent(operationName, k -> new PerformanceMetrics());
    }
    
    /**
     * Record a DTO parsing operation
     * @param dtoType DTO type (e.g., "ModelJson", "BlockJson")
     * @param executionTime Execution time in milliseconds
     * @param success Whether the operation was successful
     */
    public static void recordParsing(String dtoType, long executionTime, boolean success) {
        String operationName = dtoType + ".parse";
        getMetrics(operationName).recordExecution(executionTime, success);
        totalOperations.incrementAndGet();
    }
    
    /**
     * Record a DTO validation operation
     * @param dtoType DTO type
     * @param executionTime Execution time in milliseconds
     * @param success Whether validation passed
     */
    public static void recordValidation(String dtoType, long executionTime, boolean success) {
        String operationName = dtoType + ".validate";
        getMetrics(operationName).recordExecution(executionTime, success);
        totalOperations.incrementAndGet();
    }
    
    /**
     * Record a DTO serialization operation
     * @param dtoType DTO type
     * @param executionTime Execution time in milliseconds
     * @param success Whether serialization was successful
     */
    public static void recordSerialization(String dtoType, long executionTime, boolean success) {
        String operationName = dtoType + ".serialize";
        getMetrics(operationName).recordExecution(executionTime, success);
        totalOperations.incrementAndGet();
    }
    
    /**
     * Record a DTO conversion operation (DTO ↔ JSONObject)
     * @param fromType Source type
     * @param toType Target type
     * @param executionTime Execution time in milliseconds
     * @param success Whether conversion was successful
     */
    public static void recordConversion(String fromType, String toType, long executionTime, boolean success) {
        String operationName = fromType + ".to." + toType;
        getMetrics(operationName).recordExecution(executionTime, success);
        totalOperations.incrementAndGet();
    }
    
    /**
     * Get performance metrics for a specific operation
     * @param operationName Operation name
     * @return Performance metrics map
     */
    public static Map<String, Object> getOperationMetrics(String operationName) {
        PerformanceMetrics metrics = operationMetrics.get(operationName);
        return metrics != null ? metrics.toMap() : new HashMap<>();
    }
    
    /**
     * Get comprehensive performance report
     * @return Performance report with all metrics
     */
    public static Map<String, Object> getPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Overall statistics
        long uptimeMs = System.currentTimeMillis() - startTime;
        report.put("uptimeMs", uptimeMs);
        report.put("totalOperations", totalOperations.get());
        report.put("operationsPerSecond", uptimeMs > 0 ? (double) totalOperations.get() / (uptimeMs / 1000.0) : 0.0);
        
        // Operation-specific metrics
        Map<String, Map<String, Object>> operationDetails = new HashMap<>();
        for (Map.Entry<String, PerformanceMetrics> entry : operationMetrics.entrySet()) {
            operationDetails.put(entry.getKey(), entry.getValue().toMap());
        }
        report.put("operations", operationDetails);
        
        // Performance insights
        report.put("insights", generatePerformanceInsights());
        
        // Memory metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryMetrics = new HashMap<>();
        memoryMetrics.put("totalMemory", runtime.totalMemory());
        memoryMetrics.put("freeMemory", runtime.freeMemory());
        memoryMetrics.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memoryMetrics.put("maxMemory", runtime.maxMemory());
        report.put("memory", memoryMetrics);
        
        return report;
    }
    
    /**
     * Generate performance insights and recommendations
     * @return List of insights and recommendations
     */
    private static List<String> generatePerformanceInsights() {
        List<String> insights = new ArrayList<>();
        
        for (Map.Entry<String, PerformanceMetrics> entry : operationMetrics.entrySet()) {
            String operationName = entry.getKey();
            PerformanceMetrics metrics = entry.getValue();
            
            // Slow operations
            if (metrics.getAverageTime() > 500) { // More than 500ms average
                insights.add("SLOW: " + operationName + " averages " + 
                           String.format("%.1f", metrics.getAverageTime()) + "ms");
            }
            
            // High error rates
            if (metrics.getSuccessRate() < 95.0) {
                insights.add("ERROR: " + operationName + " has " + 
                           String.format("%.1f", metrics.getSuccessRate()) + "% success rate");
            }
            
            // High usage operations
            if (metrics.executionCount.get() > totalOperations.get() * 0.1) {
                insights.add("HIGH_USAGE: " + operationName + " represents " + 
                           (metrics.executionCount.get() * 100 / totalOperations.get()) + "% of operations");
            }
        }
        
        // General recommendations
        if (insights.isEmpty()) {
            insights.add("GOOD: All DTO operations performing within acceptable parameters");
        }
        
        // Memory recommendations
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
        
        if (memoryUsagePercent > 80) {
            insights.add("MEMORY: High memory usage (" + String.format("%.1f", memoryUsagePercent) + "%)");
        }
        
        return insights;
    }
    
    /**
     * Reset all performance metrics
     */
    public static void resetMetrics() {
        operationMetrics.clear();
        totalOperations.set(0);
        log.info("DTO performance metrics reset");
    }
    
    /**
     * Get top N slowest operations
     * @param n Number of operations to return
     * @return List of operation names sorted by average execution time
     */
    public static List<Map<String, Object>> getTopSlowestOperations(int n) {
        return operationMetrics.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> op = new HashMap<>();
                    op.put("operation", entry.getKey());
                    op.put("averageTime", entry.getValue().getAverageTime());
                    op.put("executionCount", entry.getValue().executionCount.get());
                    return op;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("averageTime"), (Double) a.get("averageTime")))
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get operations with highest error rates
     * @param n Number of operations to return
     * @return List of operation names sorted by error rate
     */
    public static List<Map<String, Object>> getTopErrorOperations(int n) {
        return operationMetrics.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> op = new HashMap<>();
                    op.put("operation", entry.getKey());
                    op.put("successRate", entry.getValue().getSuccessRate());
                    op.put("errorCount", entry.getValue().errorCount.get());
                    return op;
                })
                .filter(op -> (Double) op.get("successRate") < 100.0)
                .sorted((a, b) -> Double.compare((Double) a.get("successRate"), (Double) b.get("successRate")))
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Check if system is performing well
     * @return true if all metrics are within acceptable ranges
     */
    public static boolean isSystemHealthy() {
        for (PerformanceMetrics metrics : operationMetrics.values()) {
            if (metrics.getAverageTime() > 1000 || metrics.getSuccessRate() < 90.0) {
                return false;
            }
        }
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
        
        return memoryUsagePercent < 90.0;
    }
}