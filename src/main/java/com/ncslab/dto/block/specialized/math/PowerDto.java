package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Power math block.
 * Power blocks compute power operations (element-wise or matrix power).
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonTypeName("Power")
public class PowerDto extends BlockDto {

    /**
     * Method for power operation.
     * Valid values: "Element-wise(.^)" (element-wise power), "Matrix(^)" (matrix power)
     */
    private TypedParameter powerMethod;

    /**
     * Sample time for the power block.
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
     * Constructs PowerDto with individual parameters.
     *
     * @param blockName                     Name of the block
     * @param blockPath                     Path of the block in the model hierarchy
     * @param powerMethod                   Power method parameter
     * @param sampleTime                    Sample time parameter
     * @param outDataTypeStr                Output data type parameter
     * @param saturateOnIntegerOverflow     Saturation parameter
     */
    public PowerDto(String blockName, String blockPath,
                    TypedParameter powerMethod,
                    TypedParameter sampleTime,
                    TypedParameter outDataTypeStr,
                    TypedParameter saturateOnIntegerOverflow) {
        super("Power", blockName, blockPath);
        this.powerMethod = powerMethod;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs PowerDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public PowerDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super("Power", blockName, blockPath);
        this.powerMethod = parameters.getTypedParameter("PowerMethod", String.class, "Element-wise(.^)");
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

        // Validate power method
        if (powerMethod == null || powerMethod.getAsString() == null || powerMethod.getAsString().trim().isEmpty()) {
            addValidationError("Power method cannot be null or empty");
            return false;
        }

        String powerMethodValue = powerMethod.getAsString().trim();
        if (!isValidPowerMethod(powerMethodValue)) {
            addValidationError("Power method must be one of: Element-wise(.^), Matrix(^)");
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
        if (saturateOnIntegerOverflow == null || saturateOnIntegerOverflow.getAsBoolean() == null) {
            addValidationError("Saturate on integer overflow cannot be null");
            return false;
        }

        return true;
    }

    private boolean isValidPowerMethod(String method) {
        return "Element-wise(.^)".equals(method) || "Matrix(^)".equals(method);
    }

    // === Helper Methods ===

    /**
     * Gets the power method with validation.
     *
     * @return Power method
     * @throws IllegalStateException if power method is invalid
     */
    public String getPowerMethodValue() {
        if (powerMethod == null || powerMethod.getAsString() == null) {
            throw new IllegalStateException("Power method is not properly initialized");
        }
        return powerMethod.getAsString().trim();
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
        if (saturateOnIntegerOverflow == null || saturateOnIntegerOverflow.getAsBoolean() == null) {
            throw new IllegalStateException("Saturate on integer overflow is not properly initialized");
        }
        return saturateOnIntegerOverflow.getAsBoolean();
    }

    /**
     * Checks if this is element-wise power operation (.^).
     *
     * @return true if power method is "Element-wise(.^)", false otherwise
     */
    public boolean isElementWise() {
        return "Element-wise(.^)".equals(getPowerMethodValue());
    }

    /**
     * Checks if this is matrix power operation (^).
     *
     * @return true if power method is "Matrix(^)", false otherwise
     */
    public boolean isMatrixPower() {
        return "Matrix(^)".equals(getPowerMethodValue());
    }

    /**
     * Gets the number of input ports (always 2 for power operation).
     *
     * @return 2 (base and exponent inputs)
     */
    public int getInputPortCount() {
        return 2;
    }

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("PowerDto{blockName='%s', blockPath='%s', powerMethod='%s', sampleTime=%s, saturate=%s}",
                getBlockName(), getBlockPath(),
                powerMethod != null ? powerMethod.getAsString() : "null",
                sampleTime != null ? sampleTime.getAsDouble() : "null",
                saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : "null");
    }
}