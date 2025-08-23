package com.ncslab.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.core.BaseDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Typed parameter with value and type information for type-safe parameter handling.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TypedParameter implements BaseDto {
    
    @JsonProperty("value")
    private Object value;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("constraints")
    private Map<String, Object> constraints;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    public TypedParameter() {}
    
    public TypedParameter(Object value, String type) {
        this.value = value;
        this.type = type;
    }
    
    public TypedParameter(Object value, String type, Map<String, Object> constraints) {
        this.value = value;
        this.type = type;
        this.constraints = constraints;
    }
    
    // Static factory methods
    public static TypedParameter of(Object value) {
        if (value == null) {
            return new TypedParameter(null, "null");
        }
        
        String type = inferType(value);
        return new TypedParameter(value, type);
    }
    
    public static TypedParameter of(Object value, String type) {
        return new TypedParameter(value, type);
    }
    
    public static TypedParameter of(Object value, String type, Map<String, Object> constraints) {
        return new TypedParameter(value, type, constraints);
    }
    
    private static String inferType(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "string";
        if (value instanceof Double || value instanceof Float) return "double";
        if (value instanceof Integer || value instanceof Long) return "int";
        if (value instanceof Boolean) return "boolean";
        if (value.getClass().isArray()) return "array";
        if (value instanceof java.util.Collection) return "list";
        return "object";
    }
    
    // Additional methods for mapper compatibility
    public Object getValue() {
        return value;
    }
    
    public <T> T getValue(Class<T> clazz) {
        if (value == null) return null;
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        
        // Type conversion
        try {
            if (clazz == String.class) {
                return clazz.cast(value.toString());
            } else if (clazz == Double.class && value instanceof Number) {
                return clazz.cast(((Number) value).doubleValue());
            } else if (clazz == Integer.class && value instanceof Number) {
                return clazz.cast(((Number) value).intValue());
            } else if (clazz == Boolean.class) {
                if (value instanceof Boolean) {
                    return clazz.cast(value);
                } else if (value instanceof String) {
                    return clazz.cast(Boolean.parseBoolean((String) value));
                }
            }
        } catch (Exception e) {
            // Conversion failed, return null
        }
        
        return null;
    }
    
    public String getValidationExpression() {
        return (String) getConstraint("validationExpression");
    }
    
    public Object getDefaultValue() {
        return getConstraint("defaultValue");
    }
    
    public boolean isRequired() {
        Object required = getConstraint("required");
        return required instanceof Boolean ? (Boolean) required : false;
    }
    
    public Class<?> getValueType() {
        if (value == null) return Object.class;
        return value.getClass();
    }
    
    // Builder pattern
    public static class Builder {
        private final TypedParameter param = new TypedParameter();
        
        public Builder value(Object value) {
            param.value = value;
            return this;
        }
        
        public Builder type(String type) {
            param.type = type;
            return this;
        }
        
        public Builder constraints(Map<String, Object> constraints) {
            param.constraints = constraints;
            return this;
        }
        
        public Builder constraint(String name, Object value) {
            param.addConstraint(name, value);
            return this;
        }
        
        public TypedParameter build() {
            return param;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Type-safe value accessors
    public String getAsString() {
        return value != null ? value.toString() : null;
    }
    
    public Double getAsDouble() {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getAsInteger() {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Boolean getAsBoolean() {
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    // Constraint management
    public void addConstraint(String name, Object value) {
        if (constraints == null) {
            constraints = new HashMap<>();
        }
        constraints.put(name, value);
    }
    
    public Object getConstraint(String name) {
        return constraints != null ? constraints.get(name) : null;
    }
    
    public boolean hasConstraint(String name) {
        return constraints != null && constraints.containsKey(name);
    }
    
    // Type checking utilities
    public boolean isNumericType() {
        return "double".equals(type) || "float".equals(type) || "int".equals(type) || "integer".equals(type) || "number".equals(type);
    }
    
    public boolean isStringType() {
        return "string".equals(type) || "text".equals(type);
    }
    
    public boolean isBooleanType() {
        return "boolean".equals(type) || "bool".equals(type);
    }
    
    public boolean isArrayType() {
        return "array".equals(type) || "list".equals(type);
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Type is required
        if (type == null || type.trim().isEmpty()) {
            result.addError("type", "Parameter type is required");
            return result;
        }
        
        // Validate type-value consistency
        if (value != null) {
            if (!isValueCompatibleWithType()) {
                result.addError("value", String.format("Value '%s' is not compatible with type '%s'", value, type));
            }
        }
        
        // Validate constraints if present
        if (constraints != null) {
            validateConstraints(result);
        }
        
        return result;
    }
    
    private boolean isValueCompatibleWithType() {
        if (value == null) return true; // Null is compatible with any type
        
        switch (type.toLowerCase()) {
            case "string":
            case "text":
                return true; // Any value can be converted to string
            case "double":
            case "float":
            case "number":
                return value instanceof Number || isNumericString(value.toString());
            case "int":
            case "integer":
                return value instanceof Integer || 
                       (value instanceof Number && ((Number) value).doubleValue() == ((Number) value).intValue()) ||
                       isIntegerString(value.toString());
            case "boolean":
            case "bool":
                return value instanceof Boolean || isBooleanString(value.toString());
            case "array":
            case "list":
                return value.getClass().isArray() || value instanceof java.util.Collection;
            default:
                return true; // Unknown types are assumed valid
        }
    }
    
    private boolean isNumericString(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isIntegerString(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isBooleanString(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str) ||
               "1".equals(str) || "0".equals(str);
    }
    
    private void validateConstraints(ValidationResult result) {
        // Validate common constraints
        if (hasConstraint("min") && isNumericType()) {
            Double min = getConstraintAsDouble("min");
            Double val = getAsDouble();
            if (min != null && val != null && val < min) {
                result.addError("value", String.format("Value %s is less than minimum %s", val, min));
            }
        }
        
        if (hasConstraint("max") && isNumericType()) {
            Double max = getConstraintAsDouble("max");
            Double val = getAsDouble();
            if (max != null && val != null && val > max) {
                result.addError("value", String.format("Value %s is greater than maximum %s", val, max));
            }
        }
        
        if (hasConstraint("minLength") && isStringType()) {
            Integer minLength = getConstraintAsInteger("minLength");
            String val = getAsString();
            if (minLength != null && val != null && val.length() < minLength) {
                result.addError("value", String.format("String length %d is less than minimum %d", val.length(), minLength));
            }
        }
        
        if (hasConstraint("maxLength") && isStringType()) {
            Integer maxLength = getConstraintAsInteger("maxLength");
            String val = getAsString();
            if (maxLength != null && val != null && val.length() > maxLength) {
                result.addError("value", String.format("String length %d is greater than maximum %d", val.length(), maxLength));
            }
        }
    }
    
    private Double getConstraintAsDouble(String name) {
        Object constraint = getConstraint(name);
        if (constraint instanceof Number) {
            return ((Number) constraint).doubleValue();
        }
        return null;
    }
    
    private Integer getConstraintAsInteger(String name) {
        Object constraint = getConstraint(name);
        if (constraint instanceof Number) {
            return ((Number) constraint).intValue();
        }
        return null;
    }
    
    @Override
    public String getDtoType() {
        return "TypedParameter";
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypedParameter that = (TypedParameter) o;
        return Objects.equals(value, that.value) && 
               Objects.equals(type, that.type) && 
               Objects.equals(constraints, that.constraints);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, type, constraints);
    }
    
    /**
     * Create a deep copy of this TypedParameter.
     */
    public TypedParameter copy() {
        TypedParameter copy = new TypedParameter();
        copy.value = this.value; // For primitives and immutable objects
        copy.type = this.type;
        
        // Deep copy constraints map
        if (this.constraints != null) {
            copy.constraints = new HashMap<>(this.constraints);
        }
        
        // Deep copy metadata map
        if (this.metadata != null) {
            copy.metadata = new HashMap<>(this.metadata);
        }
        
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("TypedParameter{value=%s, type='%s', valid=%s}", 
                           value, type, isValid());
    }

    public TypedParameter orElse(TypedParameter defaultValue) {
        return defaultValue;
    }
}