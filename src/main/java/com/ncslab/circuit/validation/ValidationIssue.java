package com.ncslab.circuit.validation;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * Represents a single validation issue (error or warning).
 */
@Data
@AllArgsConstructor
public class ValidationIssue {

    /**
     * Issue severity level
     */
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Issue category
     */
    public enum Category {
        SHORT_CIRCUIT("Short Circuit"),
        OPEN_CIRCUIT("Open Circuit"),
        INVALID_CONFIGURATION("Invalid Configuration"),
        MISSING_GROUND("Missing Ground"),
        FLOATING_NODE("Floating Node"),
        INVALID_CONNECTION("Invalid Connection"),
        PARAMETER_ERROR("Parameter Error"),
        TOPOLOGY_ERROR("Topology Error"),
        COMPATIBILITY_ERROR("Compatibility Error");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Severity severity;
    private Category category;
    private String message;
    private String location; // Block name or connection description
    private String suggestion; // Optional fix suggestion

    /**
     * Create an error issue
     * @param category Issue category
     * @param message Error message
     * @param location Location in circuit
     * @return ValidationIssue
     */
    public static ValidationIssue error(Category category, String message, String location) {
        return new ValidationIssue(Severity.ERROR, category, message, location, null);
    }

    /**
     * Create an error issue with suggestion
     * @param category Issue category
     * @param message Error message
     * @param location Location in circuit
     * @param suggestion Fix suggestion
     * @return ValidationIssue
     */
    public static ValidationIssue error(Category category, String message, String location, String suggestion) {
        return new ValidationIssue(Severity.ERROR, category, message, location, suggestion);
    }

    /**
     * Create a warning issue
     * @param category Issue category
     * @param message Warning message
     * @param location Location in circuit
     * @return ValidationIssue
     */
    public static ValidationIssue warning(Category category, String message, String location) {
        return new ValidationIssue(Severity.WARNING, category, message, location, null);
    }

    /**
     * Create a warning issue with suggestion
     * @param category Issue category
     * @param message Warning message
     * @param location Location in circuit
     * @param suggestion Fix suggestion
     * @return ValidationIssue
     */
    public static ValidationIssue warning(Category category, String message, String location, String suggestion) {
        return new ValidationIssue(Severity.WARNING, category, message, location, suggestion);
    }

    /**
     * Create an info issue
     * @param category Issue category
     * @param message Info message
     * @param location Location in circuit
     * @return ValidationIssue
     */
    public static ValidationIssue info(Category category, String message, String location) {
        return new ValidationIssue(Severity.INFO, category, message, location, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ");
        sb.append(category.getDisplayName()).append(": ");
        sb.append(message);
        if (location != null && !location.isEmpty()) {
            sb.append(" (at ").append(location).append(")");
        }
        if (suggestion != null && !suggestion.isEmpty()) {
            sb.append(" - Suggestion: ").append(suggestion);
        }
        return sb.toString();
    }
}
