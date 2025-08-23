package com.ncslab.dto.block;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.core.BaseDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO for block parameters with type-safe parameter handling.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockParametersDto implements BaseDto {
    
    @JsonProperty("parameters")
    private Map<String, TypedParameter> parameters = new HashMap<>();
    
    private Map<String, Object> metadata = new HashMap<>();
    
    public BlockParametersDto() {}
    
    public BlockParametersDto(Map<String, TypedParameter> parameters) {
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
    }
    
    // Convenience methods for parameter access
    public void addParameter(String name, Object value, String type) {
        parameters.put(name, new TypedParameter(value, type));
    }
    
    public void addParameter(String name, TypedParameter parameter) {
        if (parameter != null) {
            parameters.put(name, parameter);
        }
    }
    
    public TypedParameter getParameter(String name) {
        return parameters.get(name);
    }
    
    public Object getParameterValue(String name) {
        TypedParameter param = getParameter(name);
        return param != null ? param.getAsString() : null;
    }
    
    public String getParameterType(String name) {
        TypedParameter param = getParameter(name);
        return param != null ? param.getType() : null;
    }
    
    public boolean hasParameter(String name) {
        return parameters.containsKey(name);
    }
    
    public void removeParameter(String name) {
        parameters.remove(name);
    }
    
    public boolean isEmpty() {
        return parameters.isEmpty();
    }
    
    public int size() {
        return parameters.size();
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (parameters != null) {
            for (Map.Entry<String, TypedParameter> entry : parameters.entrySet()) {
                String paramName = entry.getKey();
                TypedParameter parameter = entry.getValue();
                
                if (paramName == null || paramName.trim().isEmpty()) {
                    result.addError("parameters", "Parameter name cannot be null or empty");
                    continue;
                }
                
                if (parameter == null) {
                    result.addError("parameters." + paramName, "Parameter cannot be null");
                    continue;
                }
                
                // Validate parameter name format
                if (!paramName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                    result.addError("parameters." + paramName, "Parameter name must start with a letter and contain only alphanumeric characters and underscores");
                }
                
                // Validate parameter itself
                ValidationResult paramValidation = parameter.validate();
                if (!paramValidation.isValid()) {
                    for (var error : paramValidation.getErrors()) {
                        result.addError("parameters." + paramName + "." + error.getField(), error.getMessage());
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    public String getDtoType() {
        return "BlockParameters";
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
    public String toString() {
        return String.format("BlockParametersDto{paramCount=%d, valid=%s}", 
                           parameters.size(), isValid());
    }
}