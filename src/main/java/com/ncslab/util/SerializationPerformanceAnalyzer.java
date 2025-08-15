package com.ncslab.util;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Builder;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Performance analyzer for serialization operations with optimization recommendations.
 * Provides detailed metrics and suggestions for improving string->DTO conversion performance.
 */
@Slf4j
public class SerializationPerformanceAnalyzer {
    
    private static final Map<String, PerformanceData> performanceHistory = new ConcurrentHashMap<>();
    private static final AtomicLong totalOperations = new AtomicLong(0);
    private static final AtomicLong totalTime = new AtomicLong(0);
    
    /**
     * Performance data for a specific operation type
     */
    @Getter
    @Builder
    public static class PerformanceData {
        private final AtomicInteger operationCount = new AtomicInteger(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final List<String> recentErrors = new ArrayList<>();
        private final AtomicLong memoryUsed = new AtomicLong(0);
        
        public void recordOperation(long executionTime, boolean success, long memoryDelta) {
            operationCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            
            // Update min/max times
            updateMinMax(executionTime);
            
            if (success) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            
            if (memoryDelta > 0) {
                memoryUsed.addAndGet(memoryDelta);
            }
        }
        
        public void recordError(String error) {
            synchronized (recentErrors) {
                recentErrors.add(error);
                if (recentErrors.size() > 20) {
                    recentErrors.remove(0);
                }
            }
        }
        
        private void updateMinMax(long executionTime) {
            // Update max
            long currentMax;
            do {
                currentMax = maxExecutionTime.get();
                if (executionTime <= currentMax) break;
            } while (!maxExecutionTime.compareAndSet(currentMax, executionTime));
            
            // Update min
            long currentMin;
            do {
                currentMin = minExecutionTime.get();
                if (executionTime >= currentMin || currentMin == Long.MAX_VALUE) break;
            } while (!minExecutionTime.compareAndSet(currentMin, executionTime));
        }
        
        public double getAverageExecutionTime() {
            int count = operationCount.get();
            return count > 0 ? (double) totalExecutionTime.get() / count : 0.0;
        }
        
        public double getSuccessRate() {
            int total = operationCount.get();
            return total > 0 ? (double) successCount.get() / total * 100.0 : 100.0;
        }
        
        public double getAverageMemoryUsage() {
            int count = operationCount.get();
            return count > 0 ? (double) memoryUsed.get() / count : 0.0;
        }
    }
    
    /**
     * Performance analysis result with recommendations
     */
    @Getter
    @Builder
    public static class PerformanceAnalysis {
        private final double overallAverageTime;
        private final double overallSuccessRate;
        private final long totalOperations;
        private final Map<String, OperationMetrics> operationMetrics;
        private final List<String> recommendations;
        private final List<String> warnings;
        private final MemoryAnalysis memoryAnalysis;
        private final OptimizationPotential optimizationPotential;
        
        @Getter
        @Builder
        public static class OperationMetrics {
            private final String operationType;
            private final double averageTime;
            private final double successRate;
            private final int operationCount;
            private final long maxTime;
            private final long minTime;
            private final double averageMemory;
        }
        
        @Getter
        @Builder
        public static class MemoryAnalysis {
            private final long totalMemoryUsed;
            private final double averageMemoryPerOperation;
            private final String memoryEfficiencyRating;
            private final List<String> memoryOptimizations;
        }
        
        @Getter
        @Builder
        public static class OptimizationPotential {
            private final double expectedSpeedup;
            private final double memoryReduction;
            private final List<String> optimizationStrategies;
            private final String migrationRecommendation;
        }
    }
    
    /**
     * Record a serialization operation performance
     */
    public static void recordSerializationOperation(String operationType, long executionTime, 
                                                   boolean success, long memoryDelta, String error) {
        PerformanceData data = performanceHistory.computeIfAbsent(operationType, k -> PerformanceData.builder().build());
        data.recordOperation(executionTime, success, memoryDelta);
        
        if (error != null) {
            data.recordError(error);
        }
        
        totalOperations.incrementAndGet();
        totalTime.addAndGet(executionTime);
        
        // Log performance warnings
        if (executionTime > 5000) { // More than 5 seconds
            log.warn("Very slow serialization operation: {} took {}ms", operationType, executionTime);
        } else if (executionTime > 1000) { // More than 1 second
            log.debug("Slow serialization operation: {} took {}ms", operationType, executionTime);
        }
    }
    
