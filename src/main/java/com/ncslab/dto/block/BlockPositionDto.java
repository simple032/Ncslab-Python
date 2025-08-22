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
 * DTO for block position with validation support.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockPositionDto implements BaseDto {
    
    @JsonProperty("x")
    private Double x;
    
    @JsonProperty("y")
    private Double y;
    
    @JsonProperty("z")
    private Double z;
    
    @JsonProperty("rotation")
    private Double rotation; // In degrees
    
    @JsonProperty("scale")
    private Double scale = 1.0; // Default scale
    
    private Map<String, Object> metadata = new HashMap<>();
    
    public BlockPositionDto() {}
    
    public BlockPositionDto(Double x, Double y) {
        this.x = x;
        this.y = y;
    }
    
    public BlockPositionDto(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Convenience methods
    public boolean is2D() {
        return z == null || z == 0.0;
    }
    
    public boolean is3D() {
        return z != null && z != 0.0;
    }
    
    public boolean isRotated() {
        return rotation != null && rotation != 0.0;
    }
    
    public boolean isScaled() {
        return scale != null && scale != 1.0;
    }
    
    public double getDistanceFrom(BlockPositionDto other) {
        if (other == null) return Double.MAX_VALUE;
        
        double dx = (x != null ? x : 0.0) - (other.x != null ? other.x : 0.0);
        double dy = (y != null ? y : 0.0) - (other.y != null ? other.y : 0.0);
        double dz = (z != null ? z : 0.0) - (other.z != null ? other.z : 0.0);
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // X and Y coordinates are required
        if (x == null) {
            result.addError("x", "X coordinate is required");
        }
        
        if (y == null) {
            result.addError("y", "Y coordinate is required");
        }
        
        // Check for reasonable coordinate limits
        if (x != null && (x < -100000 || x > 100000)) {
            result.addError("x", "X coordinate must be between -100000 and 100000");
        }
        
        if (y != null && (y < -100000 || y > 100000)) {
            result.addError("y", "Y coordinate must be between -100000 and 100000");
        }
        
        if (z != null && (z < -100000 || z > 100000)) {
            result.addError("z", "Z coordinate must be between -100000 and 100000");
        }
        
        // Validate rotation (should be between -360 and 360 degrees)
        if (rotation != null && (rotation < -360 || rotation > 360)) {
            result.addError("rotation", "Rotation must be between -360 and 360 degrees");
        }
        
        // Validate scale (should be positive)
        if (scale != null && scale <= 0) {
            result.addError("scale", "Scale must be positive");
        } else if (scale != null && (scale < 0.01 || scale > 100)) {
            result.addError("scale", "Scale must be between 0.01 and 100");
        }
        
        return result;
    }
    
    @Override
    public String getDtoType() {
        return "BlockPosition";
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
        StringBuilder sb = new StringBuilder("BlockPositionDto{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        if (z != null && z != 0.0) {
            sb.append(", z=").append(z);
        }
        if (rotation != null && rotation != 0.0) {
            sb.append(", rotation=").append(rotation);
        }
        if (scale != null && scale != 1.0) {
            sb.append(", scale=").append(scale);
        }
        sb.append(", valid=").append(isValid());
        sb.append("}");
        return sb.toString();
    }
}