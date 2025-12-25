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
 * DTO representation of SecondOrderIntegrator block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the SecondOrderIntegrator block
 * which performs double integration: y'' = u.
 *
 * The block maintains two states:
 * - Position (x): The integrated output
 * - Velocity (v = dx/dt): The rate of change of position
 *
 * Mathematical Behavior:
 * - dx/dt = v
 * - dv/dt = u (input is acceleration)
 * - Output y = x (position)
 *
 * SIMULINK Parameters:
 * - InitialConditionX: Initial position value at t=0 (default: 0.0)
 * - InitialConditionDxDt: Initial velocity value at t=0 (default: 0.0)
 * - LimitX: Whether to limit position output (default: false)
 * - UpperLimitX: Upper limit for position (default: Infinity)
 * - LowerLimitX: Lower limit for position (default: -Infinity)
 * - LimitDxDt: Whether to limit velocity (default: false)
 * - UpperLimitDxDt: Upper limit for velocity (default: Infinity)
 * - LowerLimitDxDt: Lower limit for velocity (default: -Infinity)
 *
 * @author NCSLab Team
 * @version 2025
 * @since DTO Migration Phase 2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SecondOrderIntegrator")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.SecondOrderIntegrator")
public class SecondOrderIntegratorDto extends BlockDto {

    // ===== SECOND-ORDER INTEGRATOR BLOCK SPECIFIC PARAMETERS =====

    /**
     * Initial position value at t=0
     * Default: 0.0
     * Validation: Must be finite
     */
    private TypedParameter initialConditionX;

    /**
     * Initial velocity (dx/dt) value at t=0
     * Default: 0.0
     * Validation: Must be finite
     */
    private TypedParameter initialConditionDxDt;

    /**
     * Whether to limit position output
     * Default: false (off)
     */
    private TypedParameter limitX;

    /**
     * Upper limit for position
     * Default: Double.POSITIVE_INFINITY
     */
    private TypedParameter upperLimitX;

    /**
     * Lower limit for position
     * Default: Double.NEGATIVE_INFINITY
     */
    private TypedParameter lowerLimitX;

    /**
     * Whether to limit velocity
     * Default: false (off)
     */
    private TypedParameter limitDxDt;

    /**
     * Upper limit for velocity
     * Default: Double.POSITIVE_INFINITY
     */
    private TypedParameter upperLimitDxDt;

    /**
     * Lower limit for velocity
     * Default: Double.NEGATIVE_INFINITY
     */
    private TypedParameter lowerLimitDxDt;

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInitialConditionXAsDouble() {
        return initialConditionX != null ? initialConditionX.getAsDouble() : 0.0;
    }

    public Double getInitialConditionDxDtAsDouble() {
        return initialConditionDxDt != null ? initialConditionDxDt.getAsDouble() : 0.0;
    }

    public Boolean getLimitXValue() {
        return limitX != null ? limitX.getAsBoolean() : false;
    }

    public Double getUpperLimitXValue() {
        return upperLimitX != null ? upperLimitX.getAsDouble() : Double.POSITIVE_INFINITY;
    }

    public Double getLowerLimitXValue() {
        return lowerLimitX != null ? lowerLimitX.getAsDouble() : Double.NEGATIVE_INFINITY;
    }

    public Boolean getLimitDxDtValue() {
        return limitDxDt != null ? limitDxDt.getAsBoolean() : false;
    }

    public Double getUpperLimitDxDtValue() {
        return upperLimitDxDt != null ? upperLimitDxDt.getAsDouble() : Double.POSITIVE_INFINITY;
    }