    /**
     * Get comprehensive performance analysis with recommendations
     */
    public static PerformanceAnalysis getPerformanceAnalysis() {
        Map<String, PerformanceAnalysis.OperationMetrics> operationMetrics = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Analyze each operation type
        for (Map.Entry<String, PerformanceData> entry : performanceHistory.entrySet()) {
            String operationType = entry.getKey();
            PerformanceData data = entry.getValue();
            
            PerformanceAnalysis.OperationMetrics metrics = PerformanceAnalysis.OperationMetrics.builder()
                    .operationType(operationType)
                    .averageTime(data.getAverageExecutionTime())
                    .successRate(data.getSuccessRate())
                    .operationCount(data.operationCount.get())
                    .maxTime(data.maxExecutionTime.get())
                    .minTime(data.minExecutionTime.get() == Long.MAX_VALUE ? 0 : data.minExecutionTime.get())
                    .averageMemory(data.getAverageMemoryUsage())
                    .build();
            
            operationMetrics.put(operationType, metrics);
            
            // Generate operation-specific recommendations
            generateOperationRecommendations(operationType, data, recommendations, warnings);
        }
        
        // Overall analysis
        long totalOps = totalOperations.get();
        double overallAverage = totalOps > 0 ? (double) totalTime.get() / totalOps : 0.0;
        double overallSuccess = calculateOverallSuccessRate();
        
        // Memory analysis
        PerformanceAnalysis.MemoryAnalysis memoryAnalysis = analyzeMemoryUsage();
        
        // Optimization potential
        PerformanceAnalysis.OptimizationPotential optimizationPotential = calculateOptimizationPotential();
        
        // General recommendations
        generateGeneralRecommendations(recommendations, warnings, overallAverage, overallSuccess);
        
        return PerformanceAnalysis.builder()
                .overallAverageTime(overallAverage)
                .overallSuccessRate(overallSuccess)
                .totalOperations(totalOps)
                .operationMetrics(operationMetrics)
                .recommendations(recommendations)
                .warnings(warnings)
                .memoryAnalysis(memoryAnalysis)
                .optimizationPotential(optimizationPotential)
                .build();
    }
    
    /**
     * Generate operation-specific recommendations
     */
    private static void generateOperationRecommendations(String operationType, PerformanceData data, 
                                                        List<String> recommendations, List<String> warnings) {
        double avgTime = data.getAverageExecutionTime();
        double successRate = data.getSuccessRate();
        
        // Performance recommendations
        if (avgTime > 1000) {
            recommendations.add(operationType + ": Consider switching to optimized direct parsing (current avg: " + 
                              String.format("%.1f", avgTime) + "ms)");
        } else if (avgTime > 500) {
            recommendations.add(operationType + ": Performance could be improved with enhanced Jackson configuration");
        }
        
        // Success rate warnings
        if (successRate < 95.0) {
            warnings.add(operationType + ": Low success rate (" + String.format("%.1f", successRate) + 
                        "%) - review error handling and validation");
        } else if (successRate < 99.0) {
            recommendations.add(operationType + ": Consider adding pre-validation to improve success rate");
        }
        
        // Memory recommendations
        double avgMemory = data.getAverageMemoryUsage();
        if (avgMemory > 1024 * 1024) { // More than 1MB per operation
            recommendations.add(operationType + ": High memory usage (" + 
                              String.format("%.1f", avgMemory / 1024) + "KB avg) - consider streaming parser");
        }
        
        // Error analysis
        synchronized (data.recentErrors) {
            if (!data.recentErrors.isEmpty()) {
                Map<String, Long> errorCounts = data.recentErrors.stream()
                        .collect(Collectors.groupingBy(
                                error -> error.length() > 50 ? error.substring(0, 50) + "..." : error,
                                Collectors.counting()));
                
                String mostCommonError = errorCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("Unknown");
                
                recommendations.add(operationType + ": Most common error: " + mostCommonError);
            }
        }
    }
    
