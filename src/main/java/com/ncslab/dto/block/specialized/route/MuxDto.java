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

/**
 * DTO for Mux block - Signal multiplexing with multiple input ports.
 * 
 * <p>This block combines multiple scalar signals into a single vector signal with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>Inputs</b>: Number of input ports or vector of input port widths</li>
 *   <li><b>DisplayOrder</b>: Display order of input ports</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>SaturateOnIntegerOverflow</b>: Handle integer overflow behavior</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Inputs must be positive integer</li>
 *   <li>All input signals must be scalars or vectors of same size</li>
 *   <li>Output vector size equals sum of input vector sizes</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Jacksonized
@JsonTypeName("Mux")
public class MuxDto extends BlockDto {

    /**
     * Number of input ports.
     * Determines how many scalar inputs are combined into the output vector.
     */
    @Builder.Default
    private TypedParameter inputs = TypedParameter.of(2);

    /**
     * Display order of input ports.
     * Controls the visual arrangement of input ports.
     */
    @Builder.Default
    private TypedParameter displayOrder = TypedParameter.of("1:N");

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
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Handle integer overflow behavior.
     * When enabled, saturates on integer overflow instead of wrapping.
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of("off");

    /**
     * Creates MuxDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public MuxDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName,blockPath, position, dimension);
    }

    /**
     * Creates MuxDto with comprehensive mux configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param numberOfInputs Number of input ports
     */
    public MuxDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                 int numberOfInputs) {
        super(blockName,blockPath, position, dimension);
        this.inputs = TypedParameter.of(numberOfInputs);
        this.displayOrder = TypedParameter.of("1:N");
        this.sampleTime = TypedParameter.of(-1.0); // Inherited
        this.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Factory method for creating MuxDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured MuxDto instance
     */
    public static MuxDto fromParameters(String blockName, String blockPath, 
                                       BlockPositionDto position, BlockDimensionDto dimension,
                                       TypedParameterMap parameters) {
        MuxDto dto = new MuxDto(blockName, blockPath, position, dimension);
        
        dto.inputs = parameters.getTypedParameter("Inputs", Integer.class)
                              .orElse(TypedParameter.of(2));
        dto.displayOrder = parameters.getTypedParameter("DisplayOrder", String.class)
                                    .orElse(TypedParameter.of("1:N"));
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class)
                                  .orElse(TypedParameter.of(-1.0));
        dto.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class)
                                      .orElse(TypedParameter.of("Inherit: Same as input"));
        dto.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", String.class)
                                                 .orElse(TypedParameter.of("off"));
        
        return dto;
    }

    // === Validation Methods ===

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate number of inputs
        if (inputs != null && inputs.getAsInteger() != null) {
            if (inputs.getAsInteger() < 2) {
                errors.add("Number of inputs must be at least 2");
            }
        }
        
        // Validate display order format
        if (displayOrder != null && displayOrder.getAsString() != null) {
            String order = displayOrder.getAsString();
            if (!order.matches("^(1:N|N:1|\\d+(,\\d+)*)$")) {
                errors.add("Display order must be in format '1:N', 'N:1', or comma-separated numbers");
            }
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
               inputs != null && inputs.getAsInteger() != null && inputs.getAsInteger() >= 2 &&
               displayOrder != null && displayOrder.getAsString() != null &&
               sampleTime != null && sampleTime.getAsDouble() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the number of input ports.
     *
     * @return Number of input ports
     */
    public int getInputsValue() {
        return inputs != null ? inputs.getAsInteger() : 2;
    }

    /**
     * Gets the display order pattern.
     *
     * @return Display order specification
     */
    public String getDisplayOrderValue() {
        return displayOrder != null ? displayOrder.getAsString() : "1:N";
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
     * Checks if the mux operates in discrete time mode.
     *
     * @return true if sample time is positive (discrete), false if inherited
     */
    public boolean isDiscreteTime() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Checks if integer overflow saturation is enabled.
     *
     * @return true if saturation is enabled
     */
    public boolean isSaturationEnabled() {
        return saturateOnIntegerOverflow != null && 
               "on".equals(saturateOnIntegerOverflow.getAsString());
    }

    /**
     * Checks if display order is top-to-bottom (1:N).
     *
     * @return true if display order is 1:N
     */
    public boolean isTopToBottomOrder() {
        return "1:N".equals(getDisplayOrderValue());
    }

    /**
     * Checks if display order is bottom-to-top (N:1).
     *
     * @return true if display order is N:1
     */
    public boolean isBottomToTopOrder() {
        return "N:1".equals(getDisplayOrderValue());
    }

    // === Helper Methods ===

    @Override
    public MuxDto copy() {
        return MuxDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .inputs(inputs != null ? inputs.copy() : null)
                .displayOrder(displayOrder != null ? displayOrder.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
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
                .put("Inputs", inputs)
                .put("DisplayOrder", displayOrder)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Inputs", "Number of input ports (minimum 2)",
            "DisplayOrder", "Display order of input ports (1:N, N:1, or custom)",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)",
            "OutDataTypeStr", "Output data type specification",
            "SaturateOnIntegerOverflow", "Handle integer overflow behavior (on/off)"
        );
    }

    @Override
    public String toString() {
        return String.format("MuxDto{blockName='%s', inputs=%d, displayOrder='%s', sampleTime=%.3f}",
                           getBlockName(), getInputsValue(), getDisplayOrderValue(), getSampleTimeValue());
    }
}