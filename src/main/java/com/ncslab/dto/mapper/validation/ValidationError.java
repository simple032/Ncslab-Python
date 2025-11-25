package com.ncslab.dto.mapper.validation;

import lombok.Data;

/**
 * Base validation error class used by validation frameworks.
 * Compatible with existing validation systems and provides field-level error information.
 */
@Data
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

    public boolean contains(String msg){        
        return message.contains(msg);
    }
    
    
    @Override
    public String toString() {
        return String.format("ValidationError{field='%s', message='%s', code='%s', invalidValue=%s}", 
                           field, message, code, invalidValue);
    }
}