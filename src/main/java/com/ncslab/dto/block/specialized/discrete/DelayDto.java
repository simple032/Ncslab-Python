package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * DTO for Delay block - Discrete delay implementation with buffer management.
 * 
 * <p>This block provides Z-domain delay functionality with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>DelayLength</b>: Number of samples to delay (positive integer)</li>
 *   <li><b>InitialCondition</b>: Initial value for delay buffer</li>
 *   <li><b>SampleTime</b>: Discrete sample time (positive value or -1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>SaturateOnIntegerOverflow</b>: Handle integer overflow behavior</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>DelayLength must be positive integer</li>
 *   <li>SampleTime must be positive or -1 (inherited)</li>
 *   <li>InitialCondition must be finite</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
@JsonTypeName("Delay")
public class DelayDto extends BlockDto {

    /**
     * Number of samples to delay.
     * Must be a positive integer value.
     */
    private TypedParameter delayLength;

    /**
     * Initial condition for the delay buffer.
     * This value fills the delay buffer at initialization.
     */
    private TypedParameter initialCondition;

    /**
     * Sample time for discrete operation.
     * Must be positive for discrete-time operation or -1 for inherited.
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Controls the data type of the block output.
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow behavior.
     * When enabled, saturates on integer overflow instead of wrapping.
     */
    private TypedParameter saturateOnIntegerOverflow;

    /**
     * Default constructor for Jackson deserialization.
     */

    /**
     * Creates DelayDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public DelayDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super("Delay", blockName, blockPath, position, dimension);
        initializeWithDefaults();
    }

    /**
     * Creates DelayDto with comprehensive delay configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param delayLength Number of samples to delay
     * @param initialCondition Initial condition for delay buffer
     * @param sampleTime Sample time for discrete operation
     */
    public DelayDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                   int delayLength, double initialCondition, double sampleTime) {
        super("Delay", blockName, blockPath, position, dimension);
        this.delayLength = TypedParameter.of(delayLength);
        this.initialCondition = TypedParameter.of(initialCondition);
        this.sampleTime = TypedParameter.of(sampleTime);
        this.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Initialize DTO with SIMULINK-compatible default values.
     */
    private void initializeWithDefaults() {
        this.delayLength = TypedParameter.of(1);
        this.initialCondition = TypedParameter.of(0.0);
        this.sampleTime = TypedParameter.of(-1.0); // Inherited
        this.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Factory method for creating DelayDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured DelayDto instance
     */
    public static DelayDto fromParameters(String blockName, String blockPath, 
                                         BlockPositionDto position, BlockDimensionDto dimension,
                                         TypedParameterMap parameters) {
        DelayDto dto = new DelayDto(blockName, blockPath, position, dimension);
        
        dto.delayLength = parameters.getTypedParameter("DelayLength", Integer.class)
                                   .orElse(TypedParameter.of(1));
        dto.initialCondition = parameters.getTypedParameter("InitialCondition", Double.class)
                                        .orElse(TypedParameter.of(0.0));
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
        
        // Validate delay length
        if (delayLength != null && delayLength.getAsInteger() != null) {
            if (delayLength.getAsInteger() <= 0) {
                errors.add("Delay length must be positive");
            }
        }
        
        // Validate initial condition
        if (initialCondition != null && initialCondition.getAsDouble() != null) {
            Double icValue = initialCondition.getAsDouble();
            if (Double.isNaN(icValue) || Double.isInfinite(icValue)) {
                errors.add("Initial condition must be finite");
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
               delayLength != null && delayLength.getAsInteger() != null && delayLength.getAsInteger() > 0 &&
               initialCondition != null && initialCondition.getAsDouble() != null &&
               sampleTime != null && sampleTime.getAsDouble() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the delay length in samples.
     *
     * @return Number of samples to delay
     */
    public int getDelayLengthValue() {
        return delayLength != null ? delayLength.getAsInteger() : 1;
    }

    /**
     * Gets the initial condition value.
     *
     * @return Initial condition for delay buffer
     */
    public double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsDouble() : 0.0;
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
     * Checks if the delay operates in discrete time mode.
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

    // === Helper Methods ===

    @Override
    public DelayDto copy() {
        return DelayDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .delayLength(delayLength != null ? delayLength.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
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
                .put("DelayLength", delayLength)
                .put("InitialCondition", initialCondition)
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
            "DelayLength", "Number of samples to delay (positive integer)",
            "InitialCondition", "Initial condition for the delay buffer",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)",
            "OutDataTypeStr", "Output data type specification",
            "SaturateOnIntegerOverflow", "Handle integer overflow behavior (on/off)"
        );
    }

    @Override
    public String toString() {
        return String.format("DelayDto{blockName='%s', delayLength=%d, initialCondition=%.3f, sampleTime=%.3f}",
                           getBlockName(), getDelayLengthValue(), getInitialConditionValue(), getSampleTimeValue());
    }
}