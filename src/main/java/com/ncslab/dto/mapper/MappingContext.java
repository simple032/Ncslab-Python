package com.ncslab.dto.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe context management for DTO-Entity mapping operations.
 * Provides contextual information during mapping operations for debugging and error handling.
 */
public class MappingContext {
    
    private final Map<String, Object> contextData = new ConcurrentHashMap<>();
    private final ThreadLocal<String> currentOperation = new ThreadLocal<>();
    private final ThreadLocal<String> currentBlockType = new ThreadLocal<>();
    private final ThreadLocal<String> currentMappingId = new ThreadLocal<>();
    
    /**
     * Set the current mapping operation type
     * @param operation Operation type (e.g., "DTO_TO_ENTITY", "ENTITY_TO_DTO")
     */
    public void setCurrentOperation(String operation) {
        currentOperation.set(operation);
    }
    
    /**
     * Get the current mapping operation type
     * @return Current operation type or null if not set
     */
    public String getCurrentOperation() {
        return currentOperation.get();
    }
    
    /**
     * Set the current block type being processed
     * @param blockType Block type identifier
     */
    public void setCurrentBlockType(String blockType) {
        currentBlockType.set(blockType);
    }
    
    /**
     * Get the current block type being processed
     * @return Current block type or null if not set
     */
    public String getCurrentBlockType() {
        return currentBlockType.get();
    }
    
    /**
     * Set a unique identifier for the current mapping operation
     * @param mappingId Unique mapping identifier
     */
    public void setCurrentMappingId(String mappingId) {
        currentMappingId.set(mappingId);
    }
    
    /**
     * Get the current mapping operation identifier
     * @return Current mapping ID or null if not set
     */
    public String getCurrentMappingId() {
        return currentMappingId.get();
    }
    
    /**
     * Put context data that persists across threads
     * @param key Context data key
     * @param value Context data value
     */
    public void putContextData(String key, Object value) {
        if (key != null) {
            if (value != null) {
                contextData.put(key, value);
            } else {
                contextData.remove(key);
            }
        }
    }
    
    /**
     * Get context data with type safety
     * @param key Context data key
     * @param type Expected type of the value
     * @param <T> Type parameter
     * @return Typed value or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextData(String key, Class<T> type) {
        Object value = contextData.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
    
    /**
     * Get context data without type checking
     * @param key Context data key
     * @return Value or null if not found
     */
    public Object getContextData(String key) {
        return contextData.get(key);
    }
    
    /**
     * Check if context data exists for a key
     * @param key Context data key
     * @return true if key exists, false otherwise
     */
    public boolean hasContextData(String key) {
        return contextData.containsKey(key);
    }
    
    /**
     * Clear all thread-local context (operation, block type, mapping ID)
     * Does not clear shared context data
     */
    public void clearThreadContext() {
        currentOperation.remove();
        currentBlockType.remove();
        currentMappingId.remove();
    }
    
    /**
     * Alias for clearThreadContext() for compatibility
     */
    public void clearContext() {
        clearThreadContext();
    }
    
    /**
     * Clear all context data (shared and thread-local)
     */
    public void clearAllContext() {
        clearThreadContext();
        contextData.clear();
    }
    
    /**
     * Clear only the shared context data
     */
    public void clearSharedContext() {
        contextData.clear();
    }
    
    /**
     * Get a snapshot of current context information for debugging
     * @return Context information string
     */
    public String getContextInfo() {
        return String.format("MappingContext{operation='%s', blockType='%s', mappingId='%s', sharedKeys=%s}",
                           getCurrentOperation(),
                           getCurrentBlockType(),
                           getCurrentMappingId(),
                           contextData.keySet());
    }
    
    /**
     * Create a context snapshot for error reporting
     * @return Map containing current context state
     */
    public Map<String, Object> createSnapshot() {
        Map<String, Object> snapshot = new ConcurrentHashMap<>();
        snapshot.put("operation", getCurrentOperation());
        snapshot.put("blockType", getCurrentBlockType());
        snapshot.put("mappingId", getCurrentMappingId());
        snapshot.put("sharedContextSize", contextData.size());
        snapshot.put("timestamp", System.currentTimeMillis());
        return snapshot;
    }
}