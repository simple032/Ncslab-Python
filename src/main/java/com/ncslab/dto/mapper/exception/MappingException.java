package com.ncslab.dto.mapper.exception;

/**
 * Exception thrown when DTO-Entity mapping operations fail.
 * Provides detailed context about the mapping failure including source/target types,
 * field names, and the specific values that caused the failure.
 */
public class MappingException extends Exception {
    
    private final String sourceType;
    private final String targetType;
    private final String fieldName;
    private final Object sourceValue;
    
    public MappingException(String message) {
        super(message);
        this.sourceType = null;
        this.targetType = null;
        this.fieldName = null;
        this.sourceValue = null;
    }
    
    public MappingException(String message, Throwable cause) {
        super(message, cause);
        this.sourceType = null;
        this.targetType = null;
        this.fieldName = null;
        this.sourceValue = null;
    }
    
    public MappingException(String message, String sourceType, String targetType, 
                          String fieldName, Object sourceValue) {
        super(message);
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.fieldName = fieldName;
        this.sourceValue = sourceValue;
    }
    
    public MappingException(String message, String sourceType, String targetType, 
                          String fieldName, Object sourceValue, Throwable cause) {
        super(message, cause);
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.fieldName = fieldName;
        this.sourceValue = sourceValue;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (sourceType != null && targetType != null) {
            sb.append(" [").append(sourceType).append(" -> ").append(targetType).append("]");
        }
        
        if (fieldName != null) {
            sb.append(" Field: ").append(fieldName);
        }
        
        if (sourceValue != null) {
            sb.append(" Value: ").append(sourceValue);
        }
        
        return sb.toString();
    }
    
    // Getters
    public String getSourceType() { return sourceType; }
    public String getTargetType() { return targetType; }
    public String getFieldName() { return fieldName; }
    public Object getSourceValue() { return sourceValue; }
}