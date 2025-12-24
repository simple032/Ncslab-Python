package com.ncslab.block.io;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Defines a single element within a bus definition.
 *
 * <p>A bus element can represent:
 * <ul>
 *   <li>Scalar signal (width=1, height=1)</li>
 *   <li>Vector signal (width>1 or height>1)</li>
 *   <li>Matrix signal (width>1 and height>1)</li>
 *   <li>Nested bus (contains another bus definition)</li>
 * </ul>
 *
 * <p>Usage examples:
 * <pre>{@code
 * // Scalar element
 * BusElementDefinition temp = BusElementDefinition.scalar("temperature", "double");
 *
 * // Vector element
 * BusElementDefinition coeff = BusElementDefinition.vector("coefficients", "double", 10);
 *
 * // Matrix element
 * BusElementDefinition matrix = BusElementDefinition.matrix("data", "double", 3, 4);
 *
 * // Nested bus element
 * BusElementDefinition controller = BusElementDefinition.nestedBus("controller", controllerBusDef);
 * }</pre>
 *
 * @author NCSLab
 * @since 2025
 */
@Data
@Builder
@Slf4j
public class BusElementDefinition {

    /**
     * Element name (e.g., "temperature", "controller").
     * Must be a valid C identifier.
     */
    private String name;

    /**
     * Data type name for primitive elements.
     * Valid values: "double", "single", "int8", "uint8", "int16", "uint16",
     * "int32", "uint32", "int64", "uint64", "boolean".
     * Null if this is a nested bus.
     */
    private String dataTypeName;

    /**
     * Width (number of columns).
     * Default is 1 for scalar elements.
     */
    @Builder.Default
    private int width = 1;

    /**
     * Height (number of rows).
     * Default is 1 for scalar elements.
     */
    @Builder.Default
    private int height = 1;

    /**
     * Whether this element represents a nested bus.
     */
    @Builder.Default
    private boolean isNestedBus = false;

    /**
     * Definition of the nested bus (if isNestedBus=true).
     */
    private BusDefinition nestedBusDefinition;

    /**
     * Optional description of this element.
     */
    private String description;

    /**
     * Element index within parent bus (for code generation).
     * This is set during bus compilation to determine the offset
     * of this element within the parent bus signal vector.
     */
    private int index;

    // Valid C identifier pattern: starts with letter or underscore, followed by letters, digits, or underscores
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    // Valid data types for bus elements
    private static final String[] VALID_DATA_TYPES = {
        "double", "single",
        "int8", "uint8", "int16", "uint16", "int32", "uint32", "int64", "uint64",
        "boolean"
    };

    /**
     * Creates a scalar element definition.
     *
     * @param name element name (must be valid C identifier)
     * @param dataType data type name (e.g., "double", "int32")
     * @return scalar element definition with width=1, height=1
     */
    public static BusElementDefinition scalar(String name, String dataType) {
        return BusElementDefinition.builder()
                .name(name)
                .dataTypeName(dataType)
                .width(1)
                .height(1)
                .isNestedBus(false)
                .build();
    }

