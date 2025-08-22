package com.ncslab.dto.block;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.core.BaseDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * DTO for block ports (input/output) with validation and type information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockPortDto implements BaseDto {
    
    @JsonProperty("portName")
    private String portName;
    
    @JsonProperty("portIndex")
    private Integer portIndex;
    
    @JsonProperty("portType")
    private String portType; // "input" or "output"
    
    @JsonProperty("dataType")
    private String dataType;
    
    @JsonProperty("dimensions")
    private List<Integer> dimensions;
    
    @JsonProperty("required")
    private Boolean required = true;
    
    @JsonProperty("description")
    private String description;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    public BlockPortDto() {}
    
    public BlockPortDto(String portName, Integer portIndex, String portType, String dataType) {
        this.portName = portName;
        this.portIndex = portIndex;
        this.portType = portType;
        this.dataType = dataType;
    }
    
    // Builder pattern
    public static class Builder {
        private final BlockPortDto dto = new BlockPortDto();
        
        public Builder portName(String portName) {
            dto.portName = portName;
            return this;
        }
        
        public Builder portIndex(Integer portIndex) {
            dto.portIndex = portIndex;
            return this;
        }
        
        public Builder portType(String portType) {
            dto.portType = portType;
            return this;
        }
        
        public Builder dataType(String dataType) {
            dto.dataType = dataType;
            return this;
        }
        
        public Builder dimensions(List<Integer> dimensions) {
            dto.dimensions = dimensions;
            return this;
        }
        
        public Builder required(Boolean required) {
            dto.required = required;
            return this;
        }
        
        public Builder description(String description) {
            dto.description = description;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            return this;
        }
        
        public BlockPortDto build() {
            return dto;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Convenience methods
    public boolean isInput() {
        return "input".equalsIgnoreCase(portType);
    }
    
    public boolean isOutput() {
        return "output".equalsIgnoreCase(portType);
    }
    
    public boolean isRequired() {
        return required != null ? required : true;
    }
    
    public boolean isScalar() {
        return dimensions == null || dimensions.isEmpty() || 
               (dimensions.size() == 1 && dimensions.get(0) == 1);
    }
    
    public boolean isVector() {
        return dimensions != null && dimensions.size() == 1 && dimensions.get(0) > 1;
    }
    
    public boolean isMatrix() {
        return dimensions != null && dimensions.size() == 2;
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Port name is required
        if (portName == null || portName.trim().isEmpty()) {
            result.addError("portName", "Port name is required");
        } else if (!portName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            result.addError("portName", "Port name must start with a letter and contain only alphanumeric characters and underscores");
        }
        
        // Port index must be non-negative
        if (portIndex != null && portIndex < 0) {
            result.addError("portIndex", "Port index must be non-negative");
        }
        
        // Port type must be valid
        if (portType == null || portType.trim().isEmpty()) {
            result.addError("portType", "Port type is required");
        } else if (!"input".equalsIgnoreCase(portType) && !"output".equalsIgnoreCase(portType)) {
            result.addError("portType", "Port type must be 'input' or 'output'");
        }
        
        // Data type is required
        if (dataType == null || dataType.trim().isEmpty()) {
            result.addError("dataType", "Data type is required");
        } else {
            // Validate data type format
            if (!isValidDataType(dataType)) {
                result.addError("dataType", "Invalid data type: " + dataType);
            }
        }
        
        // Validate dimensions
        if (dimensions != null) {
            for (int i = 0; i < dimensions.size(); i++) {
                Integer dim = dimensions.get(i);
                if (dim == null || dim <= 0) {
                    result.addError("dimensions[" + i + "]", "Dimension must be positive");
                }
            }
            
            // Check for reasonable dimension limits
            if (dimensions.size() > 3) {
                result.addError("dimensions", "Maximum 3 dimensions supported");
            }
        }
        
        return result;
    }
    
    private boolean isValidDataType(String dataType) {
        // Common data types in simulation systems
        String[] validTypes = {
            "double", "float", "int", "integer", "boolean", "bool",
            "complex", "string", "signal", "bus", "inherit"
        };
        
        for (String validType : validTypes) {
            if (validType.equalsIgnoreCase(dataType)) {
                return true;
            }
        }
        
        // Allow custom data types (must start with letter)
        return dataType.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }
    
    @Override
    public String getDtoType() {
        return "BlockPort";
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
        return String.format("BlockPortDto{name='%s', index=%d, type='%s', dataType='%s', required=%s, valid=%s}", 
                           portName, portIndex, portType, dataType, required, isValid());
    }
}