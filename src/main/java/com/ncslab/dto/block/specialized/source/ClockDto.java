package com.ncslab.dto.block.specialized.source;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for Clock source block.
 * Clock blocks output the current simulation time.
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Clock")
public class ClockDto extends BlockDto {

    /**
     * Sample time for the clock block.
     * 0 = continuous, -1 = inherited, >0 = discrete with specified sample time
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Common values: "double", "single", "int32", "int16", "int8", "uint32", "uint16", "uint8"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Whether to saturate on integer overflow.
     * When enabled, prevents integer overflow by clamping to max/min values.
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // Override getters to provide defaults if null
    public TypedParameter getSampleTime() {
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(0.0);  // Continuous time by default
        }
        return sampleTime;
    }
    
    public TypedParameter getOutDataTypeStr() {
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("Inherit: Same as Simulink");
        }
        return outDataTypeStr;
    }
    
    public TypedParameter getSaturateOnIntegerOverflow() {
        if (saturateOnIntegerOverflow == null) {
            saturateOnIntegerOverflow = TypedParameter.of("off");
        }
        return saturateOnIntegerOverflow;
    }

    /**
     * Initialize default values for parameters
     */
    public ClockDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(0.0);  // Continuous time by default
        }
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("Inherit: Same as Simulink");
        }
        if (saturateOnIntegerOverflow == null) {
            saturateOnIntegerOverflow = TypedParameter.of("off");
        }
    }

    /**
     * Constructs ClockDto with individual parameters.
     *
     * @param blockName                     Name of the block
     * @param blockPath                     Path of the block in the model hierarchy
     * @param sampleTime                   Sample time parameter
     * @param outDataTypeStr               Output data type parameter
     * @param saturateOnIntegerOverflow    Saturation parameter
     */
    public ClockDto(String blockName, String blockPath,
                    TypedParameter sampleTime,
                    TypedParameter outDataTypeStr,
                    TypedParameter saturateOnIntegerOverflow) {
        super(blockName,blockPath);
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs ClockDto with typed parameter map.
     *
     * @param blockName     Name of the block
     * @param blockPath     Path of the block in the model hierarchy
     * @param parameters    Map of typed parameters
     */
    public ClockDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName,blockPath);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 0.0);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "double");
        this.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", Boolean.class, false);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        Double sampleTimeValue = sampleTime.getAsDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue.isNaN() || sampleTimeValue.isInfinite()) {
            addValidationError("Sample time must be >= -1.0 and finite");
            return false;
        }

        // Validate output data type
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null || outDataTypeStr.getAsString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
            return false;
        }

        // Validate saturation parameter
        if (saturateOnIntegerOverflow == null || saturateOnIntegerOverflow.getAsString() == null) {
            addValidationError("Saturate on integer overflow cannot be null");
            return false;
        }

        return true;
    }

    // === Helper Methods ===

    /**
     * Gets the sample time value with validation.
     *
     * @return Sample time value
     * @throws IllegalStateException if sample time is invalid
     */
    public double getSampleTimeValue() {
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            throw new IllegalStateException("Sample time is not properly initialized");
        }
        return sampleTime.getAsDouble();
    }

    /**
     * Gets the output data type string with validation.
     *
     * @return Output data type string
     * @throws IllegalStateException if output data type is invalid
     */
    public String getOutDataTypeString() {
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null) {
            throw new IllegalStateException("Output data type is not properly initialized");
        }
        return outDataTypeStr.getAsString();
    }

    /**
     * Gets the saturation setting with validation.
     *
     * @return Saturation setting
     * @throws IllegalStateException if saturation setting is invalid
     */
    public boolean getSaturateOnIntegerOverflowValue() {
        if (saturateOnIntegerOverflow == null || saturateOnIntegerOverflow.getAsString() == null) {
            throw new IllegalStateException("Saturate on integer overflow is not properly initialized");
        }
        return "on".equals(saturateOnIntegerOverflow.getAsString());
    }

    /**
     * Checks if the clock is configured for continuous time simulation.
     *
     * @return true if continuous time (sample time = 0), false otherwise
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the clock inherits its sample time from the connected block.
     *
     * @return true if inherited (sample time = -1), false otherwise
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the clock is configured for discrete time simulation.
     *
     * @return true if discrete time (sample time > 0), false otherwise
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("ClockDto{blockName='%s', blockPath='%s', sampleTime=%s, outDataType='%s', saturate=%s}",
                getBlockName(), getBlockPath(),
                sampleTime != null ? sampleTime.getAsDouble() : "null",
                outDataTypeStr != null ? outDataTypeStr.getAsString() : "null",
                saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsString() : "null");
    }
}