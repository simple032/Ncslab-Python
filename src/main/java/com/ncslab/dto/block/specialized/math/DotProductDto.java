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
 * DTO representation of DotProduct math block.
 *
 * Computes the dot product of two vectors: output = sum(A[i] * B[i])
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Mathematical Behavior:
 * - dot(A, B) = A[0]*B[0] + A[1]*B[1] + ... + A[n]*B[n]
 * - Both inputs must have the same dimensions
 * - Output is always a scalar (1x1)
 * - Supports row vectors (1xN), column vectors (Nx1), and matrices (treated as flattened)
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
@JsonTypeName("DotProduct")
@MigrationCompatible(originalClass = "com.ncslab.block.math.DotProduct")
public class DotProductDto extends BlockDto {

    // ===== DOT PRODUCT BLOCK SPECIFIC PARAMETERS =====

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification
     * Default: "Inherit: Same as first input"
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

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }

    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

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
    public DotProductDto copy() {
        return DotProductDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("DotProductDto{id=%d, name='%s', type='%s', sampleTime=%.2f}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getSampleTimeValue());
    }
}
