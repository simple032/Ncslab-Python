package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;
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

/**
 * DTO representation of Discrete PID Controller block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Discrete PID Controller block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - P: Proportional gain (default: 1.0)
 * - I: Integral gain (default: 1.0)
 * - D: Derivative gain (default: 0.0)
 * - N: Filter coefficient for derivative term (default: 100.0)
 * - SampleTime: Sample time Ts for discrete operation (-1 for inherited)
 * - InitialConditionForIntegrator: Initial condition for integral term (default: 0.0)
 * - InitialConditionForFilter: Initial condition for derivative filter (default: 0.0)
 *
 * Discrete PID Implementation using Tustin (bilinear) discretization:
 * - u(k) = P*e(k) + I_sum(k) + Df(k)
 * - I_sum(k) = I_sum(k-1) + I*Ts*(e(k)+e(k-1))/2
 * - Df(k) = (Df(k-1)*(2-N*Ts) + 2*N*D*(e(k)-e(k-1))) / (2+N*Ts)
 *
 * @author NCSLabLink Development Team
 * @version 1.0
 * @since DTO Migration 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiscretePIDController")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.DiscretePIDController")
public class DiscretePIDControllerDto extends DiscreteBlockDto {

    // === Core Discrete PID Controller Parameters ===

    @Builder.Default
    private TypedParameter P = TypedParameter.of(1.0);

    @Builder.Default
    private TypedParameter I = TypedParameter.of(1.0);

    @Builder.Default
    private TypedParameter D = TypedParameter.of(0.0);

    @Builder.Default
    private TypedParameter N = TypedParameter.of(100.0);

    @Builder.Default
    private TypedParameter initialConditionForIntegrator = TypedParameter.of(0.0);

    @Builder.Default
    private TypedParameter initialConditionForFilter = TypedParameter.of(0.0);

    // === Type-Safe Getters ===

    public Double getPValue() {
        return P != null ? P.getAsDouble() : 1.0;
    }

    public Double getIValue() {
        return I != null ? I.getAsDouble() : 1.0;
    }

    public Double getDValue() {
        return D != null ? D.getAsDouble() : 0.0;
    }

    public Double getNValue() {
        return N != null ? N.getAsDouble() : 100.0;
    }

    public Double getInitialConditionForIntegratorValue() {
        return initialConditionForIntegrator != null ? initialConditionForIntegrator.getAsDouble() : 0.0;
    }

    public Double getInitialConditionForFilterValue() {
        return initialConditionForFilter != null ? initialConditionForFilter.getAsDouble() : 0.0;
    }

    // === DTO Methods ===

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate P parameter
        if (P != null && !P.isNumericType()) {
            result.addError("Proportional gain (P) must be a numeric value");
        }

        // Validate I parameter
        if (I != null && !I.isNumericType()) {
            result.addError("Integral gain (I) must be a numeric value");
        }

        // Validate D parameter
        if (D != null && !D.isNumericType()) {
            result.addError("Derivative gain (D) must be a numeric value");
        }

        // Validate N parameter (must be positive)
        if (N != null) {
            if (!N.isNumericType()) {
                result.addError("Filter coefficient (N) must be a numeric value");
            } else {
                double nValue = N.getAsDouble();
                if (nValue <= 0.0 || Double.isNaN(nValue) || Double.isInfinite(nValue)) {
                    result.addError("Filter coefficient (N) must be positive and finite, got: " + nValue);
                }
            }
        }

        // Validate initial condition for integrator
        if (initialConditionForIntegrator != null && !initialConditionForIntegrator.isNumericType()) {
            result.addError("Initial condition for integrator must be a numeric value");
        }

        // Validate initial condition for filter
        if (initialConditionForFilter != null && !initialConditionForFilter.isNumericType()) {
            result.addError("Initial condition for filter must be a numeric value");
        }

        // Validate sample time (must be positive for discrete PID)
        if (sampleTime != null && sampleTime.isNumericType()) {
            double st = sampleTime.getAsDouble();
            if (st != -1.0 && st <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited), got: " + st);
            }
        }

        return result;
    }

    /**
     * Create builder with SIMULINK-compatible defaults for Discrete PID Controller
     */
    public static DiscretePIDControllerDtoBuilder builderWithDefaults() {
        return DiscretePIDControllerDto.builder()
                .P(TypedParameter.of(1.0))
                .I(TypedParameter.of(1.0))
                .D(TypedParameter.of(0.0))
                .N(TypedParameter.of(100.0))
                .sampleTime(TypedParameter.of(-1.0)) // inherited from DiscreteBlockDto
                .initialConditionForIntegrator(TypedParameter.of(0.0))
                .initialConditionForFilter(TypedParameter.of(0.0));
    }
}
