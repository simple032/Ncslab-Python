package com.ncslab.dto.mapper;

import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.mapper.validation.MappingValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncslab.dto.core.BaseDto;

/**
 * Service for handling validation operations in the mapping framework.
 * Provides validation logic for DTOs, entities, and mapping operations.
 */
public class ValidationService {
    
    private final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    
    public ValidationService() {
        // Initialize validation service
    }
    
    /**
     * Validate a DTO object
     * @param dto The DTO to validate
     * @return Validation result
     */
    public ValidationResult validateDto(Object dto) {
        ValidationResult result = new ValidationResult();
        
        if (dto == null) {
            result.addError("dto", "DTO cannot be null");
            return result;
        }
        
        // Perform basic validation
        try {
            // If the DTO implements validation, use it
            if (dto instanceof BaseDto) {
                BaseDto baseDto = (BaseDto) dto;
                return baseDto.validate();
            }
        } catch (Exception e) {
            result.addError("validation", "Validation failed: " + e.getMessage());
            logger.error("DTO validation failed", e);
        }
        
        return result;
    }
    
    /**
     * Validate mapping constraints
     * @param source Source object
     * @param target Target object
     * @return Mapping validation result
     */
    public MappingValidationResult validateMappingConstraints(Object source, Object target) {
        MappingValidationResult result = new MappingValidationResult();
        
        if (source == null) {
            result.addError("source", "Source object cannot be null");
        }
        
        if (target == null) {
            result.addError("target", "Target object cannot be null");
        }
        
        return result;
    }
    
    /**
     * Validate that required fields are present
     * @param object Object to validate
     * @param requiredFields Array of required field names
     * @return Validation result
     */
    public ValidationResult validateRequiredFields(Object object, String... requiredFields) {
        ValidationResult result = new ValidationResult();
        
        if (object == null) {
            result.addError("object", "Object cannot be null");
            return result;
        }
        
        Class<?> clazz = object.getClass();
        
        for (String fieldName : requiredFields) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(object);
                
                if (value == null) {
                    result.addError(fieldName, "Required field is null: " + fieldName);
                } else if (value instanceof String && ((String) value).trim().isEmpty()) {
                    result.addError(fieldName, "Required string field is empty: " + fieldName);
                }
            } catch (NoSuchFieldException e) {
                result.addWarning(fieldName, "Field not found: " + fieldName);
            } catch (IllegalAccessException e) {
                result.addError(fieldName, "Cannot access field: " + fieldName);
            }
        }
        
        return result;
    }
}