package com.ncslab.dto.common;

import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO representation of block input/output ports.
 * Maps the InputPort and OutputPort entities from the block system.
 * 
 * @author DTO Migration Framework
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@MigrationCompatible(originalClass = "com.ncslab.block.io.Port")
public class PortDto {
    
    // ===== CORE PORT IDENTIFICATION =====
    
    /**
     * Port index within the block (0-based).
     */
    private Integer portIndex;
    
    /**
     * Port name for identification.
     */
    private String portName;
    
    /**
     * Port type: "input" or "output".
     */
    private PortType portType;
    
    // ===== DATA TYPE AND DIMENSIONS =====
    
    /**
     * Data type of signals passing through this port.
     */
    private DataTypeDto dataType;
    
    /**
     * Signal dimensions [rows, cols] for matrix signals.
     * [1, 1] for scalar, [n, 1] for column vector, [1, n] for row vector.
     */
    private List<Integer> dimensions;
    
    /**
     * Sample time for this port (-1 for inherited).
     */
    private Double sampleTime;
    
    /**
     * Whether this port supports complex numbers.
     */
    private Boolean supportsComplex;
    
    // ===== CONNECTION INFORMATION =====
    
    /**
     * Whether this port is currently connected.
     */
    private Boolean isConnected;
    
    /**
     * Connection reference for connected ports.
     */
    private ConnectionDto connection;
    
    // ===== VALIDATION AND CONSTRAINTS =====
    
    /**
     * Whether this port is required (must be connected).
     */
    private Boolean isRequired;
    
    /**
     * Minimum value constraint for numeric signals.
     */
    private Double minValue;
    
    /**
     * Maximum value constraint for numeric signals.
     */
    private Double maxValue;
    
    /**
     * Default value when not connected (for input ports).
     */
    private Object defaultValue;
    
    // ===== VISUAL PROPERTIES =====
    
    /**
     * Visual position relative to block.
     */
    private PortPositionDto position;
    
    /**
     * Port label for display.
     */
    private String displayLabel;
    
    /**
     * Whether to show the port label.
     */
    private Boolean showLabel;
    
    // ===== METADATA =====
    
    /**
     * Description of this port's purpose.
     */
    private String description;
    
    /**
     * Physical units for this signal (e.g., "m/s", "V", "A").
     */
    private String units;
    
    /**
     * Custom properties for extensibility.
     */
    private java.util.Map<String, Object> customProperties;
    
    // ===== ENUMS =====
    
    public enum PortType {
        INPUT, OUTPUT
    }
    
    // ===== INITIALIZATION =====
    
    /**
     * Initialize collections and default values.
     */
    public void initializeDefaults() {
        if (dimensions == null) {
            dimensions = new ArrayList<>();
            dimensions.add(1); // Default to scalar [1, 1]
            dimensions.add(1);
        }
        if (customProperties == null) {
            customProperties = new java.util.HashMap<>();
        }
        if (isRequired == null) {
            isRequired = false;
        }
        if (supportsComplex == null) {
            supportsComplex = false;
        }
        if (showLabel == null) {
            showLabel = true;
        }
    }
    
    // ===== VALIDATION =====
    
    /**
     * Validate this port's configuration.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Validate required fields
        if (portIndex == null || portIndex < 0) {
            result.addError("Port index must be non-negative");
        }
        
        if (portType == null) {
            result.addError("Port type is required");
        }
        
        // Validate dimensions
        if (dimensions != null) {
            if (dimensions.size() != 2) {
                result.addError("Dimensions must be [rows, cols]");
            } else {
                Integer rows = dimensions.get(0);
                Integer cols = dimensions.get(1);
                if (rows == null || rows <= 0) {
                    result.addError("Dimension rows must be positive");
                }
                if (cols == null || cols <= 0) {
                    result.addError("Dimension cols must be positive");
                }
            }
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime < -1) {
            result.addError("Sample time must be >= -1 (inherited) or positive");
        }
        
        // Validate value constraints
        if (minValue != null && maxValue != null && minValue > maxValue) {
            result.addError("Minimum value cannot be greater than maximum value");
        }
        
        // Validate data type
        if (dataType != null) {
            ValidationResult dataTypeResult = dataType.validate();
            if (!dataTypeResult.isValid()) {
                result.addError("Data type validation failed: " + dataTypeResult.getErrors());
            }
        }
        
        // Validate connection
        if (connection != null) {
            ValidationResult connectionResult = connection.validate();
            if (!connectionResult.isValid()) {
                result.addError("Connection validation failed: " + connectionResult.getErrors());
            }
        }
        
        // Validate position
        if (position != null) {
            ValidationResult positionResult = position.validate();
            if (!positionResult.isValid()) {
                result.addError("Position validation failed: " + positionResult.getErrors());
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this port is scalar (1x1).
     */
    public boolean isScalar() {
        return dimensions != null && 
               dimensions.size() == 2 && 
               dimensions.get(0) == 1 && 
               dimensions.get(1) == 1;
    }
    
    /**
     * Check if this port is a vector.
     */
    public boolean isVector() {
        if (dimensions == null || dimensions.size() != 2) return false;
        Integer rows = dimensions.get(0);
        Integer cols = dimensions.get(1);
        return (rows == 1 && cols > 1) || (rows > 1 && cols == 1);
    }
    
