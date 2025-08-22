package com.ncslab.dto.mapper.validation;

/**
 * Represents a warning encountered during DTO-Entity mapping.
 * Warnings indicate potential issues but do not prevent the mapping from completing.
 */
public class MappingWarning {
    private String field;
    private String message;
    private String code;
    private Object actualValue;
    
    public MappingWarning(String field, String message) {
        this.field = field;
        this.message = message;
        this.code = "MAPPING_WARNING";
    }
    
    public MappingWarning(String field, String message, String code, Object actualValue) {
        this.field = field;
        this.message = message;
        this.code = code;
        this.actualValue = actualValue;
    }
    
    // Getters and setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Object getActualValue() { return actualValue; }
    public void setActualValue(Object actualValue) { this.actualValue = actualValue; }
    
    @Override
    public String toString() {
        return String.format("MappingWarning{field='%s', message='%s', code='%s', actualValue=%s}", 
                           field, message, code, actualValue);
    }
}