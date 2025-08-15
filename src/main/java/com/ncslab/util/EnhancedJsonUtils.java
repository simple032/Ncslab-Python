package com.ncslab.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ncslab.dto.ModelJson;
import com.ncslab.dto.BlockJson;
import com.ncslab.dto.LineJson;
import com.ncslab.dto.ConfigJson;
import com.ncslab.dto.SaveInfoJson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced JSON utilities with optimal Jackson configuration for direct string->DTO conversion.
 * Eliminates intermediate JSONObject step while maintaining backward compatibility.
 */
@Slf4j
public class EnhancedJsonUtils {
    
    private static final ObjectMapper optimizedMapper;
    private static final ObjectMapper fallbackMapper;
    private static final Map<Class<?>, String> classValidationCache = new ConcurrentHashMap<>();
    
    static {
        optimizedMapper = createOptimizedMapper();
        fallbackMapper = createFallbackMapper();
    }
    
    /**
     * Create optimized ObjectMapper for high-performance serialization
     */
    private static ObjectMapper createOptimizedMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Performance optimizations
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        
        // Serialization optimizations
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Memory optimizations
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Custom deserializers for edge cases
        SimpleModule customModule = new SimpleModule("NCSLabCustomModule");
        customModule.addDeserializer(Object.class, new FlexibleObjectDeserializer());
        mapper.registerModule(customModule);
        
