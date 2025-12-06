package com.ncslab.dto.block.specialized.continuous;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO representation of IntegratorLimited block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the IntegratorLimited block,
 * which is an Integrator with saturation limits. When the output reaches the limits,
 * integration stops to prevent wind-up.
 *
 * Parameters:
 * - InitialCondition: Initial output value at t=0 (default: 0)
 * - UpperLimit: Upper saturation limit (default: Inf)
 * - LowerLimit: Lower saturation limit (default: -Inf)
 * - LimitOutput: Whether to limit output (default: true)
 *
 * @author NCSLab Team
 * @version 2025
 * @since IntegratorLimited Implementation
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IntegratorLimited")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.IntegratorLimited")
public class IntegratorLimitedDto extends BlockDto {

    // ===== INTEGRATOR LIMITED BLOCK SPECIFIC PARAMETERS =====

    /**
     * Initial output value at t=0
     * Default: 0.0
     * Validation: Must be finite
     */
    private TypedParameter initialCondition;

    /**
     * Upper saturation limit
     * Default: Double.POSITIVE_INFINITY
     */
    private TypedParameter upperLimit;

    /**
     * Lower saturation limit
     * Default: Double.NEGATIVE_INFINITY
     */
    private TypedParameter lowerLimit;

    /**
     * Whether to limit output values
     * Default: true (on)
     */
    private TypedParameter limitOutput;

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInitialConditionAsDouble() {
        return initialCondition != null ? initialCondition.getAsDouble() : 0.0;
    }

    public Double getUpperLimitValue() {
        return upperLimit != null ? upperLimit.getAsDouble() : Double.POSITIVE_INFINITY;
    }

    public Double getLowerLimitValue() {
        return lowerLimit != null ? lowerLimit.getAsDouble() : Double.NEGATIVE_INFINITY;
    }

    public Boolean getLimitOutputValue() {
        return limitOutput != null ? limitOutput.getAsBoolean() : true;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate initial condition
        if (initialCondition != null) {
            Object icValue = initialCondition.getValue();
            if (icValue instanceof Number) {
                double numValue = ((Number) icValue).doubleValue();
                if (Double.isNaN(numValue) || Double.isInfinite(numValue)) {
                    result.addError("Initial condition must be finite");
                }
            }
        }

        // Validate saturation limits
        Double upperLimitVal = getUpperLimitValue();
        Double lowerLimitVal = getLowerLimitValue();
        if (upperLimitVal != null && lowerLimitVal != null) {
            if (!Double.isInfinite(upperLimitVal) && !Double.isInfinite(lowerLimitVal)) {
                if (upperLimitVal <= lowerLimitVal) {
                    result.addError("Upper limit must be greater than lower limit");
                }
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if saturation is enabled
     */
    public boolean isSaturationEnabled() {
        return Boolean.TRUE.equals(getLimitOutputValue());
    }

    @Override
    public IntegratorLimitedDto copy() {
        return IntegratorLimitedDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .upperLimit(upperLimit != null ? upperLimit.copy() : null)
                .lowerLimit(lowerLimit != null ? lowerLimit.copy() : null)
                .limitOutput(limitOutput != null ? limitOutput.copy() : null)
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
        if (upperLimit != null) {
            Double upperVal = upperLimit.getAsDouble();
            params.put("UpperLimit", (upperVal != null && Double.isInfinite(upperVal)) ? "inf" : String.valueOf(upperVal));
        }
        if (lowerLimit != null) {
            Double lowerVal = lowerLimit.getAsDouble();
            params.put("LowerLimit", (lowerVal != null && Double.isInfinite(lowerVal)) ? "-inf" : String.valueOf(lowerVal));
        }
        if (limitOutput != null) {
            Boolean limitVal = limitOutput.getAsBoolean();
            params.put("LimitOutput", Boolean.TRUE.equals(limitVal) ? "on" : "off");
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("IntegratorLimitedDto{id=%d, name='%s', type='%s', ic=%s, upper=%s, lower=%s, limit=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getInitialConditionAsDouble(),
                           getUpperLimitValue(),
                           getLowerLimitValue(),
                           getLimitOutputValue());
    }
}