    /**
     * Check if this port is a matrix.
     */
    public boolean isMatrix() {
        if (dimensions == null || dimensions.size() != 2) return false;
        Integer rows = dimensions.get(0);
        Integer cols = dimensions.get(1);
        return rows > 1 && cols > 1;
    }
    
    /**
     * Get total number of elements (rows * cols).
     */
    public int getElementCount() {
        if (dimensions == null || dimensions.size() != 2) return 0;
        Integer rows = dimensions.get(0);
        Integer cols = dimensions.get(1);
        return (rows != null ? rows : 0) * (cols != null ? cols : 0);
    }
    
    /**
     * Check if this port is compatible with another port for connection.
     */
    public boolean isCompatibleWith(PortDto other) {
        if (other == null) return false;
        
        // Cannot connect ports of same type
        if (this.portType == other.portType) return false;
        
        // Check dimension compatibility
        if (!areDimensionsCompatible(this.dimensions, other.dimensions)) {
            return false;
        }
        
        // Check data type compatibility
        if (this.dataType != null && other.dataType != null) {
            return this.dataType.isCompatibleWith(other.dataType);
        }
        
        return true;
    }
    
    /**
     * Check if dimensions are compatible for connection.
     */
    private boolean areDimensionsCompatible(List<Integer> dims1, List<Integer> dims2) {
        if (dims1 == null || dims2 == null) return true; // Allow if not specified
        if (dims1.size() != 2 || dims2.size() != 2) return false;
        
        // Exact match
        if (dims1.equals(dims2)) return true;
        
        // Scalar can connect to anything
        boolean dims1Scalar = dims1.get(0) == 1 && dims1.get(1) == 1;
        boolean dims2Scalar = dims2.get(0) == 1 && dims2.get(1) == 1;
        if (dims1Scalar || dims2Scalar) return true;
        
        // More complex compatibility rules can be added here
        
        return false;
    }
    
    /**
     * Create a summary string for debugging.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(portType).append(" port[").append(portIndex).append("]");
        if (portName != null) sb.append(" '").append(portName).append("'");
        if (dimensions != null) sb.append(" ").append(dimensions);
        if (dataType != null) sb.append(" ").append(dataType.getTypeName());
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}

/**
 * Data type information for ports.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class DataTypeDto {
    
    /**
     * Type name (e.g., "double", "int32", "boolean", "bus").
     */
    private String typeName;
    
    /**
     * Whether this is a fixed-point type.
     */
    private Boolean isFixedPoint;
    
    /**
     * Word length for fixed-point types.
     */
    private Integer wordLength;
    
    /**
     * Fraction length for fixed-point types.
     */
    private Integer fractionLength;
    
    /**
     * Whether this type is signed.
     */
    private Boolean isSigned;
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (typeName == null || typeName.trim().isEmpty()) {
            result.addError("Data type name is required");
        }
        
        if (Boolean.TRUE.equals(isFixedPoint)) {
            if (wordLength == null || wordLength <= 0) {
                result.addError("Word length must be positive for fixed-point types");
            }
            if (fractionLength == null || fractionLength < 0) {
                result.addError("Fraction length must be non-negative for fixed-point types");
            }
            if (wordLength != null && fractionLength != null && fractionLength >= wordLength) {
                result.addError("Fraction length must be less than word length");
            }
        }
        
        return result;
    }
    
    public boolean isCompatibleWith(DataTypeDto other) {
        if (other == null) return false;
        
        // Same type name is always compatible
        if (typeName != null && typeName.equals(other.typeName)) {
            return true;
        }
        
        // Add more sophisticated compatibility rules here
        // For example: double compatible with single, int32 with int16, etc.
        
        return false;
    }
}

/**
 * Connection information for connected ports.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class ConnectionDto {
    
    /**
     * Source block ID.
     */
    private Integer sourceBlockId;
    
    /**
     * Source port index.
     */
    private Integer sourcePortIndex;
    
    /**
     * Destination block ID.
     */
    private Integer destinationBlockId;
    
    /**
     * Destination port index.
     */
    private Integer destinationPortIndex;
    
    /**
     * Connection name/label.
     */
    private String connectionName;
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (sourceBlockId == null || sourceBlockId < 0) {
            result.addError("Source block ID must be non-negative");
        }
        if (sourcePortIndex == null || sourcePortIndex < 0) {
            result.addError("Source port index must be non-negative");
        }
        if (destinationBlockId == null || destinationBlockId < 0) {
            result.addError("Destination block ID must be non-negative");
        }
        if (destinationPortIndex == null || destinationPortIndex < 0) {
            result.addError("Destination port index must be non-negative");
        }
        
        return result;
    }
}

/**
 * Position information for port layout.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class PortPositionDto {
    
    /**
     * Side of the block where port is located.
     */
    private PortSide side;
    
    /**
     * Position along the side (0.0 to 1.0).
     */
    private Double position;
    
    /**
     * Offset from the calculated position.
     */
    private Double offset;
    
    public enum PortSide {
        LEFT, RIGHT, TOP, BOTTOM
    }
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (position != null && (position < 0.0 || position > 1.0)) {
            result.addError("Port position must be between 0.0 and 1.0");
        }
        
        return result;
    }
}