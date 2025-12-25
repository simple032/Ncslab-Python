package com.ncslab.dto.block.specialized.discontinuous;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of Quantizer discontinuous block.
 *
 * The Quantizer block discretizes input signals to a specified quantization interval.
 * Output is computed as: y = q * round(u / q) where q is the quantization interval.
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Quantizer")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.Quantizer")
public class QuantizerDto extends BlockDto {

    /**
     * Quantization interval (step size)
     * Default: 0.5
     * Validation: Must be positive and non-zero
     */
    @Builder.Default
    private TypedParameter quantizationInterval = TypedParameter.of(0.5);

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getQuantizationIntervalValue() {
        return quantizationInterval != null ? quantizationInterval.getAsDouble() : 0.5;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate quantization interval
        Double interval = getQuantizationIntervalValue();

        if (interval != null) {
            if (interval <= 0.0) {
                result.addError("Quantization interval must be positive and non-zero");
            }

            if (interval.isNaN() || interval.isInfinite()) {
                result.addError("Quantization interval must be finite");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Apply quantization to input value
     * Formula: y = q * round(u / q)
     */
    public double quantize(double input) {
        double q = getQuantizationIntervalValue();
        return q * Math.round(input / q);
    }

    /**
     * Get the number of quantization levels within a range
     */
    public int getQuantizationLevels(double range) {
        return (int) Math.ceil(range / getQuantizationIntervalValue());
    }

    /**
     * Calculate quantization error for a given input
     */
    public double getQuantizationError(double input) {
        return input - quantize(input);
    }

    @Override
    public QuantizerDto copy() {
        return QuantizerDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .quantizationInterval(quantizationInterval != null ? quantizationInterval.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("QuantizationInterval", quantizationInterval)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("QuantizerDto{id=%d, name='%s', type='%s', interval=%.3f}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getQuantizationIntervalValue());
    }
}
