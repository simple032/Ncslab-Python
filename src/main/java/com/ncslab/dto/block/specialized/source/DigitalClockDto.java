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
 * Data Transfer Object for Digital Clock source block.
 * Digital Clock outputs simulation time at discrete sample intervals (sample-and-hold behavior).
 * Between sample times, the block holds the previous sampled time value.
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
@JsonTypeName("DigitalClock")
public class DigitalClockDto extends BlockDto {

    /**
     * Sample time for the digital clock block.
     * The discrete sample period at which the clock updates its output.
     * Must be > 0 for discrete operation.
     * Default: 1.0
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Common values: "double", "single", "int32", "int16", "int8", "uint32", "uint16", "uint8"
     * Default: "double"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Whether to saturate on integer overflow.
     * When enabled, prevents integer overflow by clamping to max/min values.
     * Default: "off"
     */
    private TypedParameter saturateOnIntegerOverflow;

    // Override getters to provide defaults if null
    public TypedParameter getSampleTime() {
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(1.0);  // Discrete time with 1.0 second default
        }
        return sampleTime;
    }

    public TypedParameter getOutDataTypeStr() {
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("double");
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
    public DigitalClockDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        initializeDefaults();
    }

    private void initializeDefaults() {
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(1.0);  // Discrete time with 1.0 second default
        }
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("double");
        }
        if (saturateOnIntegerOverflow == null) {
            saturateOnIntegerOverflow = TypedParameter.of("off");
        }
    }

    /**
     * Constructs DigitalClockDto with individual parameters.
     *
     * @param blockName                     Name of the block
     * @param blockPath                     Path of the block in the model hierarchy
     * @param sampleTime                   Sample time parameter (must be > 0)
     * @param outDataTypeStr               Output data type parameter
     * @param saturateOnIntegerOverflow    Saturation parameter
     */
    public DigitalClockDto(String blockName, String blockPath,
                    TypedParameter sampleTime,
                    TypedParameter outDataTypeStr,
                    TypedParameter saturateOnIntegerOverflow) {
        super(blockName, blockPath);
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs DigitalClockDto with typed parameter map.
     *
     * @param blockName     Name of the block
     * @param blockPath     Path of the block in the model hierarchy
     * @param parameters    Map of typed parameters
     */
    public DigitalClockDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 1.0);
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
        if (sampleTimeValue <= 0.0 || sampleTimeValue.isNaN() || sampleTimeValue.isInfinite()) {
            addValidationError("Digital Clock sample time must be > 0 and finite, got: " + sampleTimeValue);
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
     * Checks if the digital clock is configured for discrete time simulation.
     * Digital clocks are always discrete (sample time > 0).
     *
     * @return true (digital clocks are always discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("DigitalClockDto{blockName='%s', blockPath='%s', sampleTime=%s, outDataType='%s', saturate=%s}",
                getBlockName(), getBlockPath(),
                sampleTime != null ? sampleTime.getAsDouble() : "null",
                outDataTypeStr != null ? outDataTypeStr.getAsString() : "null",
                saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsString() : "null");
    }
}
