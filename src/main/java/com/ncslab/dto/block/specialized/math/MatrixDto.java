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
 * DTO representation of Matrix (constant matrix) block.
 *
 * Outputs a constant matrix value specified in MATLAB format.
 *
 * SIMULINK Parameters:
 * - MatrixValue: Matrix value in MATLAB format (e.g., "[1]", "[1,2;3,4]")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 *
 * Mathematical Behavior:
 * - Outputs constant matrix values
 * - Matrix format: rows separated by semicolon, columns by comma
 * - Supports scalar values (e.g., "5" or "[5]")
 * - Examples:
 *   - Scalar: "1" or "[1]"
 *   - Row vector: "[1,2,3]"
 *   - Column vector: "[1;2;3]"
 *   - Matrix: "[1,2;3,4]"
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
@JsonTypeName("Matrix")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Matrix")
public class MatrixDto extends BlockDto {

    // ===== MATRIX BLOCK SPECIFIC PARAMETERS =====

    /**
     * Matrix value in MATLAB format
     * Default: "[1]" (scalar 1)
     * Examples: "5", "[1,2,3]", "[1;2;3]", "[1,2;3,4]"
     */
    private TypedParameter matrixValue;

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification
     * Default: "double"
     */
    private TypedParameter outDataTypeStr;

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
        if (paramValues.containsKey("MatrixValue")) {
            Object matrixValue = paramValues.get("MatrixValue");
            this.matrixValue = TypedParameter.of(matrixValue);
        }

        if (paramValues.containsKey("SampleTime")) {
            Object stValue = paramValues.get("SampleTime");
            this.sampleTime = TypedParameter.of(stValue);
        }

        if (paramValues.containsKey("OutDataTypeStr")) {
            Object outDtValue = paramValues.get("OutDataTypeStr");
            this.outDataTypeStr = TypedParameter.of(outDtValue);
        }
    }

    // ===== PARAMETER ACCESS HELPERS =====

    public String getMatrixValueAsString() {
        return matrixValue != null ? matrixValue.getAsString() : "[1]";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "double";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate matrix value parameter
        if (matrixValue == null) {
            result.addError("MatrixValue parameter is required");
        } else {
            String matrixStr = matrixValue.getAsString();
            if (matrixStr == null || matrixStr.trim().isEmpty()) {
                result.addError("MatrixValue cannot be empty");
            } else {
                // Basic validation: check for balanced brackets
                if (matrixStr.startsWith("[") && !matrixStr.endsWith("]")) {
                    result.addError("MatrixValue has unbalanced brackets");
                }
                if (!matrixStr.startsWith("[") && matrixStr.endsWith("]")) {
                    result.addError("MatrixValue has unbalanced brackets");
                }
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
     * Check if the matrix is a scalar value
     */
    public boolean isScalar() {
        String matrixStr = getMatrixValueAsString();
        if (matrixStr == null) {
            return false;
        }

        // Try to parse as a single number
        try {
            Double.parseDouble(matrixStr);
            return true;
        } catch (NumberFormatException e) {
            // Check if it's a bracketed single value
            if (matrixStr.startsWith("[") && matrixStr.endsWith("]")) {
                String inner = matrixStr.substring(1, matrixStr.length() - 1).trim();
                try {
                    Double.parseDouble(inner);
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
            return false;
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

    /**
     * Get the number of rows in the matrix
     * Returns -1 if format is invalid or cannot be determined
     */
    public int getRowCount() {
        String matrixStr = getMatrixValueAsString();
        if (matrixStr == null || matrixStr.trim().isEmpty()) {
            return -1;
        }

        // Handle scalar
        if (isScalar()) {
            return 1;
        }

        // Count semicolons + 1
        String inner = matrixStr;
        if (matrixStr.startsWith("[") && matrixStr.endsWith("]")) {
            inner = matrixStr.substring(1, matrixStr.length() - 1);
        }

        int semicolonCount = 0;
        for (char c : inner.toCharArray()) {
            if (c == ';') {
                semicolonCount++;
            }
        }

        return semicolonCount + 1;
    }

    @Override
    public MatrixDto copy() {
        return MatrixDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .matrixValue(matrixValue != null ? matrixValue.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("MatrixValue", matrixValue)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("MatrixDto{id=%d, name='%s', type='%s', value='%s', scalar=%b}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getMatrixValueAsString(),
                           isScalar());
    }
}
