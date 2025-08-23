package com.ncslab.entity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base interface for all entities in the NCSLabLink system.
 * Provides common functionality for identity, timestamps, and metadata handling.
 */
public interface Entity {
    
    /**
     * Get the unique identifier for this entity
     * @return Unique identifier (typically UUID or database ID)
     */
    String getId();
    
    /**
     * Set the unique identifier for this entity
     * @param id Unique identifier
     */
    void setId(String id);
    
    /**
     * Get the entity type identifier
     * @return String identifying the entity type
     */
    String getEntityType();
    
    /**
     * Get the creation timestamp
     * @return LocalDateTime when this entity was created
     */
    LocalDateTime getCreatedAt();
    
    /**
     * Set the creation timestamp
     * @param createdAt Creation timestamp
     */
    void setCreatedAt(LocalDateTime createdAt);
    
    /**
     * Get the last modification timestamp
     * @return LocalDateTime when this entity was last modified
     */
    LocalDateTime getUpdatedAt();
    
    /**
     * Set the last modification timestamp
     * @param updatedAt Last modification timestamp
     */
    void setUpdatedAt(LocalDateTime updatedAt);
    
    /**
     * Get metadata associated with this entity
     * @return Map of metadata key-value pairs
     */
    Map<String, Object> getMetadata();
    
    /**
     * Set metadata for this entity
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
     * Check if this entity has been persisted (has an ID)
     * @return true if persisted, false otherwise
     */
    default boolean isPersisted() {
        String id = getId();
        return id != null && !id.trim().isEmpty();
    }
    
    /**
     * Update the modification timestamp to now
     */
    default void touch() {
        setUpdatedAt(LocalDateTime.now());
    }
    
    /**
     * Get a summary string representation of this entity
     * @return Summary string
     */
    default String getSummary() {
        return String.format("%s{id=%s, type=%s, persisted=%s}", 
                           getClass().getSimpleName(), getId(), getEntityType(), isPersisted());
    }
}