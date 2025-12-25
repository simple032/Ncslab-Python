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
import lombok.Builder;

/**
 * DTO representation of RoundingFunction block for various rounding operations.
 *
 * The RoundingFunction block applies one of several rounding functions to its input:
 * - floor: Round toward negative infinity
 * - ceil: Round toward positive infinity
 * - round: Round toward nearest integer
 * - fix: Round toward zero (truncate)
 *
 * All operations are element-wise for matrix inputs.
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("RoundingFunction")
@MigrationCompatible(originalClass = "com.ncslab.block.math.RoundingFunction")
public class RoundingFunctionDto extends BlockDto {

    /**
     * Rounding operator selection
     * Valid values: "floor", "ceil", "round", "fix"
     * Default: "floor"
     */
    @Builder.Default
    private TypedParameter operator = TypedParameter.of("floor");

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

    // ===== PARAMETER ACCESS HELPERS =====

    public String getOperatorValue() {
        return operator != null ? operator.getAsString() : "floor";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate operator
        if (operator != null) {
            String op = operator.getAsString();
            if (op != null && !isValidOperator(op)) {
                result.addError("Operator must be one of: floor, ceil, round, fix");
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

    private boolean isValidOperator(String op) {
        return "floor".equals(op) || "ceil".equals(op) ||
               "round".equals(op) || "fix".equals(op);
    }

    // ===== OPERATOR CHECKING METHODS =====

    public boolean isFloor() {
        return "floor".equals(getOperatorValue());
    }

    public boolean isCeil() {
        return "ceil".equals(getOperatorValue());
    }

    public boolean isRound() {
        return "round".equals(getOperatorValue());
    }

    public boolean isFix() {
        return "fix".equals(getOperatorValue());
    }

    // ===== UTILITY METHODS =====

    /**
     * Get the C/C++ function name for this rounding operation
     */
    public String getCFunctionName() {
        String op = getOperatorValue();
        switch (op) {
            case "floor":
                return "floor";
            case "ceil":
                return "ceil";
            case "round":
                return "round";
            case "fix":
                return "trunc"; // fix is equivalent to trunc in C
            default:
                return "floor"; // Default fallback
        }
    }

    /**
     * Get human-readable description of the rounding operation
     */
    public String getOperatorDescription() {
        String op = getOperatorValue();
        switch (op) {
            case "floor":
                return "Round toward negative infinity";
            case "ceil":
                return "Round toward positive infinity";
            case "round":
                return "Round toward nearest integer";
            case "fix":
                return "Truncate toward zero";
            default:
                return "Rounding operation";
        }
    }

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

    @Override
    public RoundingFunctionDto copy() {
        return RoundingFunctionDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .operator(operator != null ? operator.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Operator", operator)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("RoundingFunctionDto{id=%d, name='%s', type='%s', operator='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getOperatorValue());
    }
}
