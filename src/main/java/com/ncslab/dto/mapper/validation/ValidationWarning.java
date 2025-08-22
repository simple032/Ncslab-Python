package com.ncslab.dto.mapper.validation;

/**
 * Represents a validation warning with field and message information.
 */
public class ValidationWarning {
    private final String field;
    private final String message;
    
    public ValidationWarning(String field, String message) {
        this.field = field;
        this.message = message;
    }
    
    public String getField() { return field; }
    public String getMessage() { return message; }
    
    @Override
    public String toString() {
        return String.format("ValidationWarning{field='%s', message='%s'}", field, message);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationWarning)) return false;
        ValidationWarning that = (ValidationWarning) o;
        return java.util.Objects.equals(field, that.field) && 
               java.util.Objects.equals(message, that.message);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(field, message);
    }
}