    public Double getLowerLimitDxDtValue() {
        return lowerLimitDxDt != null ? lowerLimitDxDt.getAsDouble() : Double.NEGATIVE_INFINITY;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate initial position
        if (initialConditionX != null) {
            Object xValue = initialConditionX.getValue();
            if (xValue instanceof Number) {
                double numValue = ((Number) xValue).doubleValue();
                if (Double.isNaN(numValue) || Double.isInfinite(numValue)) {
                    result.addError("Initial position condition must be finite");
                }
            }
        }

        // Validate initial velocity
        if (initialConditionDxDt != null) {
            Object vValue = initialConditionDxDt.getValue();
            if (vValue instanceof Number) {
                double numValue = ((Number) vValue).doubleValue();
                if (Double.isNaN(numValue) || Double.isInfinite(numValue)) {
                    result.addError("Initial velocity condition must be finite");
                }
            }
        }

        // Validate position limits
        Double upperX = getUpperLimitXValue();
        Double lowerX = getLowerLimitXValue();
        if (upperX != null && lowerX != null) {
            if (!Double.isInfinite(upperX) && !Double.isInfinite(lowerX)) {
                if (upperX <= lowerX) {
                    result.addError("Upper position limit must be greater than lower position limit");
                }
            }
        }

        // Validate velocity limits
        Double upperV = getUpperLimitDxDtValue();
        Double lowerV = getLowerLimitDxDtValue();
        if (upperV != null && lowerV != null) {
            if (!Double.isInfinite(upperV) && !Double.isInfinite(lowerV)) {
                if (upperV <= lowerV) {
                    result.addError("Upper velocity limit must be greater than lower velocity limit");
                }
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if position saturation is enabled
     */
    public boolean isPositionSaturationEnabled() {
        return Boolean.TRUE.equals(getLimitXValue());
    }

    /**
     * Check if velocity saturation is enabled
     */
    public boolean isVelocitySaturationEnabled() {
        return Boolean.TRUE.equals(getLimitDxDtValue());
    }

    /**
     * Check if any saturation is enabled
     */
    public boolean hasAnySaturation() {
        return isPositionSaturationEnabled() || isVelocitySaturationEnabled();
    }

    @Override
    public SecondOrderIntegratorDto copy() {
        return SecondOrderIntegratorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .initialConditionX(initialConditionX != null ? initialConditionX.copy() : null)
                .initialConditionDxDt(initialConditionDxDt != null ? initialConditionDxDt.copy() : null)
                .limitX(limitX != null ? limitX.copy() : null)
                .upperLimitX(upperLimitX != null ? upperLimitX.copy() : null)
                .lowerLimitX(lowerLimitX != null ? lowerLimitX.copy() : null)
                .limitDxDt(limitDxDt != null ? limitDxDt.copy() : null)
                .upperLimitDxDt(upperLimitDxDt != null ? upperLimitDxDt.copy() : null)
                .lowerLimitDxDt(lowerLimitDxDt != null ? lowerLimitDxDt.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (initialConditionX != null) {
            params.put("InitialConditionX", String.valueOf(initialConditionX.getValue()));
        }
        if (initialConditionDxDt != null) {
            params.put("InitialConditionDxDt", String.valueOf(initialConditionDxDt.getValue()));
        }
        if (limitX != null) {
            Boolean limitXVal = limitX.getAsBoolean();
            params.put("LimitX", Boolean.TRUE.equals(limitXVal) ? "on" : "off");
        }
        if (upperLimitX != null) {
            Double upperXVal = upperLimitX.getAsDouble();
            params.put("UpperLimitX", (upperXVal != null && Double.isInfinite(upperXVal)) ? "inf" : String.valueOf(upperXVal));
        }
        if (lowerLimitX != null) {
            Double lowerXVal = lowerLimitX.getAsDouble();
            params.put("LowerLimitX", (lowerXVal != null && Double.isInfinite(lowerXVal)) ? "-inf" : String.valueOf(lowerXVal));
        }
        if (limitDxDt != null) {
            Boolean limitVVal = limitDxDt.getAsBoolean();
            params.put("LimitDxDt", Boolean.TRUE.equals(limitVVal) ? "on" : "off");
        }
        if (upperLimitDxDt != null) {
            Double upperVVal = upperLimitDxDt.getAsDouble();
            params.put("UpperLimitDxDt", (upperVVal != null && Double.isInfinite(upperVVal)) ? "inf" : String.valueOf(upperVVal));
        }
        if (lowerLimitDxDt != null) {
            Double lowerVVal = lowerLimitDxDt.getAsDouble();
            params.put("LowerLimitDxDt", (lowerVVal != null && Double.isInfinite(lowerVVal)) ? "-inf" : String.valueOf(lowerVVal));
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("SecondOrderIntegratorDto{id=%d, name='%s', type='%s', x0=%s, v0=%s, limitX=%s, limitV=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getInitialConditionXAsDouble(),
                           getInitialConditionDxDtAsDouble(),
                           isPositionSaturationEnabled(),
                           isVelocitySaturationEnabled());
    }
}
