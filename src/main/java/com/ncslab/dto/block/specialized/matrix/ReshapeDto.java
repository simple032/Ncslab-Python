package com.ncslab.dto.block.specialized.matrix;

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
 * DTO representation of Reshape block for matrix reshaping operations.
 *
 * The Reshape block reshapes the input signal to specified output dimensions
 * while preserving the total number of elements.
 * Similar to MATLAB's reshape() function.
 *
 * Example:
 * - Input: [1 2 3 4 5 6] (1x6 vector)
 * - OutputDimensions: [2, 3]
 * - Output: [[1 3 5], [2 4 6]] (2x3 matrix, column-major ordering)
 *
 * SIMULINK Equivalent: Reshape block
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Reshape")
@MigrationCompatible(originalClass = "com.ncslab.block.matrix.Reshape")
public class ReshapeDto extends BlockDto {

    /**
     * Output dimensions as [rows, cols] or [M, N, P, ...] for n-D arrays
     * Format: "2,3" for a 2x3 matrix, "4,1" for a 4x1 column vector
     * Default: "1,1" (scalar)
     */
    @Builder.Default
    private TypedParameter outputDimensions = TypedParameter.of("1,1");

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

    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);

    // ===== PARAMETER ACCESS HELPERS =====

    public String getOutputDimensionsValue() {
        return outputDimensions != null ? outputDimensions.getAsString() : "1,1";
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

    /**
     * Parse output dimensions from string format "rows,cols" to int array
     */
    public int[] getParsedOutputDimensions() {
        String dims = getOutputDimensionsValue();
        String[] parts = dims.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate output dimensions
        if (outputDimensions != null) {
            String dims = outputDimensions.getAsString();
            if (dims != null) {
                try {
                    String[] parts = dims.split(",");
                    if (parts.length < 2) {
                        result.addError("Output dimensions must specify at least 2 dimensions (rows,cols)");
                    }
                    for (String part : parts) {
                        int dim = Integer.parseInt(part.trim());
                        if (dim <= 0) {
                            result.addError("All dimensions must be positive integers");
                        }
                    }
                } catch (NumberFormatException e) {
                    result.addError("Output dimensions must be comma-separated positive integers");
                }
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

    /**
     * Calculate total number of elements from output dimensions
     */
    public int getTotalElements() {
        int[] dims = getParsedOutputDimensions();
        int total = 1;
        for (int dim : dims) {
            total *= dim;
        }
        return total;
    }

    @Override
    public ReshapeDto copy() {
        return ReshapeDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .outputDimensions(outputDimensions != null ? outputDimensions.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("OutputDimensions", outputDimensions)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ReshapeDto{id=%d, name='%s', type='%s', outputDims='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getOutputDimensionsValue());
    }
}
