package com.ncslab.dto.block.specialized.route;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * DTO for Bus Creator block - Creates bus signal from multiple input signals.
 *
 * <p>This block combines multiple input signals into a structured bus signal with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>NumberOfInputs</b>: Number of input ports (default: 2, range: 1-100)</li>
 *   <li><b>ElementNames</b>: Comma-separated names for each bus element (e.g., "signal1,signal2,signal3")</li>
 *   <li><b>BusOutputName</b>: Name of the output bus (default: "Bus")</li>
 *   <li><b>IsVirtual</b>: True for virtual bus (no struct), false for non-virtual (C struct)</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification (default: "Bus: &lt;object name&gt;")</li>
 * </ul>
 * </p>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>NumberOfInputs must be at least 1 and at most 100</li>
 *   <li>ElementNames count must match NumberOfInputs</li>
 *   <li>All element names must be valid identifiers (alphanumeric + underscore)</li>
 *   <li>BusOutputName must be a valid identifier</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Jacksonized
@JsonTypeName("BusCreator")
public class BusCreatorDto extends BlockDto {

    /**
     * Number of input ports.
     * Determines how many signals are combined into the bus.
     */
    @Builder.Default
    private TypedParameter numberOfInputs = TypedParameter.of(2);

    /**
     * Comma-separated element names.
     * Names assigned to each bus element in order.
     */
    @Builder.Default
    private TypedParameter elementNames = TypedParameter.of("signal1,signal2");

    /**
     * Name of the output bus.
     * Used for C struct type name in non-virtual mode.
     */
    @Builder.Default
    private TypedParameter busOutputName = TypedParameter.of("Bus");

    /**
     * Virtual bus flag.
     * True: No C struct generated (signals stored separately)
     * False: C struct generated (signals packed into struct)
     */
    @Builder.Default
    private TypedParameter isVirtual = TypedParameter.of(true);

