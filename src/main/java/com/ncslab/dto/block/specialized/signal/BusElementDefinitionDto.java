package com.ncslab.dto.block.specialized.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO for BusElementDefinition - defines the schema for a single element within a bus definition.
 *
 * <p>A bus element definition specifies the structure of one element within a bus,
 * including its name, data type, dimensions, and whether it represents a nested bus.</p>
 *
 * <p>This DTO is used as part of BusDefinitionDto to define the complete structure
 * of a bus template that can be reused across multiple bus instances.</p>
 *
 * <p>Key Features:
 * <ul>
 *   <li>Defines element schema with name, type, and dimensions</li>
 *   <li>Supports nested bus definitions for hierarchical structures</li>
 *   <li>Validation for element structure integrity</li>
 *   <li>Conversion methods for entity integration</li>
 * </ul>
 *
 * <p>Example Usage:
 * <pre>{@code
 * // Create a simple scalar element definition
 * BusElementDefinitionDto speedDef = BusElementDefinitionDto.builder()
 *     .name("speed")
 *     .dataTypeName("double")
 *     .width(1)
 *     .height(1)
 *     .build();
 *
 * // Create a matrix element definition
 * BusElementDefinitionDto matrixDef = BusElementDefinitionDto.builder()
 *     .name("sensorData")
 *     .dataTypeName("double")
 *     .width(3)
 *     .height(4)
 *     .description("3x4 sensor data matrix")
 *     .build();
 *
 * // Create a nested bus element definition
 * BusDefinitionDto nestedBusDef = ...; // Define nested bus structure
 * BusElementDefinitionDto nestedDef = BusElementDefinitionDto.builder()
 *     .name("sensors")
 *     .isNestedBus(true)
 *     .nestedBusDefinition(nestedBusDef)
 *     .build();
 * }</pre>
 *
 * @author NCSLab Bus Architecture
 * @version 1.0
 * @since Bus Architecture Phase 2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonTypeName("BusElementDefinition")
