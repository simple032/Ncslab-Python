package com.ncslab.circuit.validation;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of circuit topology validation.
 * Contains validation errors, warnings, and status information.
 */
@Data
public class CircuitValidationResult {

    private boolean valid;
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> warnings;
    private long validationTimeMs;

    /**
     * Create a new validation result
     */
    public CircuitValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Add a validation error
     * @param issue Error to add
     */
    public void addError(ValidationIssue issue) {
        errors.add(issue);
        valid = false;
    }

    /**
     * Add a validation warning
     * @param issue Warning to add
     */
    public void addWarning(ValidationIssue issue) {
        warnings.add(issue);
    }

    /**
     * Add multiple errors
     * @param issues Errors to add
     */
    public void addErrors(List<ValidationIssue> issues) {
        errors.addAll(issues);
        if (!issues.isEmpty()) {
            valid = false;
        }
    }

    /**
     * Add multiple warnings
     * @param issues Warnings to add
     */
    public void addWarnings(List<ValidationIssue> issues) {
        warnings.addAll(issues);
    }

    /**
     * Check if there are any errors
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there are any warnings
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Get total issue count (errors + warnings)
     * @return Total issue count
     */
    public int getIssueCount() {
        return errors.size() + warnings.size();
    }

    /**
     * Get validation summary
     * @return Summary string
     */
    public String getSummary() {
        if (valid && warnings.isEmpty()) {
            return String.format("Circuit validation passed (%.2f ms)", validationTimeMs / 1000.0);
        } else if (valid) {
            return String.format("Circuit validation passed with %d warning(s) (%.2f ms)",
                warnings.size(), validationTimeMs / 1000.0);
        } else {
            return String.format("Circuit validation failed: %d error(s), %d warning(s) (%.2f ms)",
                errors.size(), warnings.size(), validationTimeMs / 1000.0);
        }
    }

    /**
     * Get detailed report
     * @return Detailed validation report
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append(getSummary()).append("\n");

        if (hasErrors()) {
            report.append("\nErrors:\n");
            for (int i = 0; i < errors.size(); i++) {
                report.append(String.format("  [%d] %s\n", i + 1, errors.get(i).toString()));
            }
        }

        if (hasWarnings()) {
            report.append("\nWarnings:\n");
            for (int i = 0; i < warnings.size(); i++) {
                report.append(String.format("  [%d] %s\n", i + 1, warnings.get(i).toString()));
            }
        }

        return report.toString();
    }

    /**
     * Merge another validation result into this one
     * @param other Other result to merge
     */
    public void merge(CircuitValidationResult other) {
        this.errors.addAll(other.getErrors());
        this.warnings.addAll(other.getWarnings());
        if (!other.isValid()) {
            this.valid = false;
        }
    }
}
