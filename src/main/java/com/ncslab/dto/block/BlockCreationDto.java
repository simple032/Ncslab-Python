package com.ncslab.dto.block;

import com.ncslab.dto.core.BaseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.mapper.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO for block creation operations with comprehensive mapping support.
 * Extends BaseDto to provide validation and metadata capabilities.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockCreationDto implements BaseDto {
    
    @JsonProperty("blockType")
    private String blockType;
    
    @JsonProperty("blockName")
    private String blockName;
    
    @JsonProperty("blockPath")
    private String blockPath;
    
    @JsonProperty("blockUUID")
    private String blockUUID;
    
    @JsonProperty("parameters")
    private BlockParametersDto parameters;
    
    @JsonProperty("inputPorts")
    private List<BlockPortDto> inputPorts;
    
    @JsonProperty("outputPorts")
    private List<BlockPortDto> outputPorts;
    
    @JsonProperty("dimensions")
    private BlockDimensionDto dimensions;
    
    @JsonProperty("position")
    private BlockPositionDto position;
    
    @JsonProperty("configuration")
    private BlockConfigurationDto configuration;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    // Default constructor
    public BlockCreationDto() {}
    
    // Builder pattern implementation
    public static class Builder {
        private final BlockCreationDto dto = new BlockCreationDto();
        
        public Builder blockType(String blockType) {
            dto.blockType = blockType;
            return this;
        }
        
        public Builder blockName(String blockName) {
            dto.blockName = blockName;
            return this;
        }
        
        public Builder blockPath(String blockPath) {
            dto.blockPath = blockPath;
            return this;
        }
        
        public Builder blockUUID(String blockUUID) {
            dto.blockUUID = blockUUID;
            return this;
        }
        
        public Builder parameters(BlockParametersDto parameters) {
            dto.parameters = parameters;
            return this;
        }
        
        public Builder inputPorts(List<BlockPortDto> inputPorts) {
            dto.inputPorts = inputPorts;
            return this;
        }
        
        public Builder outputPorts(List<BlockPortDto> outputPorts) {
            dto.outputPorts = outputPorts;
            return this;
        }
        
        public Builder dimensions(BlockDimensionDto dimensions) {
            dto.dimensions = dimensions;
            return this;
        }
        
        public Builder position(double x, double y) {
            dto.position = new BlockPositionDto(x, y);
            return this;
        }
        
        public Builder position(BlockPositionDto position) {
            dto.position = position;
            return this;
        }
        
        public Builder configuration(BlockConfigurationDto configuration) {
            dto.configuration = configuration;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (dto.metadata == null) {
                dto.metadata = new HashMap<>();
            }
            dto.metadata.put(key, value);
            return this;
        }
        
        public BlockCreationDto build() {
            return dto;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Validate required fields
        if (blockType == null || blockType.trim().isEmpty()) {
            result.addError("blockType", "Block type is required");
        }
        
        if (blockName == null || blockName.trim().isEmpty()) {
            result.addError("blockName", "Block name is required");
        }
        
        // Validate block name format (no special characters that could cause issues)
        if (blockName != null && !blockName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            result.addError("blockName", "Block name must start with a letter and contain only alphanumeric characters and underscores");
        }
        
        // Validate nested DTOs
        if (parameters != null) {
            ValidationResult paramValidation = parameters.validate();
            if (!paramValidation.isValid()) {
                for (ValidationError error : paramValidation.getErrors()) {
                    result.addError("parameters." + error.getField(), error.getMessage());
                }
            }
        }
        
        if (dimensions != null) {
            ValidationResult dimensionValidation = dimensions.validate();
            if (!dimensionValidation.isValid()) {
                for (ValidationError error : dimensionValidation.getErrors()) {
                    result.addError("dimensions." + error.getField(), error.getMessage());
                }
            }
        }
        
        if (position != null) {
            ValidationResult positionValidation = position.validate();
            if (!positionValidation.isValid()) {
                for (ValidationError error : positionValidation.getErrors()) {
                    result.addError("position." + error.getField(), error.getMessage());
                }
            }
        }
        
        if (configuration != null) {
            ValidationResult configValidation = configuration.validate();
            if (!configValidation.isValid()) {
                for (ValidationError error : configValidation.getErrors()) {
                    result.addError("configuration." + error.getField(), error.getMessage());
                }
            }
        }
        
        // Validate port lists
        if (inputPorts != null) {
            for (int i = 0; i < inputPorts.size(); i++) {
                ValidationResult portValidation = inputPorts.get(i).validate();
                if (!portValidation.isValid()) {
                    for (ValidationError error : portValidation.getErrors()) {
                        result.addError("inputPorts[" + i + "]." + error.getField(), error.getMessage());
                    }
                }
            }
        }
        
        if (outputPorts != null) {
            for (int i = 0; i < outputPorts.size(); i++) {
                ValidationResult portValidation = outputPorts.get(i).validate();
                if (!portValidation.isValid()) {
                    for (ValidationError error : portValidation.getErrors()) {
                        result.addError("outputPorts[" + i + "]." + error.getField(), error.getMessage());
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    public String getDtoType() {
        return "BlockCreation";
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
        return String.format("BlockCreationDto{type='%s', name='%s', path='%s', uuid='%s', valid=%s}", 
                           blockType, blockName, blockPath, blockUUID, isValid());
    }
}