    /**
     * Sample time for discrete operation.
     * Must be positive for discrete-time operation or -1 for inherited.
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification.
     * Controls the data type of the block output.
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Bus: <object name>");

    /**
     * Creates BusCreatorDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public BusCreatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
    }

    /**
     * Creates BusCreatorDto with comprehensive bus configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param numberOfInputs Number of input ports
     * @param elementNames Comma-separated element names
     * @param busOutputName Output bus name
     * @param isVirtual Virtual bus flag
     */
    public BusCreatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                        int numberOfInputs, String elementNames, String busOutputName, boolean isVirtual) {
        super(blockName, blockPath, position, dimension);
        this.numberOfInputs = TypedParameter.of(numberOfInputs);
        this.elementNames = TypedParameter.of(elementNames);
        this.busOutputName = TypedParameter.of(busOutputName);
        this.isVirtual = TypedParameter.of(isVirtual);
        this.sampleTime = TypedParameter.of(-1.0); // Inherited
        this.outDataTypeStr = TypedParameter.of("Bus: <object name>");
    }

    /**
     * Factory method for creating BusCreatorDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured BusCreatorDto instance
     */
    public static BusCreatorDto fromParameters(String blockName, String blockPath,
                                              BlockPositionDto position, BlockDimensionDto dimension,
                                              TypedParameterMap parameters) {
        BusCreatorDto dto = new BusCreatorDto(blockName, blockPath, position, dimension);

        dto.numberOfInputs = parameters.getTypedParameter("NumberOfInputs", Integer.class)
                                      .orElse(TypedParameter.of(2));
        dto.elementNames = parameters.getTypedParameter("ElementNames", String.class)
                                    .orElse(TypedParameter.of("signal1,signal2"));
        dto.busOutputName = parameters.getTypedParameter("BusOutputName", String.class)
                                     .orElse(TypedParameter.of("Bus"));
        dto.isVirtual = parameters.getTypedParameter("IsVirtual", Boolean.class)
                                 .orElse(TypedParameter.of(true));
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class)
                                  .orElse(TypedParameter.of(-1.0));
        dto.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class)
                                      .orElse(TypedParameter.of("Bus: <object name>"));

        return dto;
    }

    // === Validation Methods ===

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate number of inputs
        if (numberOfInputs != null && numberOfInputs.getAsInteger() != null) {
            int numInputs = numberOfInputs.getAsInteger();
            if (numInputs < 1) {
                errors.add("Number of inputs must be at least 1");
            }
            if (numInputs > 100) {
                errors.add("Number of inputs cannot exceed 100");
            }
        } else {
            errors.add("NumberOfInputs parameter is required");
        }

        // Validate element names
        if (elementNames != null && elementNames.getAsString() != null) {
            String[] names = getElementNamesArray();
            int expectedCount = numberOfInputs != null ? numberOfInputs.getAsInteger() : 0;

            if (names.length != expectedCount) {
                errors.add(String.format("Element names count (%d) must match number of inputs (%d)",
                                        names.length, expectedCount));
            }

            // Validate each element name
            for (String name : names) {
                String trimmed = name.trim();
                if (!isValidIdentifier(trimmed)) {
                    errors.add(String.format("Invalid element name '%s': must be alphanumeric with underscores only", trimmed));
                }
            }
        } else {
            errors.add("ElementNames parameter is required");
        }

        // Validate bus output name
        if (busOutputName != null && busOutputName.getAsString() != null) {
            if (!isValidIdentifier(busOutputName.getAsString())) {
                errors.add("BusOutputName must be a valid identifier (alphanumeric with underscores)");
            }
        } else {
            errors.add("BusOutputName parameter is required");
        }

        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be positive or -1 (inherited)");
            }
        }

        return errors;
    }

    @Override
    public boolean isValidConfiguration() {
        return validateParameters().isEmpty() &&
               numberOfInputs != null && numberOfInputs.getAsInteger() != null && numberOfInputs.getAsInteger() >= 1 &&
               elementNames != null && elementNames.getAsString() != null &&
               busOutputName != null && busOutputName.getAsString() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the number of input ports.
     *
     * @return Number of input ports
     */
    public int getNumberOfInputsValue() {
        return numberOfInputs != null ? numberOfInputs.getAsInteger() : 2;
    }

    /**
     * Gets the element names as an array.
     *
     * @return Array of element names
     */
    public String[] getElementNamesArray() {
        if (elementNames == null || elementNames.getAsString() == null) {
            return new String[]{"signal1", "signal2"};
        }
        String namesStr = elementNames.getAsString();
        return Arrays.stream(namesStr.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
    }

    /**
     * Gets the bus output name.
     *
     * @return Bus output name
     */
    public String getBusOutputNameValue() {
        return busOutputName != null ? busOutputName.getAsString() : "Bus";
    }

    /**
     * Checks if the bus is virtual.
     *
     * @return true if virtual bus, false if non-virtual
     */
    public boolean isVirtualBus() {
        if (isVirtual == null) {
            return true; // Default to virtual
        }
        // Handle both boolean and string representations
        if (isVirtual.getAsBoolean() != null) {
            return isVirtual.getAsBoolean();
        }
        if (isVirtual.getAsString() != null) {
            String value = isVirtual.getAsString().toLowerCase();
            return "true".equals(value) || "on".equals(value) || "1".equals(value);
        }
        return true; // Default to virtual
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    /**
     * Checks if the bus operates in discrete time mode.
     *
     * @return true if sample time is positive (discrete), false if inherited
     */
    public boolean isDiscreteTime() {
        return getSampleTimeValue() > 0.0;
    }

    // === Helper Methods ===

    /**
     * Validates if a string is a valid C/Java identifier.
     *
     * @param identifier String to validate
     * @return true if valid identifier
     */
    private boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        String trimmed = identifier.trim();
        // Must start with letter or underscore, followed by alphanumeric or underscore
        return trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    @Override
    public BusCreatorDto copy() {
        return BusCreatorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .numberOfInputs(numberOfInputs != null ? numberOfInputs.copy() : null)
                .elementNames(elementNames != null ? elementNames.copy() : null)
                .busOutputName(busOutputName != null ? busOutputName.copy() : null)
                .isVirtual(isVirtual != null ? isVirtual.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumberOfInputs", numberOfInputs)
                .put("ElementNames", elementNames)
                .put("BusOutputName", busOutputName)
                .put("IsVirtual", isVirtual)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "NumberOfInputs", "Number of input ports (range: 1-100)",
            "ElementNames", "Comma-separated names for each bus element",
            "BusOutputName", "Name of the output bus (used for C struct type)",
            "IsVirtual", "True for virtual bus (no struct), false for non-virtual (C struct)",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)",
            "OutDataTypeStr", "Output data type specification"
        );
    }

    @Override
    public String toString() {
        return String.format("BusCreatorDto{blockName='%s', numberOfInputs=%d, busOutputName='%s', isVirtual=%b, sampleTime=%.3f}",
                           getBlockName(), getNumberOfInputsValue(), getBusOutputNameValue(), isVirtualBus(), getSampleTimeValue());
    }
}
