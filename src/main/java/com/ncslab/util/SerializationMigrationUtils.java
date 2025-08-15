package com.ncslab.util;

import com.ncslab.dto.ModelJson;
import com.ncslab.dto.BlockJson;
import com.ncslab.dto.LineJson;
import com.ncslab.dto.OptimizedModelJson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Migration utilities for transitioning from JSONObject->DTO to direct string->DTO conversion.
 * Provides backward compatibility and gradual migration patterns.
 */
@Slf4j
public class SerializationMigrationUtils {
    
    private static final ExecutorService migrationExecutor = Executors.newCachedThreadPool();
    
    /**
     * Migration result wrapper
     */
    public static class MigrationResult<T> {
        private final T result;
        private final String method;
        private final long processingTime;
        private final boolean success;
        private final String error;
        
        private MigrationResult(T result, String method, long processingTime, boolean success, String error) {
            this.result = result;
            this.method = method;
            this.processingTime = processingTime;
            this.success = success;
            this.error = error;
        }
        
        public static <T> MigrationResult<T> success(T result, String method, long processingTime) {
            return new MigrationResult<>(result, method, processingTime, true, null);
        }
        
        public static <T> MigrationResult<T> failure(String method, long processingTime, String error) {
            return new MigrationResult<>(null, method, processingTime, false, error);
        }
        
        public T getResult() { return result; }
        public String getMethod() { return method; }
        public long getProcessingTime() { return processingTime; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        
        public T getResultOrThrow() {
            if (!success) {
                throw new RuntimeException("Migration failed (" + method + "): " + error);
            }
            return result;
        }
    }
    
    /**
     * Pattern 1: Direct Migration with Fallback
     * Try optimized direct parsing first, fall back to legacy if needed
     */
    public static MigrationResult<ModelJson> parseModelWithFallback(String jsonString) {
        long startTime = System.nanoTime();
        
        // Try optimized direct parsing first
        try {
            ModelJson result = EnhancedJsonUtils.parseModelJsonOptimized(jsonString);
            if (result != null && result.isValid()) {
                long duration = (System.nanoTime() - startTime) / 1_000_000;
                log.debug("Direct parsing successful in {}ms", duration);
                return MigrationResult.success(result, "direct", duration);
            }
        } catch (Exception e) {
            log.debug("Direct parsing failed, trying legacy: {}", e.getMessage());
        }
        
        // Fallback to legacy JSONObject approach
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            ModelJson result = ModelJson.fromLegacyJson(jsonObject);
            if (result != null && result.isValid()) {
                long duration = (System.nanoTime() - startTime) / 1_000_000;
                log.info("Legacy parsing successful in {}ms after direct failure", duration);
                return MigrationResult.success(result, "legacy", duration);
            }
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Both direct and legacy parsing failed", e);
            return MigrationResult.failure("both", duration, "Direct and legacy parsing failed: " + e.getMessage());
        }
        
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        return MigrationResult.failure("validation", duration, "Parsing succeeded but validation failed");
    }
    
    /**
     * Pattern 2: Async Migration for Non-Critical Operations
     * Process using both methods asynchronously and compare results
     */
    public static CompletableFuture<MigrationResult<ModelJson>> parseModelAsync(String jsonString) {
        return CompletableFuture.supplyAsync(() -> {
            return parseModelWithFallback(jsonString);
        }, migrationExecutor);
    }
    
    /**
     * Pattern 3: Batch Migration for Multiple Objects
     * Process lists of JSON strings efficiently
     */
    public static List<MigrationResult<ModelJson>> parseModelsBatch(List<String> jsonStrings) {
        List<MigrationResult<ModelJson>> results = new ArrayList<>();
        
        for (String jsonString : jsonStrings) {
            results.add(parseModelWithFallback(jsonString));
        }
        
        // Log batch statistics
        long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        long directSuccessCount = results.stream().mapToLong(r -> r.isSuccess() && "direct".equals(r.getMethod()) ? 1 : 0).sum();
        
        log.info("Batch migration completed: {}/{} successful, {}/{} used direct parsing", 
                successCount, results.size(), directSuccessCount, successCount);
        
        return results;
    }
    
