package com.ncslab.dto.core;

import com.ncslab.dto.mapper.validation.ValidationResult;
import java.util.Map;

/**
 * Base interface for all Data Transfer Objects (DTOs) in the NCSLabLink system.
 * Provides common functionality for validation, metadata handling, and type identification.
 */
public interface BaseDto {
    
    /**
     * Validate this DTO and return validation result
     * @return ValidationResult containing any validation errors
     */
    ValidationResult validate();
    
    /**
     * Get the type identifier for this DTO
     * @return String identifying the DTO type
     */
    String getDtoType();
    
    /**
     * Get metadata associated with this DTO
     * @return Map of metadata key-value pairs
     */
    Map<String, Object> getMetadata();
    
    /**
     * Set metadata for this DTO
     * @param metadata Map of metadata key-value pairs
     */
    void setMetadata(Map<String, Object> metadata);
    
    /**
     * Add a single metadata entry
     * @param key Metadata key
     * @param value Metadata value
     */
    default void addMetadata(String key, Object value) {
        Map<String, Object> metadata = getMetadata();
        if (metadata != null) {
            metadata.put(key, value);
        }
    }
    
    /**
     * Check if this DTO is valid for basic operations
     * @return true if valid, false otherwise
     */
    default boolean isValid() {
        return validate().isValid();
    }
    
    /**
     * Get a summary string representation of this DTO
     * @return Summary string
     */
    default String getSummary() {
        return String.format("%s{type=%s, valid=%s}", 
                           getClass().getSimpleName(), getDtoType(), isValid());
    }
}