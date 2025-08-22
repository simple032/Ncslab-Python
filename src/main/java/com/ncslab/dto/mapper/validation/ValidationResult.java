package com.ncslab.dto.mapper.validation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Base validation result class that holds validation errors and success status.
 * Used by DTOs and other validation components.
 */
public class ValidationResult {
    private boolean valid = true;
    private List<ValidationError> errors = new ArrayList<>();
    private List<ValidationWarning> warnings = new ArrayList<>();
    
    public ValidationResult() {}
    
    public ValidationResult(boolean valid) {
        this.valid = valid;
    }
    
    public void addError(String field, String message) {
        errors.add(new ValidationError(field, message));
        valid = false;
    }
    
    public void addError(ValidationError error) {
        errors.add(error);
        valid = false;
    }
    
    public void addError(String message) {
        errors.add(new ValidationError("general", message));
        valid = false;
    }
    
    public void addWarning(String field, String message) {
        warnings.add(new ValidationWarning(field, message));
    }
    
    public void addWarning(ValidationWarning warning) {
        warnings.add(warning);
    }
    
    public void addErrors(List<ValidationError> validationErrors) {
        errors.addAll(validationErrors);
        if (!validationErrors.isEmpty()) {
            valid = false;
        }
    }
    
    public void merge(ValidationResult other) {
        if (other != null) {
            this.errors.addAll(other.errors);
            this.warnings.addAll(other.warnings);
            if (!other.valid) {
                this.valid = false;
            }
        }
    }
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    
    public List<ValidationError> getErrors() { return Collections.unmodifiableList(errors); }
    public List<ValidationWarning> getWarnings() { return Collections.unmodifiableList(warnings); }
    
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    public int getErrorCount() { return errors.size(); }
    public int getWarningCount() { return warnings.size(); }
    
    @Override
    public String toString() {
        return String.format("ValidationResult{valid=%s, errorCount=%d}", valid, errors.size());
    }
}