package com.ncslab.dto.block.specialized.signal;

import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for BusElement - represents a single element within a bus.
 *
 * A bus element can be either:
 * <ul>
 *   <li>A signal element with specified dimensions and data type</li>
 *   <li>A nested bus element containing another bus structure</li>
 * </ul>
 *
 * <p>This enables hierarchical bus structures where buses can contain other buses,
 * forming complex signal hierarchies for block diagram modeling.
 *
 * @author NCSLab Bus Architecture
 * @version 1.0
 * @since Bus Architecture Phase 1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BusElementDto {

    /**
     * Element name identifier
     * Default: "element"
     */
    @Builder.Default
    private String name = "element";

    /**
     * Whether this element is a nested bus
     * true = nested bus element, false = signal element
     * Default: false
     */
    @Builder.Default
    private Boolean isNestedBus = false;

    /**
     * Nested bus (if isNestedBus is true)
     * Contains the nested bus structure
     */
    private BusSignalDto nestedBus;

    /**
     * Signal width (number of columns) for non-bus elements
     * Default: 1
     */
    @Builder.Default
    private Integer width = 1;

    /**
     * Signal height (number of rows) for non-bus elements
     * Default: 1
     */
    @Builder.Default
    private Integer height = 1;

    /**
     * Data type name for non-bus elements
     * Examples: "double", "int32", "single"
     * Default: "double"
     */
    @Builder.Default
    private String dataTypeName = "double";

    /**
     * Element description/comment
     * Optional documentation for the element
     */
    private String description;

    /**
     * Element index within parent bus (for code generation)
     * Used during code generation to track element order
     */
    private Integer index;

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Check if this element is a nested bus.
     *
     * @return true if nested bus, false if signal element
     */
    public boolean isNestedBusElement() {
        return Boolean.TRUE.equals(isNestedBus);
    }

    /**
     * Check if this element is a signal element.
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
     * Check if this is a scalar element (1x1).
     *
     * @return true if scalar
     */
    public boolean isScalar() {
        return getWidthValue() == 1 && getHeightValue() == 1;
    }

    /**
     * Check if this is a vector element (1xN or Nx1).
     *
     * @return true if vector
     */
    public boolean isVector() {
        return (getWidthValue() == 1 && getHeightValue() > 1) ||
               (getWidthValue() > 1 && getHeightValue() == 1);
    }

    /**
     * Check if this is a matrix element (MxN where M,N > 1).
     *
     * @return true if matrix
     */
    public boolean isMatrix() {
        return getWidthValue() > 1 && getHeightValue() > 1;
    }

    // ===== VALIDATION =====

    /**
     * Validate this bus element.
     *
     * <p>Validation checks:
     * <ul>
     *   <li>Element name is not null or empty</li>
     *   <li>Nested bus elements have valid nested bus DTO</li>
     *   <li>Signal elements have valid dimensions (>= 1)</li>
     *   <li>Signal elements have data type names (warning)</li>
     * </ul>
     *
     * @return validation result with errors and warnings
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // Validate element name
        if (name == null || name.trim().isEmpty()) {
            result.addError("name", "Element name cannot be null or empty");
        }

        // Validate based on element type
        if (Boolean.TRUE.equals(isNestedBus)) {
            // Nested bus - should have nested bus DTO
            if (nestedBus == null) {
                result.addError("nestedBus",
                    "Nested bus element '" + name + "' must have a nested bus DTO");
            } else {
                // Validate nested bus
                ValidationResult nestedResult = nestedBus.validate();
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
                result.addWarning("dataTypeName",
                    "Element '" + name + "' should have a data type name");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Get the dimension string for this element.
     *
     * @return dimension string (e.g., "1x1", "3x4")
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
     * @return a deep copy of this bus element DTO
     */
    public BusElementDto copy() {
        return BusElementDto.builder()
            .name(name)
            .isNestedBus(isNestedBus)
            .nestedBus(nestedBus != null ? nestedBus.copy() : null)
            .width(width)
            .height(height)
            .dataTypeName(dataTypeName)
            .description(description)
            .index(index)
            .build();
    }

    @Override
    public String toString() {
        if (Boolean.TRUE.equals(isNestedBus)) {
            return String.format("BusElementDto{name='%s', nestedBus=%s}",
                               name, nestedBus != null ? nestedBus.getBusName() : "null");
        } else {
            return String.format("BusElementDto{name='%s', dimensions=%dx%d, type=%s}",
                               name, getWidthValue(), getHeightValue(), dataTypeName);
        }
    }
}
