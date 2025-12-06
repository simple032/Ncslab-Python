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
 * DTO representation of DiscreteDerivative block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the DiscreteDerivative block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - Gain: Gain multiplier (K in the formula, default: 1.0)
 * - InitialCondition: Initial condition for u[n-1] (default: 0)
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Discrete Derivative Behavior:
 * y[n] = K * (u[n] - u[n-1]) / Ts
 *
 * The discrete derivative block computes the backward difference derivative
 * of the input signal with respect to the sample time. The Gain parameter K
 * provides additional scaling of the derivative.
 *
 * @author NCSLab
 * @version 1.0
 * @since 2025-12-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiscreteDerivative")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.DiscreteDerivative")
public class DiscreteDerivativeDto extends DiscreteBlockDto {

    // ===== DISCRETE DERIVATIVE SPECIFIC PARAMETERS =====

    /**
     * Gain multiplier (K in the derivative formula)
     * Default: 1.0
     * Validation: Must be finite number
     */
    private TypedParameter gain;

    /**
     * Initial condition for u[n-1] (previous input value)
     * Default: 0
     * Validation: Must be finite number
     */
    private TypedParameter initialCondition;

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getGainValue() {
        return gain != null ? gain.getValue(Double.class) : 1.0;
    }

    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }

    // ===== DISCRETE-TIME VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate gain
        if (gain != null) {
            try {
                Double gainVal = gain.getValue(Double.class);
                if (gainVal != null && (Double.isNaN(gainVal) || Double.isInfinite(gainVal))) {
                    result.addError("Gain must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid gain format: " + e.getMessage());
            }
        }

        // Validate initial condition
        if (initialCondition != null) {
            try {
                Double icVal = initialCondition.getValue(Double.class);
                if (icVal != null && (Double.isNaN(icVal) || Double.isInfinite(icVal))) {
                    result.addError("Initial condition must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid initial condition format: " + e.getMessage());
            }
        }

        // Validate discrete sample time
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
     * Create discrete derivative with specific gain, initial condition and sample time
     */
    public static DiscreteDerivativeDto create(String name, String path, double gain, double initialCondition, double sampleTime) {
        DiscreteDerivativeDto dto = new DiscreteDerivativeDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setSampleTime(TypedParameter.of(sampleTime));

        // Set default parameters
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));

        // Set specific parameters
        dto.setGain(TypedParameter.of(gain));
        dto.setInitialCondition(TypedParameter.of(initialCondition));

        return dto;
    }

    /**
     * Create discrete derivative with inherited sample time
     */
    public static DiscreteDerivativeDto createInherited(String name, String path, double gain, double initialCondition) {
        return create(name, path, gain, initialCondition, -1.0);
    }

    /**
     * Create discrete derivative with default gain and inherited sample time
     */
    public static DiscreteDerivativeDto createSimple(String name, String path) {
        return create(name, path, 1.0, 0.0, -1.0);
    }

    @Override
    public DiscreteDerivativeDto copy() {
        return DiscreteDerivativeDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .gain(gain != null ? gain.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (gain != null) {
            params.put("Gain", String.valueOf(gain.getValue()));
        }
        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("DiscreteDerivativeDto{id=%d, name='%s', type='%s', gain=%s, initialCondition=%s, sampleTime=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getGainValue(),
                           getInitialConditionValue(),
                           getSampleTime());
    }
}
