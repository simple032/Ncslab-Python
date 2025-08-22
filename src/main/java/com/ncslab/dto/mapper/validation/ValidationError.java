package com.ncslab.dto.mapper.validation;

/**
 * Base validation error class used by validation frameworks.
 * Compatible with existing validation systems and provides field-level error information.
 */
public class ValidationError {
    private String field;
    private String message;
    private String code;
    private Object invalidValue;
    
    public ValidationError() {}
    
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
        this.code = "VALIDATION_ERROR";
    }
    
    public ValidationError(String field, String message, String code, Object invalidValue) {
        this.field = field;
        this.message = message;
        this.code = code;
        this.invalidValue = invalidValue;
    }
    
    // Getters and setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Object getInvalidValue() { return invalidValue; }
    public void setInvalidValue(Object invalidValue) { this.invalidValue = invalidValue; }
    
    @Override
    public String toString() {
        return String.format("ValidationError{field='%s', message='%s', code='%s', invalidValue=%s}", 
                           field, message, code, invalidValue);
    }
}