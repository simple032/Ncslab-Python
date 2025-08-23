package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Exponential math block.
 * Exponential blocks compute exponential functions (exp, exp10, exp2, expn).
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonTypeName("Exponential")
public class ExponentialDto extends BlockDto {

    /**
     * Type of exponential operation.
     * Valid values: "exp" (e^x), "exp10" (10^x), "exp2" (2^x), "expn" (custom base)
     */
    private TypedParameter expType;

    /**
     * Custom base for "expn" exponential type.
     * Only used when expType is "expn"
     */
    private TypedParameter customBase;

    /**
     * Enable zero crossing detection.
     * Improves accuracy for signals that cross zero
     */
    private TypedParameter zeroCrossing;

    /**
     * Sample time for the exponential block.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Common values: "Inherit: Same as input", "double", "single"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Whether to saturate on integer overflow.
     * When enabled, prevents integer overflow by clamping to max/min values
     */
    private TypedParameter saturateOnIntegerOverflow;

    /**
     * Constructs ExponentialDto with individual parameters.
     *
     * @param blockName                     Name of the block
     * @param blockPath                     Path of the block in the model hierarchy
     * @param expType                       Exponential type parameter
     * @param customBase                    Custom base parameter
     * @param zeroCrossing                  Zero crossing parameter
     * @param sampleTime                    Sample time parameter
     * @param outDataTypeStr                Output data type parameter
     * @param saturateOnIntegerOverflow     Saturation parameter
     */
    public ExponentialDto(String blockName, String blockPath,
                          TypedParameter expType,
                          TypedParameter customBase,
                          TypedParameter zeroCrossing,
                          TypedParameter sampleTime,
                          TypedParameter outDataTypeStr,
                          TypedParameter saturateOnIntegerOverflow) {
        super("Exponential", blockName, blockPath);
        this.expType = expType;
        this.customBase = customBase;
        this.zeroCrossing = zeroCrossing;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs ExponentialDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public ExponentialDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super("Exponential", blockName, blockPath);
        this.expType = parameters.getTypedParameter("ExpType", String.class, "exp");
        this.customBase = parameters.getTypedParameter("CustomBase", Double.class, 10.0);
        this.zeroCrossing = parameters.getTypedParameter("ZeroCrossing", Boolean.class, true);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "Inherit: Same as input");
        this.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", Boolean.class, false);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate exponential type
        if (expType == null || expType.getAsString() == null || expType.getAsString().trim().isEmpty()) {
            addValidationError("Exponential type cannot be null or empty");
            return false;
        }

        String expTypeValue = expType.getAsString().trim();
        if (!isValidExpType(expTypeValue)) {
            addValidationError("Exponential type must be one of: exp, exp10, exp2, expn");
            return false;
        }

        // Validate custom base
        if (customBase == null || customBase.getAsDouble() == null) {
            addValidationError("Custom base cannot be null");
            return false;
        }

        Double customBaseValue = customBase.getAsDouble();
        if (customBaseValue <= 0.0 || customBaseValue.isNaN() || customBaseValue.isInfinite()) {
            addValidationError("Custom base must be > 0 and finite");
            return false;
        }

        // For expn type, validate custom base is reasonable
        if ("expn".equals(expTypeValue) && (customBaseValue == 1.0)) {
            addValidationError("Custom base cannot be 1.0 for expn type");
            return false;
        }

        // Validate zero crossing
        if (zeroCrossing == null || zeroCrossing.getAsString() == null) {
            addValidationError("Zero crossing setting cannot be null");
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

    private boolean isValidExpType(String type) {
        return "exp".equals(type) || "exp10".equals(type) || "exp2".equals(type) || "expn".equals(type);
    }

    // === Helper Methods ===

    /**
     * Gets the exponential type with validation.
     *
     * @return Exponential type
     * @throws IllegalStateException if exponential type is invalid
     */
    public String getExpTypeValue() {
        if (expType == null || expType.getAsString() == null) {
            throw new IllegalStateException("Exponential type is not properly initialized");
        }
        return expType.getAsString().trim();
    }

    /**
     * Gets the custom base value with validation.
     *
     * @return Custom base value
     * @throws IllegalStateException if custom base is invalid
     */
    public double getCustomBaseValue() {
        if (customBase == null || customBase.getAsDouble() == null) {
            throw new IllegalStateException("Custom base is not properly initialized");
        }
        return customBase.getAsDouble();
    }

    /**
     * Gets the zero crossing setting with validation.
     *
     * @return Zero crossing setting
     * @throws IllegalStateException if zero crossing setting is invalid
     */
    public boolean getZeroCrossingValue() {
        if (zeroCrossing == null || zeroCrossing.getAsString() == null) {
            throw new IllegalStateException("Zero crossing setting is not properly initialized");
        }
        return zeroCrossing.getAsBoolean();
    }

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
        return saturateOnIntegerOverflow.getAsBoolean();
    }

    /**
     * Checks if this is a natural exponential (e^x).
     *
     * @return true if exp type is "exp", false otherwise
     */
    public boolean isNaturalExp() {
        return "exp".equals(getExpTypeValue());
    }

    /**
     * Checks if this is a base-10 exponential (10^x).
     *
     * @return true if exp type is "exp10", false otherwise
     */
    public boolean isBase10Exp() {
        return "exp10".equals(getExpTypeValue());
    }

    /**
     * Checks if this is a base-2 exponential (2^x).
     *
     * @return true if exp type is "exp2", false otherwise
     */
    public boolean isBase2Exp() {
        return "exp2".equals(getExpTypeValue());
    }

    /**
     * Checks if this is a custom base exponential (n^x).
     *
     * @return true if exp type is "expn", false otherwise
     */
    public boolean isCustomBaseExp() {
        return "expn".equals(getExpTypeValue());
    }

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("ExponentialDto{blockName='%s', blockPath='%s', expType='%s', customBase=%s, zeroCrossing=%s, sampleTime=%s}",
                getBlockName(), getBlockPath(),
                expType != null ? expType.getAsString() : "null",
                customBase != null ? customBase.getAsDouble() : "null",
                zeroCrossing != null ? zeroCrossing.getAsString() : "null",
                sampleTime != null ? sampleTime.getAsDouble() : "null");
    }
}