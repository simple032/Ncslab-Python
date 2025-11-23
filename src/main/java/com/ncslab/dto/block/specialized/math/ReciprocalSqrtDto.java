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
import lombok.AllArgsConstructor;

/**
 * DTO representation of ReciprocalSqrt (rsqrt) math block.
 *
 * Computes reciprocal square root (inverse square root): output = 1 / sqrt(input)
 * Also known as rsqrt, this operation is commonly used in graphics and normalization.
 *
 * SIMULINK Parameters:
 * - Function: Operation type ("rsqrt" or "sqrt")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Mathematical Behavior:
 * - rsqrt(x) = 1 / sqrt(|x|)
 * - Division by zero returns Infinity
 * - Negative inputs use absolute value
 * - Supports both scalar and matrix inputs (element-wise for matrices)
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ReciprocalSqrt")
@MigrationCompatible(originalClass = "com.ncslab.block.math.ReciprocalSqrt")
public class ReciprocalSqrtDto extends BlockDto {

    // ===== RECIPROCAL SQRT BLOCK SPECIFIC PARAMETERS =====

    /**
     * Operation type ("rsqrt" or "sqrt")
     * Default: "rsqrt"
     * Validation: Must be either "rsqrt" or "sqrt"
     */
    private TypedParameter function;

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;

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
        if (paramValues.containsKey("Function")) {
            Object functionValue = paramValues.get("Function");
            this.function = TypedParameter.of(functionValue);
        }

        if (paramValues.containsKey("SampleTime")) {
            Object stValue = paramValues.get("SampleTime");
            this.sampleTime = TypedParameter.of(stValue);
        }

        if (paramValues.containsKey("OutDataTypeStr")) {
            Object outDtValue = paramValues.get("OutDataTypeStr");
            this.outDataTypeStr = TypedParameter.of(outDtValue);
        }

        if (paramValues.containsKey("SaturateOnIntegerOverflow")) {
            Object satValue = paramValues.get("SaturateOnIntegerOverflow");
            this.saturateOnIntegerOverflow = TypedParameter.of(satValue);
        }
    }

    // ===== PARAMETER ACCESS HELPERS =====

    public String getFunctionValue() {
        return function != null ? function.getAsString() : "rsqrt";
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
        ValidationResult result = super.validate(); // Call parent validation

        // Validate function parameter
        if (function != null) {
            String functionValue = function.getAsString();
            if (functionValue != null &&
                !functionValue.equals("rsqrt") &&
                !functionValue.equals("sqrt")) {
                result.addError("Function must be 'rsqrt' or 'sqrt'");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st != -1.0 && st <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited)");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if function is reciprocal square root
     */
    public boolean isReciprocalSqrt() {
        return "rsqrt".equals(getFunctionValue());
    }

    /**
     * Check if function is forward square root (for compatibility)
     */
    public boolean isForwardSqrt() {
        return "sqrt".equals(getFunctionValue());
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
    public ReciprocalSqrtDto copy() {
        return ReciprocalSqrtDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .function(function != null ? function.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Function", function)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ReciprocalSqrtDto{id=%d, name='%s', type='%s', function='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getFunctionValue());
    }
}