    /**
     * Generate general recommendations
     */
    private static void generateGeneralRecommendations(List<String> recommendations, List<String> warnings,
                                                      double overallAverage, double overallSuccess) {
        // Overall performance recommendations
        if (overallAverage > 500) {
            recommendations.add("GENERAL: Overall performance is slow (" + String.format("%.1f", overallAverage) + 
                              "ms avg) - prioritize migration to direct string->DTO parsing");
        }
        
        if (overallSuccess < 95.0) {
            warnings.add("GENERAL: Overall success rate is low (" + String.format("%.1f", overallSuccess) + 
                        "%) - review data validation and error handling");
        }
        
        // Migration recommendations
        long legacyOperations = performanceHistory.entrySet().stream()
                .filter(e -> e.getKey().contains("legacy") || e.getKey().contains("JSONObject"))
                .mapToLong(e -> e.getValue().operationCount.get())
                .sum();
        
        long directOperations = performanceHistory.entrySet().stream()
                .filter(e -> e.getKey().contains("direct") || e.getKey().contains("optimized"))
                .mapToLong(e -> e.getValue().operationCount.get())
                .sum();
        
        if (legacyOperations > directOperations) {
            recommendations.add("MIGRATION: " + legacyOperations + " legacy operations vs " + directOperations + 
                              " direct operations - accelerate migration to direct parsing");
        }
        
        // Resource recommendations
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
        
        if (memoryUsagePercent > 85) {
            warnings.add("MEMORY: High memory usage (" + String.format("%.1f", memoryUsagePercent) + 
                        "%) - consider memory optimization strategies");
        }
    }
    
    /**
     * Analyze memory usage patterns
     */
    private static PerformanceAnalysis.MemoryAnalysis analyzeMemoryUsage() {
        long totalMemoryUsed = performanceHistory.values().stream()
                .mapToLong(data -> data.memoryUsed.get())
                .sum();
        
        long totalOps = totalOperations.get();
        double averageMemoryPerOperation = totalOps > 0 ? (double) totalMemoryUsed / totalOps : 0.0;
        
        String efficiencyRating;
        if (averageMemoryPerOperation < 1024) {
            efficiencyRating = "Excellent";
        } else if (averageMemoryPerOperation < 10240) {
            efficiencyRating = "Good";
        } else if (averageMemoryPerOperation < 102400) {
            efficiencyRating = "Average";
        } else {
            efficiencyRating = "Poor";
        }
        
        List<String> memoryOptimizations = new ArrayList<>();
        if (averageMemoryPerOperation > 50000) {
            memoryOptimizations.add("Consider using streaming parser for large JSON documents");
            memoryOptimizations.add("Implement object pooling for frequently created DTOs");
        }
        if (averageMemoryPerOperation > 10000) {
            memoryOptimizations.add("Review Jackson configuration for memory-efficient settings");
            memoryOptimizations.add("Consider lazy loading for nested objects");
        }
        
        return PerformanceAnalysis.MemoryAnalysis.builder()
                .totalMemoryUsed(totalMemoryUsed)
                .averageMemoryPerOperation(averageMemoryPerOperation)
                .memoryEfficiencyRating(efficiencyRating)
                .memoryOptimizations(memoryOptimizations)
                .build();
    }
    