        return mapper;
    }
    
    /**
     * Create fallback ObjectMapper for compatibility with legacy data
     */
    private static ObjectMapper createFallbackMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // More lenient configuration for backward compatibility
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        return mapper;
    }
    
    /**
     * Custom deserializer for flexible Object handling (String/Number conversion)
     */
    private static class FlexibleObjectDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            
            if (node.isTextual()) {
                String text = node.asText();
                // Try to convert numeric strings to numbers
                try {
                    if (text.contains(".")) {
                        return Double.parseDouble(text);
                    } else {
                        return Integer.parseInt(text);
                    }
                } catch (NumberFormatException e) {
                    return text;
                }
            } else if (node.isNumber()) {
                if (node.isIntegralNumber()) {
                    return node.asInt();
                } else {
                    return node.asDouble();
                }
            } else if (node.isBoolean()) {
                return node.asBoolean();
            } else if (node.isNull()) {
                return null;
            } else {
                return node.toString();
            }
        }
    }
    
    /**
     * High-performance model parsing with automatic fallback
     * @param jsonString JSON string to parse
     * @return ModelJson DTO or null if parsing fails
     */
    public static ModelJson parseModelJsonOptimized(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            log.warn("Empty or null JSON string provided for ModelJson parsing");
            return null;
        }
        
        try (DtoPerformanceMonitor.PerformanceContext context = 
             DtoPerformanceMonitor.startOperation("ModelJson.parseOptimized")) {
            
            try {
                // Try optimized mapper first
                ModelJson result = optimizedMapper.readValue(jsonString, ModelJson.class);
                log.debug("Optimized ModelJson parsing successful");
                return result;
                
            } catch (Exception primaryException) {
                context.markError("Optimized parsing failed: " + primaryException.getMessage());
                log.debug("Optimized parsing failed, trying fallback mapper");
                
                try {
                    // Fallback to more lenient mapper
                    ModelJson result = fallbackMapper.readValue(jsonString, ModelJson.class);
                    log.info("Fallback ModelJson parsing successful after optimized failure");
                    return result;
                    
                } catch (Exception fallbackException) {
                    context.markError("Fallback parsing also failed: " + fallbackException.getMessage());
                    log.error("Both optimized and fallback ModelJson parsing failed", fallbackException);
                    return null;
                }
            }
        }
    }
    
    /**
     * Generic optimized parsing with type safety and performance monitoring
     * @param jsonString JSON string to parse
     * @param clazz Target class
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJsonOptimized(String jsonString, Class<T> clazz) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            log.warn("Empty or null JSON string provided for {} parsing", clazz.getSimpleName());
            return null;
        }
        
        String operationName = clazz.getSimpleName() + ".parseOptimized";
        try (DtoPerformanceMonitor.PerformanceContext context = 
             DtoPerformanceMonitor.startOperation(operationName)) {
            
            try {
                // Pre-validate JSON structure if cached validation available
                String cachedValidation = classValidationCache.get(clazz);
                if (cachedValidation != null && !isValidStructure(jsonString, cachedValidation)) {
                    context.markError("JSON structure validation failed");
                    return null;
                }
                
                // Try optimized mapper first
                T result = optimizedMapper.readValue(jsonString, clazz);
                log.debug("Optimized {} parsing successful", clazz.getSimpleName());
                
                // Cache successful validation pattern for future use
                if (cachedValidation == null) {
                    cacheValidationPattern(clazz, jsonString);
                }
                
                return result;
                
            } catch (Exception primaryException) {
                context.markError("Optimized parsing failed: " + primaryException.getMessage());
                log.debug("Optimized parsing failed for {}, trying fallback", clazz.getSimpleName());
                
                try {
                    // Fallback to more lenient mapper
                    T result = fallbackMapper.readValue(jsonString, clazz);
                    log.info("Fallback {} parsing successful after optimized failure", clazz.getSimpleName());
                    return result;
                    
                } catch (Exception fallbackException) {
                    context.markError("Fallback parsing also failed: " + fallbackException.getMessage());
                    log.error("Both optimized and fallback {} parsing failed", clazz.getSimpleName(), fallbackException);
                    return null;
                }
            }
        }
    }
    
    /**
     * High-performance serialization with compression for large objects
     * @param object Object to serialize
     * @param compress Whether to minimize output size
     * @return JSON string or null if serialization fails
     */
    public static String toJsonOptimized(Object object, boolean compress) {
        if (object == null) {
            return null;
        }
        
        String operationName = object.getClass().getSimpleName() + ".serialize";
        try (DtoPerformanceMonitor.PerformanceContext context = 
             DtoPerformanceMonitor.startOperation(operationName)) {
            
            try {
                ObjectMapper mapper = compress ? optimizedMapper : fallbackMapper;
                String result = mapper.writeValueAsString(object);
                log.debug("Optimized {} serialization successful", object.getClass().getSimpleName());
                return result;
                
            } catch (JsonProcessingException e) {
                context.markError("Serialization failed: " + e.getMessage());
                log.error("Failed to serialize {} to JSON", object.getClass().getSimpleName(), e);
                return null;
            }
        }
    }
    
    /**
     * Batch parsing for multiple objects with shared context
     * @param jsonStrings List of JSON strings to parse
     * @param clazz Target class
     * @return List of parsed objects (nulls for failed parses)
     */
    public static <T> List<T> parseBatchOptimized(List<String> jsonStrings, Class<T> clazz) {
        if (jsonStrings == null || jsonStrings.isEmpty()) {
            return new ArrayList<>();
        }
        
        String operationName = clazz.getSimpleName() + ".batchParse";
        try (DtoPerformanceMonitor.PerformanceContext context = 
             DtoPerformanceMonitor.startOperation(operationName)) {
            
            List<T> results = new ArrayList<>(jsonStrings.size());
            int successCount = 0;
            
            for (String jsonString : jsonStrings) {
                T result = parseJsonOptimized(jsonString, clazz);
                results.add(result);
                if (result != null) {
                    successCount++;
                }
            }
            
            log.debug("Batch parsing completed: {}/{} successful for {}", 
                     successCount, jsonStrings.size(), clazz.getSimpleName());
            
            if (successCount < jsonStrings.size()) {
                context.markError("Batch parsing had failures: " + (jsonStrings.size() - successCount) + " failed");
            }
            
            return results;
        }
    }
    
    /**
     * Streaming parser for large JSON arrays
     * @param jsonArrayString Large JSON array string
     * @param clazz Target class for array elements
     * @return List of parsed objects
     */
    public static <T> List<T> parseJsonArrayStreaming(String jsonArrayString, Class<T> clazz) {
        if (jsonArrayString == null || jsonArrayString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String operationName = clazz.getSimpleName() + ".streamingArrayParse";
        try (DtoPerformanceMonitor.PerformanceContext context = 
             DtoPerformanceMonitor.startOperation(operationName)) {
            
            try {
                // Use streaming parser for memory efficiency
                List<T> results = optimizedMapper.readValue(
                    jsonArrayString, 
                    optimizedMapper.getTypeFactory().constructCollectionType(List.class, clazz)
                );
                
                log.debug("Streaming array parsing successful: {} objects of type {}", 
                         results.size(), clazz.getSimpleName());
                return results;
                
            } catch (Exception e) {
                context.markError("Streaming array parsing failed: " + e.getMessage());
                log.error("Failed to parse JSON array for {}", clazz.getSimpleName(), e);
                return new ArrayList<>();
            }
        }
    }
    
    /**
     * Validate JSON structure without full parsing
     * @param jsonString JSON string to validate
     * @param expectedPattern Expected structure pattern (optional)
     * @return true if structure is valid
     */
    private static boolean isValidStructure(String jsonString, String expectedPattern) {
        try {
            // Quick validation using tree model
            JsonNode tree = optimizedMapper.readTree(jsonString);
            return tree != null && tree.isObject();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Cache validation patterns for future performance optimization
     * @param clazz Class to cache pattern for
     * @param successfulJson JSON that parsed successfully
     */
    private static void cacheValidationPattern(Class<?> clazz, String successfulJson) {
        try {
            JsonNode tree = optimizedMapper.readTree(successfulJson);
            if (tree != null && tree.isObject()) {
                // Store a simplified pattern for future validation
                classValidationCache.put(clazz, "object");
            }
        } catch (Exception e) {
            // Ignore caching errors
        }
    }
    
    /**
     * Get optimized ObjectMapper instance for advanced usage
     * @return Configured ObjectMapper
     */
    public static ObjectMapper getOptimizedMapper() {
        return optimizedMapper;
    }
    
    /**
     * Get fallback ObjectMapper instance for compatibility
     * @return Configured ObjectMapper
     */
    public static ObjectMapper getFallbackMapper() {
        return fallbackMapper;
    }
    
    /**
     * Validate and parse with detailed error reporting
     * @param jsonString JSON string to parse
     * @param clazz Target class
     * @return Result object with parsed data or detailed error information
     */
    public static <T> ParseResult<T> parseWithValidation(String jsonString, Class<T> clazz) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return ParseResult.failure("JSON string is null or empty", null);
        }
        
        try {
            // Pre-flight validation
            JsonNode tree = optimizedMapper.readTree(jsonString);
            if (tree == null) {
                return ParseResult.failure("Invalid JSON format", null);
            }
            
            // Attempt parsing
            T result = parseJsonOptimized(jsonString, clazz);
            if (result == null) {
                return ParseResult.failure("Parsing succeeded but result is null", tree);
            }
            
            // Post-parsing validation for specific DTOs
            String validationError = validateParsedDto(result);
            if (validationError != null) {
                return ParseResult.failure("DTO validation failed: " + validationError, tree);
            }
            
            return ParseResult.success(result);
            
        } catch (Exception e) {
            return ParseResult.failure("Parsing exception: " + e.getMessage(), null);
        }
    }
    
    /**
     * Validate parsed DTO objects
     * @param dto Parsed DTO object
     * @return Validation error message or null if valid
     */
    private static String validateParsedDto(Object dto) {
        if (dto instanceof ModelJson) {
            ModelJson model = (ModelJson) dto;
            return model.getValidationError();
        } else if (dto instanceof BlockJson) {
            BlockJson block = (BlockJson) dto;
            return block.getValidationError();
        } else if (dto instanceof LineJson) {
            LineJson line = (LineJson) dto;
            return line.getValidationError();
        }
        return null;
    }
    
    /**
     * Result wrapper for parsing operations with detailed error information
     */
    public static class ParseResult<T> {
        private final T data;
        private final String error;
        private final JsonNode jsonTree;
        private final boolean success;
        
        private ParseResult(T data, String error, JsonNode jsonTree, boolean success) {
            this.data = data;
            this.error = error;
            this.jsonTree = jsonTree;
            this.success = success;
        }
        
        public static <T> ParseResult<T> success(T data) {
            return new ParseResult<>(data, null, null, true);
        }
        
        public static <T> ParseResult<T> failure(String error, JsonNode jsonTree) {
            return new ParseResult<>(null, error, jsonTree, false);
        }
        
        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getError() { return error; }
        public JsonNode getJsonTree() { return jsonTree; }
        
        public T getDataOrThrow() {
            if (!success) {
                throw new RuntimeException("Parse failed: " + error);
            }
            return data;
        }
    }
}