    /**
     * Pattern 4: Smart Migration Based on JSON Structure
     * Analyze JSON structure to choose optimal parsing method
     */
    public static MigrationResult<ModelJson> parseModelSmart(String jsonString) {
        long startTime = System.nanoTime();
        
        // Quick structure analysis
        JsonStructureInfo structureInfo = analyzeJsonStructure(jsonString);
        
        if (structureInfo.isOptimalForDirect()) {
            // Use direct parsing for well-structured JSON
            try {
                ModelJson result = EnhancedJsonUtils.parseModelJsonOptimized(jsonString);
                if (result != null && result.isValid()) {
                    long duration = (System.nanoTime() - startTime) / 1_000_000;
                    return MigrationResult.success(result, "smart-direct", duration);
                }
            } catch (Exception e) {
                log.debug("Smart direct parsing failed: {}", e.getMessage());
            }
        }
        
        // Use legacy parsing for complex or problematic JSON
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            ModelJson result = ModelJson.fromLegacyJson(jsonObject);
            if (result != null && result.isValid()) {
                long duration = (System.nanoTime() - startTime) / 1_000_000;
                return MigrationResult.success(result, "smart-legacy", duration);
            }
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            return MigrationResult.failure("smart-both", duration, "Smart parsing failed: " + e.getMessage());
        }
        
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        return MigrationResult.failure("smart-validation", duration, "Smart parsing succeeded but validation failed");
    }
    
    /**
     * Pattern 5: Progressive Migration with Metrics
     * Gradually migrate while collecting performance data
     */
    public static MigrationResult<ModelJson> parseModelProgressive(String jsonString, boolean preferDirect) {
        if (preferDirect) {
            // Try direct first, measure performance
            MigrationResult<ModelJson> directResult = tryDirectParsing(jsonString);
            if (directResult.isSuccess()) {
                return directResult;
            }
            
            // Fall back to legacy if direct fails
            log.info("Direct parsing failed, falling back to legacy for progressive migration");
            return tryLegacyParsing(jsonString);
        } else {
            // Use legacy by default, but measure direct parsing in background for comparison
            MigrationResult<ModelJson> legacyResult = tryLegacyParsing(jsonString);
            
            // Async comparison for metrics (don't block main thread)
            CompletableFuture.runAsync(() -> {
                try {
                    MigrationResult<ModelJson> directResult = tryDirectParsing(jsonString);
                    compareMigrationResults(legacyResult, directResult);
                } catch (Exception e) {
                    log.debug("Background direct parsing comparison failed", e);
                }
            }, migrationExecutor);
            
            return legacyResult;
        }
    }
    
    /**
     * Helper: Try direct parsing only
     */
    private static MigrationResult<ModelJson> tryDirectParsing(String jsonString) {
        long startTime = System.nanoTime();
        try {
            ModelJson result = EnhancedJsonUtils.parseModelJsonOptimized(jsonString);
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            if (result != null && result.isValid()) {
                return MigrationResult.success(result, "direct", duration);
            } else {
                return MigrationResult.failure("direct", duration, "Parsing succeeded but result invalid");
            }
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            return MigrationResult.failure("direct", duration, e.getMessage());
        }
    }
    
    /**
     * Helper: Try legacy parsing only
     */
    private static MigrationResult<ModelJson> tryLegacyParsing(String jsonString) {
        long startTime = System.nanoTime();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            ModelJson result = ModelJson.fromLegacyJson(jsonObject);
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            if (result != null && result.isValid()) {
                return MigrationResult.success(result, "legacy", duration);
            } else {
                return MigrationResult.failure("legacy", duration, "Parsing succeeded but result invalid");
            }
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            return MigrationResult.failure("legacy", duration, e.getMessage());
        }
    }
    
    /**
     * Compare migration results for performance analysis
     */
    private static void compareMigrationResults(MigrationResult<ModelJson> legacy, MigrationResult<ModelJson> direct) {
        if (legacy.isSuccess() && direct.isSuccess()) {
            double speedupRatio = (double) legacy.getProcessingTime() / direct.getProcessingTime();
            log.info("Migration comparison: Direct {}ms vs Legacy {}ms ({}x speedup)", 
                    direct.getProcessingTime(), legacy.getProcessingTime(), 
                    String.format("%.2f", speedupRatio));
        } else {
            log.debug("Migration comparison: Legacy={}, Direct={}", 
                     legacy.isSuccess() ? "success" : "failed", 
                     direct.isSuccess() ? "success" : "failed");
        }
    }
    
    /**
     * Analyze JSON structure to determine optimal parsing method
     */
    private static JsonStructureInfo analyzeJsonStructure(String jsonString) {
        try {
            // Quick checks without full parsing
            boolean hasSpecialChars = jsonString.contains("\\") || jsonString.contains("\n") || jsonString.contains("\r");
            boolean hasDeepNesting = countOccurrences(jsonString, '{') > 10;
            boolean hasLargeArrays = countOccurrences(jsonString, '[') > 5;
            boolean isComplex = jsonString.length() > 50000;
            
            return new JsonStructureInfo(!hasSpecialChars, !hasDeepNesting, !hasLargeArrays, !isComplex);
        } catch (Exception e) {
            return new JsonStructureInfo(false, false, false, false);
        }
    }
    
    private static int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * JSON structure analysis result
     */
    private static class JsonStructureInfo {
        private final boolean simpleFormat;
        private final boolean shallowNesting;
        private final boolean smallArrays;
        private final boolean reasonableSize;
        
        public JsonStructureInfo(boolean simpleFormat, boolean shallowNesting, boolean smallArrays, boolean reasonableSize) {
            this.simpleFormat = simpleFormat;
            this.shallowNesting = shallowNesting;
            this.smallArrays = smallArrays;
            this.reasonableSize = reasonableSize;
        }
        
        public boolean isOptimalForDirect() {
            return simpleFormat && shallowNesting && smallArrays && reasonableSize;
        }
    }
    
    /**
     * Migration compatibility checker
     */
    public static class CompatibilityChecker {
        
        /**
         * Check if JSON string is compatible with direct parsing
         */
        public static boolean isDirectParsingCompatible(String jsonString) {
            try {
                // Quick validation checks
                if (jsonString == null || jsonString.trim().isEmpty()) {
                    return false;
                }
                
                // Check for known problematic patterns
                if (jsonString.contains("\\u") || jsonString.contains("\\n") || jsonString.contains("\\r")) {
                    return false; // Unicode escapes or control characters
                }
                
                // Basic structure validation
                int braceCount = 0;
                boolean inString = false;
                for (int i = 0; i < jsonString.length(); i++) {
                    char c = jsonString.charAt(i);
                    if (c == '"' && (i == 0 || jsonString.charAt(i-1) != '\\')) {
                        inString = !inString;
                    } else if (!inString) {
                        if (c == '{') braceCount++;
                        else if (c == '}') braceCount--;
                    }
                }
                
                return braceCount == 0; // Balanced braces
                
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * Get compatibility score (0-100)
         */
        public static int getCompatibilityScore(String jsonString) {
            int score = 100;
            
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return 0;
            }
            
            // Size penalty
            if (jsonString.length() > 100000) score -= 20;
            else if (jsonString.length() > 50000) score -= 10;
            
            // Complexity penalty
            if (countOccurrences(jsonString, '{') > 20) score -= 15;
            if (countOccurrences(jsonString, '[') > 10) score -= 10;
            
            // Special character penalty
            if (jsonString.contains("\\u")) score -= 15;
            if (jsonString.contains("\\n") || jsonString.contains("\\r")) score -= 10;
            if (jsonString.contains("\\t")) score -= 5;
            
            return Math.max(0, score);
        }
    }
    
    /**
     * Cleanup migration executor on shutdown
     */
    public static void shutdown() {
        migrationExecutor.shutdown();
    }
}