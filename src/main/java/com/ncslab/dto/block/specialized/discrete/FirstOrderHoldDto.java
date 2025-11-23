package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of FirstOrderHold block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the FirstOrderHold block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 *
 * First-Order Hold Behavior:
 * The first-order hold block performs linear extrapolation between samples
 * to provide smoother signal reconstruction than zero-order hold.
 *
 * y(t) = y[k-1] + (y[k-1] - y[k-2]) * (t - t[k-1]) / (t[k-1] - t[k-2])
 *
 * where:
 * - y[k-1] is the most recent sample
 * - y[k-2] is the previous sample
 * - t[k-1] is the time of the most recent sample
 * - t[k-2] is the time of the previous sample
 * - t is the current time
 *
 * Key Features:
 * - Linear extrapolation between samples (more accurate than zero-order hold)
 * - Suitable for signals with relatively constant derivatives
 * - Requires two samples before extrapolation can begin
 * - No feedthrough (breaks algebraic loops)
 *
 * @author NCSLab
 * @version 1.0
 * @since First-Order Hold Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("FirstOrderHold")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.FirstOrderHold")
public class FirstOrderHoldDto extends DiscreteBlockDto {

    // ===== FIRST ORDER HOLD SPECIFIC PARAMETERS =====

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    // ===== PARAMETER ACCESS HELPERS =====

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    // ===== DISCRETE-TIME VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate sample time (inherited from DiscreteBlockDto)
        if (getSampleTime() != null) {
            double sampleTime = getSampleTime().getAsDouble();
            if (sampleTime != -1.0 && sampleTime <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited)");
            }
            if (Double.isNaN(sampleTime) || Double.isInfinite(sampleTime)) {
                result.addError("Sample time must be finite");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Get the extrapolation interval in seconds
     * This is the period between samples where linear extrapolation is performed
     */
    public double getExtrapolationInterval() {
        if (isDiscreteSampleTime()) {
            return getSampleTime().getAsDouble(); // Extrapolate for one sample period
        }
        return Double.NaN; // Cannot determine without knowing inherited sample time
    }

    /**
     * Calculate the Nyquist frequency for anti-aliasing considerations
     */
    public double getNyquistFrequency() {
        if (isDiscreteSampleTime()) {
            return 1.0 / (2.0 * getSampleTime().getAsDouble()); // f_nyquist = 1/(2*T)
        }
        return Double.NaN;
    }

    /**
     * Check if block uses continuous-time sampling (inherited)
     */
    public boolean isContinuous() {
        return getSampleTime() != null && getSampleTime().getAsDouble() == -1.0;
    }

    /**
     * Check if block uses inherited sample time
     */
    public boolean isInherited() {
        return isContinuous();
    }

    /**
     * Get the number of input ports (always 1 for FirstOrderHold)
     */
    public int getInputPortCount() {
        return 1;
    }

    /**
     * Get the number of output ports (always 1 for FirstOrderHold)
     */
    public int getOutputPortCount() {
        return 1;
    }

    // ===== FACTORY METHODS =====

    /**
     * Create first-order hold with specific sample time
     */
    public static FirstOrderHoldDto create(String name, String path, double sampleTime) {
        return FirstOrderHoldDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .sampleTime(TypedParameter.of(sampleTime))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .build();
    }

    /**
     * Create first-order hold with inherited sample time
     */
    public static FirstOrderHoldDto createInherited(String name, String path) {
        return create(name, path, -1.0);
    }

    /**
     * Create first-order hold for common control system frequencies
     * Samples at 10x the signal frequency for good reconstruction
     */
    public static FirstOrderHoldDto createForFrequency(String name, String path, double frequency) {
        // Sample at 10x the signal frequency for good reconstruction
        double sampleTime = 1.0 / (10.0 * frequency);
        return create(name, path, sampleTime);
    }

    @Override
    public FirstOrderHoldDto copy() {
        return FirstOrderHoldDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime() != null ? getSampleTime().copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    @Override
    public com.ncslab.dto.common.TypedParameterMap toParameterMap() {
        com.ncslab.dto.common.TypedParameterMap params = new com.ncslab.dto.common.TypedParameterMap();

        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime());
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", outDataTypeStr);
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("FirstOrderHoldDto{id=%d, name='%s', type='%s', sampleTime=%s, nyquist=%.2fHz}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getSampleTime(),
                           isDiscreteSampleTime() ? getNyquistFrequency() : Double.NaN);
    }
}
