package com.ncslab.dto.block;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.core.BaseDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO for block dimensions with validation support.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockDimensionDto implements BaseDto {
    
    @JsonProperty("width")
    private Double width;
    
    @JsonProperty("height")
    private Double height;
    
    @JsonProperty("depth")
    private Double depth;
    
    @JsonProperty("unit")
    private String unit = "px"; // Default to pixels
    
    private Map<String, Object> metadata = new HashMap<>();
    
    public BlockDimensionDto() {}
    
    public BlockDimensionDto(Double width, Double height) {
        this.width = width;
        this.height = height;
    }
    
    public BlockDimensionDto(Double width, Double height, Double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }
    
    // Convenience methods
    public boolean is2D() {
        return depth == null || depth == 0.0;
    }
    
    public boolean is3D() {
        return depth != null && depth > 0.0;
    }
    
    public double getArea() {
        if (width != null && height != null) {
            return width * height;
        }
        return 0.0;
    }
    
    public double getVolume() {
        if (width != null && height != null) {
            double area = width * height;
            return depth != null ? area * depth : area;
        }
        return 0.0;
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Width and height are required and must be positive
        if (width == null) {
            result.addError("width", "Width is required");
        } else if (width <= 0) {
            result.addError("width", "Width must be positive");
        }
        
        if (height == null) {
            result.addError("height", "Height is required");
        } else if (height <= 0) {
            result.addError("height", "Height must be positive");
        }
        
        // Depth is optional but must be positive if specified
        if (depth != null && depth < 0) {
            result.addError("depth", "Depth must be non-negative");
        }
        
        // Validate unit
        if (unit != null && !isValidUnit(unit)) {
            result.addError("unit", "Invalid unit: " + unit + ". Valid units: px, pt, mm, cm, in");
        }
        
        // Check for reasonable size limits
        if (width != null && width > 10000) {
            result.addError("width", "Width exceeds maximum limit of 10000");
        }
        
        if (height != null && height > 10000) {
            result.addError("height", "Height exceeds maximum limit of 10000");
        }
        
        if (depth != null && depth > 10000) {
            result.addError("depth", "Depth exceeds maximum limit of 10000");
        }
        
        return result;
    }
    
    private boolean isValidUnit(String unit) {
        String[] validUnits = {"px", "pt", "mm", "cm", "in", "em", "rem", "%"};
        for (String validUnit : validUnits) {
            if (validUnit.equalsIgnoreCase(unit)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getDtoType() {
        return "BlockDimension";
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
        if (is3D()) {
            return String.format("BlockDimensionDto{width=%s, height=%s, depth=%s, unit='%s', valid=%s}", 
                               width, height, depth, unit, isValid());
        } else {
            return String.format("BlockDimensionDto{width=%s, height=%s, unit='%s', valid=%s}", 
                               width, height, unit, isValid());
        }
    }
}