    /**
     * Creates a vector element definition.
     *
     * @param name element name (must be valid C identifier)
     * @param dataType data type name (e.g., "double", "int32")
     * @param length vector length (number of elements)
     * @return vector element definition with width=length, height=1
     */
    public static BusElementDefinition vector(String name, String dataType, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Vector length must be positive: " + length);
        }
        return BusElementDefinition.builder()
                .name(name)
                .dataTypeName(dataType)
                .width(length)
                .height(1)
                .isNestedBus(false)
                .build();
    }

    /**
     * Creates a matrix element definition.
     *
     * @param name element name (must be valid C identifier)
     * @param dataType data type name (e.g., "double", "int32")
     * @param height number of rows
     * @param width number of columns
     * @return matrix element definition with specified dimensions
     */
    public static BusElementDefinition matrix(String name, String dataType, int height, int width) {
        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must be positive: %dx%d", height, width));
        }
        return BusElementDefinition.builder()
                .name(name)
                .dataTypeName(dataType)
                .width(width)
                .height(height)
                .isNestedBus(false)
                .build();
    }

    /**
     * Creates a nested bus element definition.
     *
     * @param name element name (must be valid C identifier)
     * @param busDefinition nested bus definition
     * @return nested bus element definition
     */
    public static BusElementDefinition nestedBus(String name, BusDefinition busDefinition) {
        if (busDefinition == null) {
            throw new IllegalArgumentException("Nested bus definition cannot be null");
        }
        return BusElementDefinition.builder()
                .name(name)
                .isNestedBus(true)
                .nestedBusDefinition(busDefinition)
                .width(1)  // Nested bus is treated as single entity
                .height(1)
                .build();
    }

    /**
     * Gets the width of this element.
     * For nested buses, returns the total width of the nested bus.
     *
     * @return element width
     */
    public int getWidth() {
        if (isNestedBus && nestedBusDefinition != null) {
            return nestedBusDefinition.getTotalWidth();
        }
        return width;
    }

    /**
     * Gets the total number of scalar signals in this element.
     * For primitive types: width * height
     * For nested buses: total width of nested bus definition
     *
     * @return total width (number of scalar signals)
     */
    public int getTotalWidth() {
        if (isNestedBus && nestedBusDefinition != null) {
            return nestedBusDefinition.getTotalWidth();
        }
        return width * height;
    }

    /**
     * Checks if this element represents a scalar value.
     *
     * @return true if width=1, height=1, and not a nested bus
     */
    public boolean isScalar() {
        return !isNestedBus && width == 1 && height == 1;
    }

    /**
     * Checks if this element represents a vector.
     * A vector has either width>1 or height>1 (but not both).
     *
     * @return true if this is a vector (1-dimensional array)
     */
    public boolean isVector() {
        return !isNestedBus && ((width > 1 && height == 1) || (width == 1 && height > 1));
    }

    /**
     * Checks if this element represents a matrix.
     * A matrix has both width>1 and height>1.
     *
     * @return true if this is a matrix (2-dimensional array)
     */
    public boolean isMatrix() {
        return !isNestedBus && width > 1 && height > 1;
    }

    /**
     * Validates this element definition.
     *
     * @throws IllegalStateException if the definition is invalid
     */
    public void validate() {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Element name cannot be null or empty");
        }
        if (!VALID_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalStateException(
                    String.format("Element name '%s' is not a valid C identifier", name));
        }

        // Validate dimensions
        if (width <= 0) {
            throw new IllegalStateException(
                    String.format("Element '%s' width must be positive: %d", name, width));
        }
        if (height <= 0) {
            throw new IllegalStateException(
                    String.format("Element '%s' height must be positive: %d", name, height));
        }

        // Validate based on element type
        if (isNestedBus) {
            // Nested bus validation
            if (nestedBusDefinition == null) {
                throw new IllegalStateException(
                        String.format("Element '%s' is marked as nested bus but nestedBusDefinition is null", name));
            }
            // Validate the nested bus definition recursively
            nestedBusDefinition.validate();

            if (dataTypeName != null) {
                log.warn("Element '{}' is a nested bus but has dataTypeName '{}' - this will be ignored",
                        name, dataTypeName);
            }
        } else {
            // Primitive element validation
            if (dataTypeName == null || dataTypeName.trim().isEmpty()) {
                throw new IllegalStateException(
                        String.format("Element '%s' must have a data type", name));
            }

            // Validate data type
            boolean validDataType = false;
            for (String validType : VALID_DATA_TYPES) {
                if (validType.equalsIgnoreCase(dataTypeName)) {
                    validDataType = true;
                    break;
                }
            }
            if (!validDataType) {
                throw new IllegalStateException(
                        String.format("Element '%s' has invalid data type '%s'. Valid types: %s",
                                name, dataTypeName, String.join(", ", VALID_DATA_TYPES)));
            }

            if (nestedBusDefinition != null) {
                log.warn("Element '{}' is not a nested bus but has nestedBusDefinition - this will be ignored",
                        name);
            }
        }

        log.debug("Element '{}' validated successfully: type={}, dimensions={}x{}, totalWidth={}",
                name,
                isNestedBus ? "nested_bus" : dataTypeName,
                height, width,
                getTotalWidth());
    }

    /**
     * Gets a display string for this element's type.
     *
     * @return type string (e.g., "double", "double[10]", "double[3x4]", "Bus: ControllerBus")
     */
    public String getTypeString() {
        if (isNestedBus && nestedBusDefinition != null) {
            return "Bus: " + nestedBusDefinition.getName();
        }

        if (isScalar()) {
            return dataTypeName;
        } else if (isVector()) {
            int length = Math.max(width, height);
            return String.format("%s[%d]", dataTypeName, length);
        } else {
            return String.format("%s[%dx%d]", dataTypeName, height, width);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusElementDefinition that = (BusElementDefinition) o;
        return width == that.width &&
                height == that.height &&
                isNestedBus == that.isNestedBus &&
                index == that.index &&
                Objects.equals(name, that.name) &&
                Objects.equals(dataTypeName, that.dataTypeName) &&
                Objects.equals(nestedBusDefinition, that.nestedBusDefinition) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataTypeName, width, height, isNestedBus,
                nestedBusDefinition, description, index);
    }

    // === DTO Conversion Methods ===

    /**
     * Convert this entity to a DTO.
     *
     * @return BusElementDefinitionDto representation of this element definition
     */
    public com.ncslab.dto.block.specialized.signal.BusElementDefinitionDto toDto() {
        return com.ncslab.dto.block.specialized.signal.BusElementDefinitionDto.builder()
            .name(name)
            .dataTypeName(dataTypeName)
            .width(width)
            .height(height)
            .isNestedBus(isNestedBus)
            .nestedBusDefinition(nestedBusDefinition != null ? nestedBusDefinition.toDto() : null)
            .description(description)
            .build();
    }

    /**
     * Create an entity from a DTO.
     *
     * @param dto the BusElementDefinitionDto to convert
     * @return BusElementDefinition entity
     * @throws IllegalArgumentException if dto is null
     */
    public static BusElementDefinition fromDto(com.ncslab.dto.block.specialized.signal.BusElementDefinitionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Cannot create BusElementDefinition from null DTO");
        }

        BusElementDefinition element = BusElementDefinition.builder()
            .name(dto.getName())
            .dataTypeName(dto.getDataTypeName())
            .width(dto.getWidthValue())
            .height(dto.getHeightValue())
            .isNestedBus(dto.isNestedBusElement())
            .description(dto.getDescription())
            .build();

        if (dto.isNestedBusElement() && dto.getNestedBusDefinition() != null) {
            element.setNestedBusDefinition(BusDefinition.fromDto(dto.getNestedBusDefinition()));
        }

        return element;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BusElementDefinition{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type=").append(getTypeString());
        if (!isScalar()) {
            sb.append(", dimensions=").append(height).append("x").append(width);
        }
        sb.append(", totalWidth=").append(getTotalWidth());
        if (description != null && !description.isEmpty()) {
            sb.append(", description='").append(description).append('\'');
        }
        sb.append(", index=").append(index);
        sb.append('}');
        return sb.toString();
    }
}
