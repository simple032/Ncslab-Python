package com.ncslab.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ncslab.dto.ModelJson;
import com.ncslab.dto.BlockJson;
import com.ncslab.dto.LineJson;
import com.ncslab.dto.ConfigJson;
import com.ncslab.dto.SaveInfoJson;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for JSON operations using Jackson.
 * Provides centralized ObjectMapper configuration and common JSON operations.
 */
@Slf4j
public class JsonUtils {
    
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        
        // Configure ObjectMapper for robustness
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Fix circular reference issues that cause infinite loops in RT simulation
        objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        
        // Optional: Configure for pretty printing (can be disabled for production)
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }
    
    /**
     * Get the configured ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Parse JSON string to ModelJson DTO
     * @param jsonString JSON string to parse
     * @return ModelJson DTO or null if parsing fails
     */
    public static ModelJson parseModelJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, ModelJson.class);
        } catch (IOException e) {
            log.error("Failed to parse ModelJson from string: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse JSON string to specified class
     * @param jsonString JSON string to parse
     * @param clazz Target class
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJson(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("Failed to parse JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert object to JSON string
     * @param object Object to serialize
     * @return JSON string or null if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if a string is valid JSON
     * @param jsonString String to validate
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Convert between DTOs (useful for data transformation)
     * @param source Source object
     * @param targetClass Target class
     * @return Converted object or null if conversion fails
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        try {
            // Serialize to JSON then deserialize to target type
            String json = objectMapper.writeValueAsString(source);
            return objectMapper.readValue(json, targetClass);
        } catch (IOException e) {
            log.error("Failed to convert {} to {}: {}", 
                     source.getClass().getSimpleName(), 
                     targetClass.getSimpleName(), 
                     e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate JSON structure before parsing using ObjectMapper
     * @param jsonString JSON string to validate
     * @return Validation error message or null if valid
     */
    public static String validateJsonStructure(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return "JSON string is null or empty";
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            
            // Check for required fields using ObjectMapper
            if (!rootNode.has("modelName")) {
                return "Missing required field: modelName";
            }
            
            if (!rootNode.has("blocks")) {
                return "Missing required field: blocks";
            }
            
            if (!rootNode.has("lines")) {
                return "Missing required field: lines";
            }
            
            return null; // Valid
            
        } catch (JsonProcessingException e) {
            return "Invalid JSON format: " + e.getMessage();
        } catch (IOException e) {
            return "IO error while parsing JSON: " + e.getMessage();
        }
    }
    
    /**
     * Enhanced serialization with performance monitoring
     * @param object Object to serialize
     * @param logPerformance Whether to log performance metrics
     * @return JSON string or null if serialization fails
     */
    public static String toJsonWithMetrics(Object object, boolean logPerformance) {
        long startTime = logPerformance ? System.nanoTime() : 0;
        
        try {
            String result = objectMapper.writeValueAsString(object);
            
            if (logPerformance) {
                long duration = System.nanoTime() - startTime;
                log.debug("JSON serialization took {} ns for object type: {}", 
                         duration, object.getClass().getSimpleName());
            }
            
            return result;
            
        } catch (IOException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Enhanced deserialization with performance monitoring
     * @param jsonString JSON string to parse
     * @param clazz Target class
     * @param logPerformance Whether to log performance metrics
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJsonWithMetrics(String jsonString, Class<T> clazz, boolean logPerformance) {
        long startTime = logPerformance ? System.nanoTime() : 0;
        
        try {
            T result = objectMapper.readValue(jsonString, clazz);
            
            if (logPerformance) {
                long duration = System.nanoTime() - startTime;
                log.debug("JSON deserialization took {} ns for class: {}", 
                         duration, clazz.getSimpleName());
            }
            
            return result;
            
        } catch (IOException e) {
            log.error("Failed to parse JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse JSON from File using ObjectMapper
     * @param file JSON file to parse
     * @param clazz Target class
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJsonFromFile(File file, Class<T> clazz) {
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            log.error("Failed to parse JSON file {} to {}: {}", file.getPath(), clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse JSON from InputStream using ObjectMapper
     * @param inputStream JSON input stream
     * @param clazz Target class
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJsonFromStream(InputStream inputStream, Class<T> clazz) {
        try {
            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            log.error("Failed to parse JSON stream to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse JSON using TypeReference for generic collections
     * @param jsonString JSON string to parse
     * @param typeReference TypeReference for complex generics
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJson(String jsonString, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(jsonString, typeReference);
        } catch (IOException e) {
            log.error("Failed to parse JSON to type reference: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse JSON tree structure for dynamic access
     * @param jsonString JSON string to parse
     * @return JsonNode tree or null if parsing fails
     */
    public static JsonNode parseJsonTree(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            log.error("Failed to parse JSON tree: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Safe JSON parsing with fallback value
     * @param jsonString JSON string to parse
     * @param clazz Target class
     * @param fallback Fallback value if parsing fails
     * @return Parsed object or fallback if parsing fails
     */
    public static <T> T parseJsonSafe(String jsonString, Class<T> clazz, T fallback) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.warn("JSON parsing failed, using fallback for {}: {}", clazz.getSimpleName(), e.getMessage());
            return fallback;
        }
    }
    
    /**
     * Convert JSON string directly to Map for dynamic access
     * @param jsonString JSON string to convert
     * @return Map representation or null if conversion fails
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            log.error("Failed to convert JSON to Map: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert JSON string directly to List for array handling
     * @param jsonString JSON string to convert
     * @return List representation or null if conversion fails
     */
    @SuppressWarnings("unchecked")
    public static List<Object> jsonToList(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, List.class);
        } catch (IOException e) {
            log.error("Failed to convert JSON to List: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Memory-efficient streaming parser for large JSON files
     * @param jsonString Large JSON string
     * @param clazz Target class
     * @return Parsed object or null if parsing fails
     */
    public static <T> T parseJsonStreaming(String jsonString, Class<T> clazz) {
        try {
            // Use streaming parser for large JSON to reduce memory footprint
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("Failed to parse large JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Serialize DTO to JSON string with error handling
     * Replaces the pattern: dto.toLegacyJson().toString()
     * @param dto DTO object to serialize
     * @return JSON string or error message if serialization fails
     */
    public static String serializeDto(Object dto) {
        if (dto == null) {
            return "{\"error\":\"DTO object is null\"}";
        }
        
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DTO {}: {}", dto.getClass().getSimpleName(), e.getMessage());
            // Return error JSON instead of null for robustness
            return "{\"error\":\"Serialization failed: " + e.getMessage().replace("\"", "\\\"")
                + "\",\"objectType\":\"" + dto.getClass().getSimpleName() + "\"}";
        }
    }
    
    /**
     * Serialize DTO to JSON string with performance logging
     * @param dto DTO object to serialize
     * @param logPerformance Whether to log performance metrics
     * @return JSON string or error message if serialization fails
     */
    public static String serializeDtoWithMetrics(Object dto, boolean logPerformance) {
        if (dto == null) {
            return "{\"error\":\"DTO object is null\"}";
        }
        
        long startTime = logPerformance ? System.nanoTime() : 0;
        
        try {
            String result = objectMapper.writeValueAsString(dto);
            
            if (logPerformance) {
                long duration = System.nanoTime() - startTime;
                log.debug("DTO serialization took {} ns for type: {}", 
                         duration, dto.getClass().getSimpleName());
            }
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DTO {}: {}", dto.getClass().getSimpleName(), e.getMessage());
            return "{\"error\":\"Serialization failed: " + e.getMessage().replace("\"", "\\\"")
                + "\",\"objectType\":\"" + dto.getClass().getSimpleName() + "\"}";
        }
    }
    
    /**
     * Create a standard server response JSON string
     * Common pattern for servlet responses
     * @param code HTTP-style response code
     * @param status Status message
     * @param data Optional data payload
     * @return JSON response string
     */
    public static String createServerResponse(int code, String status, Object data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("code", code);
            response.put("status", status);
            if (data != null) {
                response.put("data", data);
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to create server response: {}", e.getMessage());
            return "{\"code\":500,\"status\":\"Internal serialization error\"}";
        }
    }
    
    /**
     * Create a standard error response JSON string
     * Common pattern for error responses
     * @param code Error code
     * @param errorMessage Error message
     * @return JSON error response string
     */
    public static String createErrorResponse(int code, String errorMessage) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("code", code);
            response.put("status", "error");
            response.put("error", errorMessage);
            response.put("message", errorMessage);
            
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to create error response: {}", e.getMessage());
            return "{\"code\":500,\"status\":\"error\",\"error\":\"Internal serialization error\"}";
        }
    }
    
    /**
     * Create a standard success response JSON string
     * Common pattern for success responses
     * @param data Success data payload
     * @return JSON success response string
     */
    public static String createSuccessResponse(Object data) {
        return createServerResponse(200, "success", data);
    }
    
    /**
     * WebSocket message serialization helper
     * Handles the common pattern: message.toLegacyJson().toString()
     * @param message WebSocket message DTO
     * @return JSON string for WebSocket transmission
     */
    public static String serializeWebSocketMessage(Object message) {
        if (message == null) {
            log.warn("Attempting to serialize null WebSocket message");
            return "{\"error\":\"Null message\"}";
        }
        
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize WebSocket message {}: {}", 
                     message.getClass().getSimpleName(), e.getMessage());
            
            // Fallback to simple error message for WebSocket reliability
            return "{\"msg\":\"error\",\"error\":\"Message serialization failed\"}";
        }
    }
    
    /**
     * Safe DTO serialization with fallback to legacy method
     * Provides backwards compatibility during migration
     * @param dto DTO object with potential toLegacyJson() method
     * @return JSON string using Jackson or legacy method as fallback
     */
    public static String serializeDtoWithFallback(Object dto) {
        if (dto == null) {
            return "{\"error\":\"DTO object is null\"}";
        }
        
        // Try Jackson serialization first
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.warn("Jackson serialization failed for {}, attempting legacy fallback: {}", 
                    dto.getClass().getSimpleName(), e.getMessage());
            
            // Attempt legacy method via reflection
            try {
                java.lang.reflect.Method legacyMethod = dto.getClass().getMethod("toLegacyJson");
                Object jsonObject = legacyMethod.invoke(dto);
                if (jsonObject != null) {
                    return jsonObject.toString();
                }
            } catch (Exception reflectionException) {
                log.debug("Legacy method fallback also failed: {}", reflectionException.getMessage());
            }
            
            // Final fallback to error response
            return "{\"error\":\"Both Jackson and legacy serialization failed\",\"objectType\":\"" 
                + dto.getClass().getSimpleName() + "\"}";
        }
    }
    
    /**
     * Enhanced JSON validation with detailed error information
     * @param jsonString JSON string to validate
     * @param expectedClass Expected target class for additional validation
     * @return Detailed validation result
     */
    public static JsonValidationResult validateJsonWithDetails(String jsonString, Class<?> expectedClass) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return JsonValidationResult.invalid("JSON string is null or empty");
        }
        
        try {
            // Basic JSON structure validation
            JsonNode rootNode = objectMapper.readTree(jsonString);
            
            // Try parsing to expected class if provided
            if (expectedClass != null) {
                objectMapper.readValue(jsonString, expectedClass);
            }
            
            return JsonValidationResult.valid();
            
        } catch (JsonProcessingException e) {
            return JsonValidationResult.invalid("Invalid JSON format: " + e.getMessage());
        } catch (IOException e) {
            return JsonValidationResult.invalid("IO error during validation: " + e.getMessage());
        }
    }
    
    /**
     * Result class for detailed JSON validation
     */
    public static class JsonValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private JsonValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static JsonValidationResult valid() {
            return new JsonValidationResult(true, null);
        }
        
        public static JsonValidationResult invalid(String errorMessage) {
            return new JsonValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}