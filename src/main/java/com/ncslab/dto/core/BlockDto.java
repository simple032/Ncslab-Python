package com.ncslab.dto.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.common.PortDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Base DTO class for all block types in the NCSLabLink system.
 * This class maps the core Block entity and serves as the foundation
 * for all specific block DTOs.
 * 
 * Provides type-safe, validated replacement for JSONObject-based block representation.
 * 
 * @author DTO Migration Framework
 * @version 1.0
 * @since DTO Migration Week 5
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@MigrationCompatible(originalClass = "com.ncslab.block.Block")
public abstract class BlockDto {
    
    // ===== CORE BLOCK IDENTIFICATION =====
    
    /**
     * Unique identifier for this block instance.
     * Corresponds to Block.blockId
     */
    private Integer blockId;
    
    /**
     * The type of this block (e.g., "Constant", "Gain", "Sum", "Integrator").
     * Must match the BlockType enumeration values.
     * Corresponds to Block.blockType
     */
    @JsonProperty("blockType")
    private String blockType;
    
    /**
     * Human-readable name for this block instance.
     * Corresponds to Block.blockName
     */
    @JsonProperty("blockName")
    private String blockName;
    
    /**
     * Unique UUID for this block.
     * Used for cross-reference and persistence.
     * Corresponds to Block.blockUUID
     */
    @JsonProperty("blockUUID")
    private String blockUUID;
    
    /**
     * Path indicating this block's location in the model hierarchy.
     * Format: "modelName" or "modelName/subsystem/..."
     * Corresponds to Block.blockPath
     */
    @JsonProperty("blockPath")
    private String blockPath;
    
    /**
     * Source block reference for library blocks.
     * Optional field for blocks derived from libraries.
     */
    @JsonProperty("srcBlock")
    private String srcBlock;
    
    // ===== POSITIONING AND LAYOUT =====
    
    /**
     * Visual position of the block in the UI.
     * Contains x, y coordinates and optional rotation/scaling.
     */
    private PositionDto position;
    
    /**
     * Visual appearance settings (color, icon, etc.).
     */
    private AppearanceDto appearance;
    
    // ===== CONNECTIVITY =====
    
    /**
     * Input port definitions for this block.
     * Defines the data inputs this block accepts.
     */
    private List<PortDto> inputPorts;
    
    /**
     * Output port definitions for this block.
     * Defines the data outputs this block produces.
     */
    private List<PortDto> outputPorts;
    
    // ===== PARAMETERS AND CONFIGURATION =====
    
    /**
     * Block-specific parameters stored as typed parameters.
     * Modern replacement for JSONObject paramValues.
     */
    private Map<String, TypedParameter> parameters;
    
    /**
     * Legacy parameter values for backward compatibility.
     * Used by legacy JSON parsing and some existing blocks.
     * @deprecated Use typed parameters instead
     */
    @JsonProperty("paramValues")
    @Deprecated
    private Map<String, Object> paramValues;
    
    /**
     * Sample time for discrete-time blocks.
     * -1 indicates inherited sample time.
     */
    private Double sampleTime;
    
    /**
     * Execution priority for this block.
     * Lower numbers execute first.
     */
    private Integer priority;
    
    // ===== METADATA =====
    
    /**
     * Documentation and description for this block.
     */
    private String description;
    
    /**
     * Tags for categorization and searching.
     */
    private Set<String> tags;
    
    /**
     * Custom properties for extensibility.
     */
    private Map<String, Object> customProperties;
    
    /**
     * Creation timestamp for auditing.
     */
    private LocalDateTime createdAt;
    
    /**
     * Last modification timestamp.
     */
    private LocalDateTime modifiedAt;
    
    /**
     * Version information for compatibility tracking.
     */
    private String version;
    
    // ===== INITIALIZATION =====
    
    /**
     * Initialize collections to prevent null pointer exceptions.
     */
    protected void initializeCollections() {
        if (inputPorts == null) inputPorts = new ArrayList<>();
        if (outputPorts == null) outputPorts = new ArrayList<>();
        if (parameters == null) parameters = new HashMap<>();
        if (tags == null) tags = new HashSet<>();
        if (customProperties == null) customProperties = new HashMap<>();
    }
    
    // ===== VALIDATION =====
    
