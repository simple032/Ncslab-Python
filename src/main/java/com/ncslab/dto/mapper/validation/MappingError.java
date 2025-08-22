package com.ncslab.dto.mapper.validation;

/**
 * Represents a specific error encountered during DTO-Entity mapping.
 * Contains detailed information about the error including field, message, code, severity, and rejected value.
 */
public class MappingError {
    private String field;
    private String message;
    private String code;
    private Severity severity;
    private Object rejectedValue;
    
    public MappingError(String field, String message) {
        this.field = field;
        this.message = message;
        this.code = "MAPPING_ERROR";
        this.severity = Severity.ERROR;
    }
    
    public MappingError(String field, String message, String code, Severity severity, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.code = code;
        this.severity = severity;
        this.rejectedValue = rejectedValue;
    }
    
    public enum Severity {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    // Getters and setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    
    public Object getRejectedValue() { return rejectedValue; }
    public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }
    
    @Override
    public String toString() {
        return String.format("MappingError{field='%s', message='%s', code='%s', severity=%s, rejectedValue=%s}", 
                           field, message, code, severity, rejectedValue);
    }
}