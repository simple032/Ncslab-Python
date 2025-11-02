package com.ncslab.dto.block.specialized.math;

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
 * Data Transfer Object for Modulo math block.
 * Modulo blocks compute remainder operations (fmod, rem).
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
@JsonTypeName("Modulo")
public class ModuloDto extends BlockDto {

    /**
     * Type of modulo operation.
     * Valid values: "fmod" (floating-point remainder), "rem" (signed remainder)
     */
    private TypedParameter moduloType;

    /**
     * Source of divisor value.
     * Valid values: "Internal" (use divisor parameter), "External" (use second input port)
     */
    private TypedParameter divisorSource;

    /**
     * Divisor value when divisorSource is "Internal".
     * Not used when divisorSource is "External"
     */
    private TypedParameter divisor;

    /**
     * Sample time for the modulo block.
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
     * Constructs ModuloDto with individual parameters.
     *
     * @param blockName                     Name of the block
     * @param blockPath                     Path of the block in the model hierarchy
     * @param moduloType                    Modulo type parameter
     * @param divisorSource                 Divisor source parameter
     * @param divisor                       Divisor parameter
     * @param sampleTime                    Sample time parameter
     * @param outDataTypeStr                Output data type parameter
     * @param saturateOnIntegerOverflow     Saturation parameter
     */
    public ModuloDto(String blockName, String blockPath,
                     TypedParameter moduloType,
                     TypedParameter divisorSource,
                     TypedParameter divisor,
                     TypedParameter sampleTime,
                     TypedParameter outDataTypeStr,
                     TypedParameter saturateOnIntegerOverflow) {
        super(blockName,blockPath);
        this.moduloType = moduloType;
        this.divisorSource = divisorSource;
        this.divisor = divisor;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs ModuloDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public ModuloDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName,blockPath);
        this.moduloType = parameters.getTypedParameter("ModuloType", String.class, "fmod");
        this.divisorSource = parameters.getTypedParameter("DivisorSource", String.class, "Internal");
        this.divisor = parameters.getTypedParameter("Divisor", Double.class, 2.0);
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

        // Validate modulo type
        if (moduloType == null || moduloType.getAsString() == null || moduloType.getAsString().trim().isEmpty()) {
            addValidationError("Modulo type cannot be null or empty");
            return false;
        }

        String moduloTypeValue = moduloType.getAsString().trim();
        if (!isValidModuloType(moduloTypeValue)) {
            addValidationError("Modulo type must be one of: fmod, rem");
            return false;
        }

        // Validate divisor source
        if (divisorSource == null || divisorSource.getAsString() == null || divisorSource.getAsString().trim().isEmpty()) {
            addValidationError("Divisor source cannot be null or empty");
            return false;
        }

        String divisorSourceValue = divisorSource.getAsString().trim();
        if (!isValidDivisorSource(divisorSourceValue)) {
            addValidationError("Divisor source must be one of: Internal, External");
            return false;
        }

        // Validate divisor value
        if (divisor == null || divisor.getAsDouble() == null) {
            addValidationError("Divisor cannot be null");
            return false;
        }

        Double divisorValue = divisor.getAsDouble();
        if (divisorValue.isNaN() || divisorValue.isInfinite()) {
            addValidationError("Divisor must be finite");
            return false;
        }

        // For internal divisor, validate it's not zero
        if ("Internal".equals(divisorSourceValue) && divisorValue == 0.0) {
            addValidationError("Internal divisor cannot be zero");
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

    private boolean isValidModuloType(String type) {
        return "fmod".equals(type) || "rem".equals(type);
    }

    private boolean isValidDivisorSource(String source) {
        return "Internal".equals(source) || "External".equals(source);
    }

    // === Helper Methods ===

    /**
     * Gets the modulo type with validation.
     *
     * @return Modulo type
     * @throws IllegalStateException if modulo type is invalid
     */
    public String getModuloTypeValue() {
        if (moduloType == null || moduloType.getAsString() == null) {
            throw new IllegalStateException("Modulo type is not properly initialized");
        }
        return moduloType.getAsString().trim();
    }

    /**
     * Gets the divisor source with validation.
     *
     * @return Divisor source
     * @throws IllegalStateException if divisor source is invalid
     */
    public String getDivisorSourceValue() {
        if (divisorSource == null || divisorSource.getAsString() == null) {
            throw new IllegalStateException("Divisor source is not properly initialized");
        }
        return divisorSource.getAsString().trim();
    }

    /**
     * Gets the divisor value with validation.
     *
     * @return Divisor value
     * @throws IllegalStateException if divisor is invalid
     */
    public double getDivisorValue() {
        if (divisor == null || divisor.getAsDouble() == null) {
            throw new IllegalStateException("Divisor is not properly initialized");
        }
        return divisor.getAsDouble();
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
     * Checks if this is a floating-point modulo (fmod).
     *
     * @return true if modulo type is "fmod", false otherwise
     */
    public boolean isFmod() {
        return "fmod".equals(getModuloTypeValue());
    }

    /**
     * Checks if this is a signed remainder (rem).
     *
     * @return true if modulo type is "rem", false otherwise
     */
    public boolean isRem() {
        return "rem".equals(getModuloTypeValue());
    }

    /**
     * Checks if divisor comes from internal parameter.
     *
     * @return true if divisor source is "Internal", false otherwise
     */
    public boolean isInternalDivisor() {
        return "Internal".equals(getDivisorSourceValue());
    }

    /**
     * Checks if divisor comes from external input port.
     *
     * @return true if divisor source is "External", false otherwise
     */
    public boolean isExternalDivisor() {
        return "External".equals(getDivisorSourceValue());
    }

    /**
     * Gets the number of input ports based on divisor source.
     *
     * @return 1 if internal divisor, 2 if external divisor
     */
    public int getInputPortCount() {
        return isExternalDivisor() ? 2 : 1;
    }

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("ModuloDto{blockName='%s', blockPath='%s', moduloType='%s', divisorSource='%s', divisor=%s, sampleTime=%s}",
                getBlockName(), getBlockPath(),
                moduloType != null ? moduloType.getAsString() : "null",
                divisorSource != null ? divisorSource.getAsString() : "null",
                divisor != null ? divisor.getAsDouble() : "null",
                sampleTime != null ? sampleTime.getAsDouble() : "null");
    }
}