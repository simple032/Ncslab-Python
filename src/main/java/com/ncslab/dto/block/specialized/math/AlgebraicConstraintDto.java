package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of AlgebraicConstraint block.
 *
 * The AlgebraicConstraint block solves the implicit algebraic equation f(z) = 0
 * where f is the input residual and z is the output solution.
 *
 * Uses Newton-Raphson iteration to find z such that f(z) approaches zero.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AlgebraicConstraint")
@MigrationCompatible(originalClass = "com.ncslab.block.math.AlgebraicConstraint")
public class AlgebraicConstraintDto extends BlockDto {

    /**
     * Initial guess for the solver
     * Default: 0.0
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter initialGuess;

    /**
     * Convergence tolerance for the solver
     * Default: 1e-6
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter tolerance;

    /**
     * Maximum number of iterations
     * Default: 100
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter maxIterations;

    /**
     * Post-deserialization hook to populate typed fields from paramValues Map.
     * This method is called automatically after Jackson finishes deserializing the JSON.
     * It extracts values from the legacy paramValues Map and converts them to TypedParameters.
     */
    @com.fasterxml.jackson.annotation.JsonSetter("paramValues")
    public void populateFromParamValues(java.util.Map<String, Object> paramValues) {
        super.setParamValues(paramValues);  // Call parent setter to maintain compatibility

        if (paramValues == null) {
            return;
        }

        // Extract and convert each parameter from paramValues
        if (paramValues.containsKey("InitialGuess")) {
            Object value = paramValues.get("InitialGuess");
            this.initialGuess = TypedParameter.of(value);
        }

        if (paramValues.containsKey("Tolerance")) {
            Object value = paramValues.get("Tolerance");
            this.tolerance = TypedParameter.of(value);
        }

        if (paramValues.containsKey("MaxIterations")) {
            Object value = paramValues.get("MaxIterations");
            this.maxIterations = TypedParameter.of(value);
        }
    }

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInitialGuessValue() {
        return initialGuess != null ? initialGuess.getAsDouble() : 0.0;
    }

    public String getInitialGuessAsString() {
        return initialGuess != null ? initialGuess.getAsString() : "0.0";
    }

    public Double getToleranceValue() {
        return tolerance != null ? tolerance.getAsDouble() : 1e-6;
    }

    public String getToleranceAsString() {
        return tolerance != null ? tolerance.getAsString() : "1e-6";
    }

    public Integer getMaxIterationsValue() {
        return maxIterations != null ? maxIterations.getAsInteger() : 100;
    }

    public String getMaxIterationsAsString() {
        return maxIterations != null ? maxIterations.getAsString() : "100";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate initial guess
        if (initialGuess != null) {
            try {
                Double guess = initialGuess.getAsDouble();
                if (guess != null && (guess.isNaN() || guess.isInfinite())) {
                    result.addError("InitialGuess must be finite");
                }
            } catch (Exception e) {
                result.addError("InitialGuess must be a valid number");
            }
        }

        // Validate tolerance
        if (tolerance != null) {
            try {
                Double tol = tolerance.getAsDouble();
                if (tol != null && tol <= 0.0) {
                    result.addError("Tolerance must be positive");
                }
                if (tol != null && (tol.isNaN() || tol.isInfinite())) {
                    result.addError("Tolerance must be finite");
                }
            } catch (Exception e) {
                result.addError("Tolerance must be a valid number");
            }
        }

        // Validate max iterations
        if (maxIterations != null) {
            try {
                Integer maxIter = maxIterations.getAsInteger();
                if (maxIter != null && maxIter <= 0) {
                    result.addError("MaxIterations must be positive");
                }
            } catch (Exception e) {
                result.addError("MaxIterations must be a valid integer");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if tolerance is very strict (< 1e-10)
     */
    public boolean isStrictTolerance() {
        return getToleranceValue() < 1e-10;
    }

    /**
     * Check if max iterations is high (> 200)
     */
    public boolean isHighIterationLimit() {
        return getMaxIterationsValue() > 200;
    }

    @Override
    public AlgebraicConstraintDto copy() {
        return AlgebraicConstraintDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .initialGuess(initialGuess != null ? initialGuess.copy() : null)
                .tolerance(tolerance != null ? tolerance.copy() : null)
                .maxIterations(maxIterations != null ? maxIterations.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("InitialGuess", initialGuess)
                .put("Tolerance", tolerance)
                .put("MaxIterations", maxIterations)
                .build();
    }

    @Override
    public String toString() {
        return String.format("AlgebraicConstraintDto{id=%d, name='%s', type='%s', guess=%s, tol=%s, maxIter=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getInitialGuessAsString(),
                           getToleranceAsString(),
                           getMaxIterationsAsString());
    }
}
