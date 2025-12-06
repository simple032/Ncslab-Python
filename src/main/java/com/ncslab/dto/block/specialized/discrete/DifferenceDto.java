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

import org.apache.commons.lang3.reflect.Typed;

import java.util.HashMap;

/**
 * DTO representation of Difference block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Difference block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - InitialCondition: Initial condition for u[n-1] (default: 0)
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 *
 * Difference Behavior:
 * y[n] = u[n] - u[n-1]
 *
 * The Difference block computes the difference between the current input and
 * the previous input. This is simpler than DiscreteDerivative (no division by Ts, no gain).
 * At the first time step, it uses the initial condition as u[n-1].
 *
 * @author NCSLabLink
 * @version 1.0
 * @since DTO Migration 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Difference")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Difference")
public class DifferenceDto extends DiscreteBlockDto {

    // ===== DIFFERENCE SPECIFIC PARAMETERS =====

    /**
     * Initial condition for u[n-1]
     * Default: 0
     * Validation: Must be finite number
     */
    private TypedParameter initialCondition;

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }

    // ===== DISCRETE-TIME VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

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
     * Create Difference block with specific initial condition and sample time
     */
    public static DifferenceDto create(String name, String path, double initialCondition, double sampleTime) {
        DifferenceDto dto = new DifferenceDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setSampleTime(TypedParameter.of(sampleTime));
        dto.setInitialCondition(TypedParameter.of(initialCondition));

        return dto;
    }

    /**
     * Create Difference block with inherited sample time
     */
    public static DifferenceDto createInherited(String name, String path, double initialCondition) {
        return create(name, path, initialCondition, -1.0);
    }

    @Override
    public DifferenceDto copy() {
        return DifferenceDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("DifferenceDto{id=%d, name='%s', type='%s', initialCondition=%s, sampleTime=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getInitialConditionValue(),
                           getSampleTime());
    }
}