    /**
     * Validate this DTO's data integrity and business rules.
     * Subclasses should override and call super.validate() first.
     * 
     * @return ValidationResult containing any errors or warnings
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Validate required fields
        if (blockType == null || blockType.trim().isEmpty()) {
            result.addError("Block type is required");
        }
        
        if (blockName == null || blockName.trim().isEmpty()) {
            result.addError("Block name is required");
        }
        
        // Validate block type format
        if (blockType != null && !isValidBlockType(blockType)) {
            result.addError("Invalid block type: " + blockType);
        }
        
        // Validate UUID format if provided
        if (blockUUID != null && !isValidUUID(blockUUID)) {
            result.addError("Invalid UUID format: " + blockUUID);
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime < -1) {
            result.addError("Sample time must be >= -1 (inherited) or positive");
        }
        
        // Validate priority
        if (priority != null && priority < 0) {
            result.addError("Priority must be non-negative");
        }
        
        // Validate ports
        if (inputPorts != null) {
            for (int i = 0; i < inputPorts.size(); i++) {
                PortDto port = inputPorts.get(i);
                if (port != null) {
                    ValidationResult portResult = port.validate();
                    if (!portResult.isValid()) {
                        result.addError("Input port " + i + " validation failed: " + portResult.getErrors());
                    }
                }
            }
        }
        
        if (outputPorts != null) {
            for (int i = 0; i < outputPorts.size(); i++) {
                PortDto port = outputPorts.get(i);
                if (port != null) {
                    ValidationResult portResult = port.validate();
                    if (!portResult.isValid()) {
                        result.addError("Output port " + i + " validation failed: " + portResult.getErrors());
                    }
                }
            }
        }
        
        // Validate parameters
        if (parameters != null) {
            for (Map.Entry<String, TypedParameter> entry : parameters.entrySet()) {
                String paramName = entry.getKey();
                TypedParameter param = entry.getValue();
                
                if (paramName == null || paramName.trim().isEmpty()) {
                    result.addError("Parameter name cannot be empty");
                }
                
                if (param != null) {
                    ValidationResult paramResult = param.validate();
                    if (!paramResult.isValid()) {
                        result.addError("Parameter " + paramName + " validation failed: " + paramResult.getErrors());
                    }
                }
            }
        }
        
        return result;
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    /**
     * Get a parameter value by name with type safety.
     */
    public <T> T getParameterValue(String name, Class<T> type) {
        TypedParameter param = parameters != null ? parameters.get(name) : null;
        return param != null ? param.getValue(type) : null;
    }
    
    /**
     * Set a parameter value with type safety.
     */
    public void setParameterValue(String name, Object value) {
        if (parameters == null) parameters = new HashMap<>();
        parameters.put(name, TypedParameter.of(value));
    }
    
    /**
     * Check if a parameter exists.
     */
    public boolean hasParameter(String name) {
        return parameters != null && parameters.containsKey(name);
    }
    
    /**
     * Get parameter with default value if not present.
     */
    public <T> T getParameterValue(String name, Class<T> type, T defaultValue) {
        T value = getParameterValue(name, type);
        return value != null ? value : defaultValue;
    }
    
    // ===== PORT ACCESS HELPERS =====
    
    /**
     * Get input port by index.
     */
    public PortDto getInputPort(int index) {
        return (inputPorts != null && index >= 0 && index < inputPorts.size()) 
               ? inputPorts.get(index) : null;
    }
    
    /**
     * Get output port by index.
     */
    public PortDto getOutputPort(int index) {
        return (outputPorts != null && index >= 0 && index < outputPorts.size()) 
               ? outputPorts.get(index) : null;
    }
    
    /**
     * Get number of input ports.
     */
    public int getInputPortCount() {
        return inputPorts != null ? inputPorts.size() : 0;
    }
    
    /**
     * Get number of output ports.
     */
    public int getOutputPortCount() {
        return outputPorts != null ? outputPorts.size() : 0;
    }
    
    // ===== CONVERSION UTILITIES =====
    
    
    // ===== LEGACY JSON CONVERSION METHODS =====
    
    /**
     * Create BlockDto from legacy JSONObject
     * @param jsonObject Legacy JSONObject
     * @return BlockDto or null if conversion fails
     */
    public static BlockDto fromLegacyJson(JSONObject jsonObject) {
        // This should be implemented by concrete subclasses based on blockType
        // For now, we provide basic field extraction
        throw new UnsupportedOperationException(
            "Subclasses must implement fromLegacyJson() method");
    }
    
    /**
     * Convert to legacy JSONObject format
     * @deprecated Use Jackson serialization with JsonUtils.serializeDto() instead
     * @return JSONObject representation
     */
    @Deprecated
    public JSONObject toLegacyJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            
            // Set basic fields
            if (blockType != null) jsonObject.put("blockType", blockType);
            if (blockName != null) jsonObject.put("blockName", blockName);
            if (blockPath != null) jsonObject.put("blockPath", blockPath);
            if (blockUUID != null) jsonObject.put("blockUUID", blockUUID);
            if (srcBlock != null) jsonObject.put("srcBlock", srcBlock);
            
