package com.ncslab.dto.mapper.validation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * Specialized validation result for mapping operations.
 * Extends basic validation with mapping-specific errors, warnings, and metadata.
 */
public class MappingValidationResult {
    
    private boolean valid = true;
    private List<MappingError> errors = new ArrayList<>();
    private List<MappingWarning> warnings = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();
    
    public void addError(String field, String message) {
        errors.add(new MappingError(field, message));
        valid = false;
    }
    
    public void addError(MappingError error) {
        errors.add(error);
        valid = false;
    }
    
    public void addErrors(List<ValidationError> validationErrors) {
        for (ValidationError error : validationErrors) {
            addError(error.getField(), error.getMessage());
        }
    }
    
    public void addWarning(String field, String message) {
        warnings.add(new MappingWarning(field, message));
    }
    
    public void addWarning(MappingWarning warning) {
        warnings.add(warning);
    }
    
    public void merge(MappingValidationResult other) {
        if (other != null) {
            this.errors.addAll(other.errors);
            this.warnings.addAll(other.warnings);
            this.metadata.putAll(other.metadata);
            if (!other.valid) {
                this.valid = false;
            }
        }
    }
    
    public boolean isValid() { return valid; }
    public List<MappingError> getErrors() { return Collections.unmodifiableList(errors); }
    public List<MappingWarning> getWarnings() { return Collections.unmodifiableList(warnings); }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public Map<String, Object> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
    
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    
    public int getErrorCount() { return errors.size(); }
    public int getWarningCount() { return warnings.size(); }
    
    @Override
    public String toString() {
        return String.format("MappingValidationResult{valid=%s, errorCount=%d, warningCount=%d}", 
                           valid, errors.size(), warnings.size());
    }
}