package com.ncslab.dto.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * Type-safe parameter map for storing typed parameters with convenient access methods.
 * Extends HashMap to provide additional type-safe getter methods.
 */
public class TypedParameterMap extends HashMap<String, TypedParameter> {
    
    private static final long serialVersionUID = 1L;
    
    public TypedParameterMap() {
        super();
    }
    
    public TypedParameterMap(int initialCapacity) {
        super(initialCapacity);
    }
    
    public TypedParameterMap(Map<String, TypedParameter> map) {
        super(map);
    }
    
    /**
     * Add a parameter with value and type
     * @param name Parameter name
     * @param value Parameter value
     * @param type Parameter type
     * @return This map for method chaining
     */
    public TypedParameterMap addParameter(String name, Object value, String type) {
        put(name, new TypedParameter(value, type));
        return this;
    }
    
    /**
     * Add a typed parameter
     * @param name Parameter name
     * @param parameter Typed parameter
     * @return This map for method chaining
     */
    public TypedParameterMap addParameter(String name, TypedParameter parameter) {
        if (parameter != null) {
            put(name, parameter);
        }
        return this;
    }
    
    /**
     * Get parameter value with type safety
     * @param name Parameter name
     * @return Parameter value or null if not found
     */
    public Object getParameterValue(String name) {
        TypedParameter param = get(name);
        return param != null ? param.getValue() : null;
    }
    
    /**
     * Get parameter type
     * @param name Parameter name
     * @return Parameter type or null if not found
     */
    public String getParameterType(String name) {
        TypedParameter param = get(name);
        return param != null ? param.getType() : null;
    }


    public <T> TypedParameter getTypedParameter(String name, Class<T> type) {
        TypedParameter param = get(name);
        return param != null && param.getType().equals(type.getSimpleName()) ? param : null;
    }

    public <T> TypedParameter getTypedParameter(String name, Class<T> type, Object defaultValue) {
        TypedParameter param = get(name);
        return param != null && param.getType().equals(type.getSimpleName()) ? param : TypedParameter.of(defaultValue);
    }
    
    /**
     * Get parameter value as string
     * @param name Parameter name
     * @return String value or null if not found
     */
    public String getAsString(String name) {
        TypedParameter param = get(name);
        return param != null ? param.getAsString() : null;
    }
    
    /**
     * Get parameter value as double
     * @param name Parameter name
     * @return Double value or null if not found or not convertible
     */
    public Double getAsDouble(String name) {
        TypedParameter param = get(name);
        return param != null ? param.getAsDouble() : null;
    }
    
    /**
     * Get parameter value as integer
     * @param name Parameter name
     * @return Integer value or null if not found or not convertible
     */
    public Integer getAsInteger(String name) {
        TypedParameter param = get(name);
        return param != null ? param.getAsInteger() : null;
    }
    
    /**
     * Get parameter value as boolean
     * @param name Parameter name
     * @return Boolean value or null if not found or not convertible
     */
    public Boolean getAsBoolean(String name) {
        TypedParameter param = get(name);
        return param != null ? param.getAsBoolean() : null;
    }
    
    /**
     * Get parameter value with default
     * @param name Parameter name
     * @param defaultValue Default value if parameter not found
     * @return Parameter value or default value
     */
    public Object getParameterValue(String name, Object defaultValue) {
        Object value = getParameterValue(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get string parameter with default
     * @param name Parameter name
     * @param defaultValue Default value
     * @return String value or default
     */
    public String getAsString(String name, String defaultValue) {
        String value = getAsString(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get double parameter with default
     * @param name Parameter name
     * @param defaultValue Default value
     * @return Double value or default
     */
    public Double getAsDouble(String name, Double defaultValue) {
        Double value = getAsDouble(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get integer parameter with default
     * @param name Parameter name
     * @param defaultValue Default value
     * @return Integer value or default
     */
    public Integer getAsInteger(String name, Integer defaultValue) {
        Integer value = getAsInteger(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get boolean parameter with default
     * @param name Parameter name
     * @param defaultValue Default value
     * @return Boolean value or default
     */
    public Boolean getAsBoolean(String name, Boolean defaultValue) {
        Boolean value = getAsBoolean(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Check if parameter exists and has a non-null value
     * @param name Parameter name
     * @return true if parameter exists with non-null value
     */
    public boolean hasParameterValue(String name) {
        TypedParameter param = get(name);
        return param != null && param.getValue() != null;
    }
    
    /**
     * Check if parameter exists with a specific type
     * @param name Parameter name
     * @param type Expected type
     * @return true if parameter exists with specified type
     */
    public boolean hasParameterOfType(String name, String type) {
        TypedParameter param = get(name);
        return param != null && type.equals(param.getType());
    }
    
    /**
     * Get all parameter names
     * @return Set of parameter names
     */
    public Set<String> getParameterNames() {
        return keySet();
    }
    
    /**
     * Get all typed parameters
     * @return Collection of typed parameters
     */
    public Collection<TypedParameter> getParameters() {
        return values();
    }
    
    /**
     * Validate all parameters
     * @return true if all parameters are valid, false otherwise
     */
    public boolean isValid() {
        for (TypedParameter param : values()) {
            if (param == null || !param.isValid()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Create a copy of this parameter map
     * @return New TypedParameterMap with copied parameters
     */
    public TypedParameterMap copy() {
        TypedParameterMap copy = new TypedParameterMap();
        for (Map.Entry<String, TypedParameter> entry : entrySet()) {
            // Create new TypedParameter instances to avoid sharing mutable objects
            TypedParameter original = entry.getValue();
            TypedParameter copied = new TypedParameter(original.getValue(), original.getType(), original.getConstraints());
            copy.put(entry.getKey(), copied);
        }
        return copy;
    }
    
    // ===== BUILDER PATTERN =====
    
    /**
     * Create a new builder for TypedParameterMap.
     * 
     * @return TypedParameterMapBuilder instance
     */
    public static TypedParameterMapBuilder builder() {
        return new TypedParameterMapBuilder();
    }
    
    /**
     * Builder class for TypedParameterMap.
     */
    public static class TypedParameterMapBuilder {
        private final TypedParameterMap map = new TypedParameterMap();
        
        /**
         * Add a typed parameter to the map.
         * 
         * @param key Parameter name
         * @param parameter TypedParameter value
         * @return This builder instance
         */
        public TypedParameterMapBuilder put(String key, TypedParameter parameter) {
            map.put(key, parameter);
            return this;
        }
        
        /**
         * Add a parameter with automatic type inference.
         * 
         * @param key Parameter name
         * @param value Parameter value
         * @return This builder instance
         */
        public TypedParameterMapBuilder put(String key, Object value) {
            map.put(key, TypedParameter.of(value));
            return this;
        }
        
        /**
         * Add a parameter with specified type.
         * 
         * @param key Parameter name
         * @param value Parameter value
         * @param type Parameter type
         * @return This builder instance
         */
        public TypedParameterMapBuilder put(String key, Object value, String type) {
            map.put(key, TypedParameter.of(value, type));
            return this;
        }
        
        /**
         * Build the TypedParameterMap.
         * 
         * @return Configured TypedParameterMap instance
         */
        public TypedParameterMap build() {
            return map;
        }
    }
    
    @Override
    public String toString() {
        return String.format("TypedParameterMap{size=%d, valid=%s}", size(), isValid());
    }
}