    /**
     * Calculate optimization potential
     */
    private static PerformanceAnalysis.OptimizationPotential calculateOptimizationPotential() {
        // Compare legacy vs direct performance
        PerformanceData legacyData = performanceHistory.get("legacy");
        PerformanceData directData = performanceHistory.get("direct");
        
        double expectedSpeedup = 1.0;
        if (legacyData != null && directData != null) {
            double legacyAvg = legacyData.getAverageExecutionTime();
            double directAvg = directData.getAverageExecutionTime();
            if (directAvg > 0) {
                expectedSpeedup = legacyAvg / directAvg;
            }
        } else {
            expectedSpeedup = 2.5; // Estimated based on typical Jackson performance improvements
        }
        
        double memoryReduction = 15.0; // Estimated 15% memory reduction from eliminating JSONObject
        
        List<String> optimizationStrategies = new ArrayList<>();
        optimizationStrategies.add("Migrate to direct string->DTO parsing using EnhancedJsonUtils");
        optimizationStrategies.add("Implement custom deserializers for complex types");
        optimizationStrategies.add("Use streaming parser for large documents");
        optimizationStrategies.add("Enable Jackson performance optimizations");
        optimizationStrategies.add("Add validation caching for repeated patterns");
        
        String migrationRecommendation;
        if (expectedSpeedup > 3.0) {
            migrationRecommendation = "HIGH PRIORITY: Significant performance gains expected";
        } else if (expectedSpeedup > 2.0) {
            migrationRecommendation = "MEDIUM PRIORITY: Moderate performance improvements possible";
        } else {
            migrationRecommendation = "LOW PRIORITY: Minor performance improvements expected";
        }
        
        return PerformanceAnalysis.OptimizationPotential.builder()
                .expectedSpeedup(expectedSpeedup)
                .memoryReduction(memoryReduction)
                .optimizationStrategies(optimizationStrategies)
                .migrationRecommendation(migrationRecommendation)
                .build();
    }
    
    /**
     * Calculate overall success rate
     */
    private static double calculateOverallSuccessRate() {
        long totalSuccess = performanceHistory.values().stream()
                .mapToLong(data -> data.successCount.get())
                .sum();
        
        long totalOps = totalOperations.get();
        return totalOps > 0 ? (double) totalSuccess / totalOps * 100.0 : 100.0;
    }
    
    /**
     * Get top performing operations
     */
    public static List<String> getTopPerformingOperations(int limit) {
        return performanceHistory.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().getAverageExecutionTime(), 
                                                  e2.getValue().getAverageExecutionTime()))
                .limit(limit)
                .map(e -> e.getKey() + " (avg: " + String.format("%.1f", e.getValue().getAverageExecutionTime()) + "ms)")
                .collect(Collectors.toList());
    }
    
    /**
     * Get worst performing operations
     */
    public static List<String> getWorstPerformingOperations(int limit) {
        return performanceHistory.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAverageExecutionTime(), 
                                                  e1.getValue().getAverageExecutionTime()))
                .limit(limit)
                .map(e -> e.getKey() + " (avg: " + String.format("%.1f", e.getValue().getAverageExecutionTime()) + "ms)")
                .collect(Collectors.toList());
    }
    
    /**
     * Reset all performance data
     */
    public static void resetPerformanceData() {
        performanceHistory.clear();
        totalOperations.set(0);
        totalTime.set(0);
        log.info("Serialization performance data reset");
    }
    
    /**
     * Export performance data for external analysis
     */
    public static Map<String, Object> exportPerformanceData() {
        Map<String, Object> export = new HashMap<>();
        export.put("totalOperations", totalOperations.get());
        export.put("totalTime", totalTime.get());
        
        Map<String, Object> operationData = new HashMap<>();
        for (Map.Entry<String, PerformanceData> entry : performanceHistory.entrySet()) {
            PerformanceData data = entry.getValue();
            Map<String, Object> opData = new HashMap<>();
            opData.put("count", data.operationCount.get());
            opData.put("totalTime", data.totalExecutionTime.get());
            opData.put("averageTime", data.getAverageExecutionTime());
            opData.put("maxTime", data.maxExecutionTime.get());
            opData.put("minTime", data.minExecutionTime.get());
            opData.put("successRate", data.getSuccessRate());
            opData.put("memoryUsed", data.memoryUsed.get());
            operationData.put(entry.getKey(), opData);
        }
        export.put("operations", operationData);
        
        return export;
    }
}