public class BusElementDefinitionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Element name identifier.
     * Must be unique within the parent bus definition.
     * Default: "element"
     */
    @Builder.Default
    private String name = "element";

    /**
     * Data type name for this element.
     * Examples: "double", "int32", "single", "uint8"
     * Default: "double"
     */
    @Builder.Default
    private String dataTypeName = "double";

    /**
     * Signal width (number of columns) for non-bus elements.
     * Must be >= 1.
     * Default: 1
     */
    @Builder.Default
    private Integer width = 1;

    /**
     * Signal height (number of rows) for non-bus elements.
     * Must be >= 1.
     * Default: 1
     */
    @Builder.Default
    private Integer height = 1;

    /**
     * Whether this element definition represents a nested bus.
     * true = nested bus element, false = signal element
     * Default: false
     */
    @Builder.Default
    private Boolean isNestedBus = false;

    /**
     * Nested bus definition (if isNestedBus is true).
     * Contains the complete structure of the nested bus.
     */
    private BusDefinitionDto nestedBusDefinition;

    /**
     * Element description/comment.
     * Optional documentation for the element.
     */
    private String description;

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Check if this element definition is a nested bus.
     *
     * @return true if nested bus, false if signal element
     */
    public boolean isNestedBusElement() {
        return Boolean.TRUE.equals(isNestedBus);
    }

    /**
     * Check if this element definition is a signal element.
     *
     * @return true if signal element, false if nested bus
     */
    public boolean isSignalElement() {
        return Boolean.FALSE.equals(isNestedBus);
    }

    /**
     * Get the width value with null safety.
     *
     * @return width value, defaults to 1 if null
     */
    public int getWidthValue() {
        return width != null ? width : 1;
    }

    /**
     * Get the height value with null safety.
     *
     * @return height value, defaults to 1 if null
     */
    public int getHeightValue() {
        return height != null ? height : 1;
    }

    /**
     * Get total number of elements (width * height).
     *
     * @return total element count
     */
    public int getTotalElements() {
        return getWidthValue() * getHeightValue();
    }

    /**
     * Check if this is a scalar element definition (1x1).
     *
     * @return true if scalar
     */
    public boolean isScalar() {
        return getWidthValue() == 1 && getHeightValue() == 1;
    }

    /**
     * Check if this is a vector element definition (1xN or Nx1).
     *
     * @return true if vector
     */
    public boolean isVector() {
        return (getWidthValue() == 1 && getHeightValue() > 1) ||
               (getWidthValue() > 1 && getHeightValue() == 1);
    }

    /**
     * Check if this is a matrix element definition (MxN where M,N > 1).
     *
     * @return true if matrix
     */
    public boolean isMatrix() {
        return getWidthValue() > 1 && getHeightValue() > 1;
    }

    // ===== VALIDATION =====

    /**
     * Validate this bus element definition.
     *
     * <p>Validation checks:
     * <ul>
     *   <li>Element name is not null or empty</li>
     *   <li>Element name contains only valid characters (alphanumeric and underscore)</li>
     *   <li>Nested bus elements have valid nested bus definition</li>
     *   <li>Signal elements have valid dimensions (>= 1)</li>
     *   <li>Signal elements have valid data type names</li>
     *   <li>No circular references in nested bus structures</li>
     * </ul>
     *
     * @return validation result with errors and warnings
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // Validate element name
        if (name == null || name.trim().isEmpty()) {
            result.addError("name", "Element name cannot be null or empty");
        } else if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            result.addError("name",
                "Element name '" + name + "' must start with letter or underscore " +
                "and contain only alphanumeric characters and underscores");
        }

        // Validate based on element type
        if (Boolean.TRUE.equals(isNestedBus)) {
            // Nested bus - should have nested bus definition
            if (nestedBusDefinition == null) {
                result.addError("nestedBusDefinition",
                    "Nested bus element '" + name + "' must have a nested bus definition");
            } else {
                // Validate nested bus definition
                ValidationResult nestedResult = nestedBusDefinition.validate();
                if (!nestedResult.isValid()) {
                    // Merge nested bus validation errors
                    result.merge(nestedResult);
                }
            }
        } else {
            // Signal element - validate dimensions
            if (width != null && width < 1) {
                result.addError("width",
                    "Element '" + name + "' width must be >= 1, got " + width);
            }
            if (height != null && height < 1) {
                result.addError("height",
                    "Element '" + name + "' height must be >= 1, got " + height);
            }

            // Validate data type
            if (dataTypeName == null || dataTypeName.trim().isEmpty()) {
                result.addError("dataTypeName",
                    "Element '" + name + "' must have a data type name");
            }
        }

        return result;
    }

    /**
     * Validate for circular references in nested bus structures.
     * This method should be called by the parent BusDefinitionDto to detect cycles.
     *
     * @param visitedDefinitions Set of bus definition names already visited in the hierarchy
     * @return validation result with circular reference errors
     */
    public ValidationResult validateCircularReferences(java.util.Set<String> visitedDefinitions) {
        ValidationResult result = new ValidationResult();

        if (Boolean.TRUE.equals(isNestedBus) && nestedBusDefinition != null) {
            String nestedName = nestedBusDefinition.getName();
            if (visitedDefinitions.contains(nestedName)) {
                result.addError("nestedBusDefinition",
                    "Circular reference detected: nested bus '" + nestedName +
                    "' creates a cycle in the bus definition hierarchy");
            } else {
                // Add current definition to visited set and validate nested definition
                java.util.Set<String> newVisited = new java.util.HashSet<>(visitedDefinitions);
                newVisited.add(nestedName);
                ValidationResult nestedResult =
                    nestedBusDefinition.validateCircularReferences(newVisited);
                result.merge(nestedResult);
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Get the dimension string for this element definition.
     *
     * @return dimension string (e.g., "1x1", "3x4", "bus")
     */
    public String getDimensionString() {
        if (isNestedBusElement()) {
            return "bus";
        }
        return getWidthValue() + "x" + getHeightValue();
    }

    /**
     * Copy this DTO (deep copy).
     *
     * @return a deep copy of this bus element definition DTO
     */
    public BusElementDefinitionDto copy() {
        return BusElementDefinitionDto.builder()
            .name(name)
            .dataTypeName(dataTypeName)
            .width(width)
            .height(height)
            .isNestedBus(isNestedBus)
            .nestedBusDefinition(nestedBusDefinition != null ? nestedBusDefinition.copy() : null)
            .description(description)
            .build();
    }

    @Override
    public String toString() {
        if (Boolean.TRUE.equals(isNestedBus)) {
            return String.format("BusElementDefinitionDto{name='%s', nestedBus=%s}",
                               name, nestedBusDefinition != null ? nestedBusDefinition.getName() : "null");
        } else {
            return String.format("BusElementDefinitionDto{name='%s', type=%s, dimensions=%dx%d}",
                               name, dataTypeName, getWidthValue(), getHeightValue());
        }
    }
}