            // Handle paramValues mapping - always include this field, even if empty
            JSONObject paramValuesJson = new JSONObject();
            
            // Add legacy paramValues if present
            if (paramValues != null && !paramValues.isEmpty()) {
                for (Map.Entry<String, Object> entry : paramValues.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        paramValuesJson.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            // Convert typed parameters to legacy format
            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String, TypedParameter> entry : parameters.entrySet()) {
                    Object value = entry.getValue().getValue();
                    if (value != null) {
                        paramValuesJson.put(entry.getKey(), value);
                    }
                }
            }
            
            jsonObject.put("paramValues", paramValuesJson);
            
            return jsonObject;
        } catch (Exception e) {
            System.err.println("Failed to convert BlockDto to JSONObject for block: " + blockName);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // ===== LEGACY PARAMETER ACCESS HELPERS =====
    
    /**
     * Get parameter value from legacy paramValues or typed parameters
     */
    public Object getParam(String key) {
        // Check legacy paramValues first
        if (paramValues != null && paramValues.containsKey(key)) {
            return paramValues.get(key);
        }
        // Check typed parameters
        if (parameters != null && parameters.containsKey(key)) {
            TypedParameter param = parameters.get(key);
            return param != null ? param.getValue() : null;
        }
        return null;
    }
    
    /**
     * Get parameter as string
     */
    public String getParamAsString(String key) {
        Object value = getParam(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Get parameter as double
     */
    public Double getParamAsDouble(String key) {
        Object value = getParam(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get parameter as integer
     */
    public Integer getParamAsInteger(String key) {
        Object value = getParam(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Check if parameter exists
     */
    public boolean hasParam(String key) {
        return (paramValues != null && paramValues.containsKey(key)) ||
               (parameters != null && parameters.containsKey(key));
    }
    
    // ===== SIMPLE VALIDATION METHODS =====
    
    /**
     * Simple validation check for backward compatibility
     * @return true if basic fields are valid
     */
    public boolean isValid() {
        return blockType != null && !blockType.trim().isEmpty() &&
               blockName != null && !blockName.trim().isEmpty();
    }
    
    /**
     * Get simple validation error message for backward compatibility
     * @return error message or null if valid
     */
    public String getValidationError() {
        if (blockType == null || blockType.trim().isEmpty()) {
            return "Block type is required";
        }
        if (blockName == null || blockName.trim().isEmpty()) {
            return "Block name is required";
        }
        return null;
    }

    // ===== UTILITY METHODS =====
    
    /**
     * Create a deep copy of this block DTO.
     */
    public abstract BlockDto copy();
    
    /**
     * Check if this block is compatible with another block for connections.
     */
    public boolean isCompatibleWith(BlockDto other) {
        // Basic compatibility check - subclasses can override
        return other != null && 
               blockType != null && 
               other.blockType != null;
    }
    
    /**
     * Get a summary string for debugging and logging.
     */
    public String getSummary() {
        return String.format("%s[id=%d, name='%s', type='%s', inputs=%d, outputs=%d]",
                getClass().getSimpleName(),
                blockId != null ? blockId : -1,
                blockName != null ? blockName : "unnamed",
                blockType != null ? blockType : "unknown",
                getInputPortCount(),
                getOutputPortCount());
    }
    
    // ===== VALIDATION HELPERS =====
    
    /**
     * Validate block type against known types.
     */
    private boolean isValidBlockType(String type) {
        // Basic validation - could be enhanced with actual BlockType enum
        return type.matches("[A-Za-z][A-Za-z0-9_]*");
    }
    
    /**
     * Validate UUID format.
     */
    private boolean isValidUUID(String uuid) {
        if ("null".equals(uuid)) return true; // Legacy compatibility
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}

/**
 * Position information for block layout.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class PositionDto {
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private Double rotation;
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (x != null && !Double.isFinite(x)) {
            result.addError("Position X must be finite");
        }
        if (y != null && !Double.isFinite(y)) {
            result.addError("Position Y must be finite");
        }
        if (width != null && width <= 0) {
            result.addError("Width must be positive");
        }
        if (height != null && height <= 0) {
            result.addError("Height must be positive");
        }
        
        return result;
    }
}

/**
 * Appearance settings for block visualization.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class AppearanceDto {
    private String backgroundColor;
    private String foregroundColor;
    private String iconPath;
    private Boolean showName;
    private String fontFamily;
    private Integer fontSize;
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (fontSize != null && fontSize <= 0) {
            result.addError("Font size must be positive");
        }
        
        return result;
    }
}